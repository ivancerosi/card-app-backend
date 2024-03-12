package rba.zadatak.repository;

import jakarta.persistence.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rba.zadatak.annotations.WriteToFile;
import rba.zadatak.entity.CardApplicant;
import rba.zadatak.exceptions.CardApplicantFileError;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * Ovaj repozitorij tj. datasource oslanja se na lockove u RDMBS-u iz razlog sto se entity uvijek prvo dohvaca iz
 * relacijske baze stoga nije potrebno ugradjivati locking mehanizam ovdje
 */
public class FileRepositoryFragmentImpl<T extends CardApplicant, ID> implements FileRepositoryFragment<T, ID> {
    private static final Logger logger = LoggerFactory.getLogger("rba.zadatak");
    Class<T> entityClass;
    List<Field> fieldSequence = new ArrayList<>();
    String idFieldName;

    private final String CHARSET="utf-8";


    private Object readField(Field field, Object obj) {
        Object value=null;
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            try {
                field.setAccessible(true);
                value = field.get(obj);
                field.setAccessible(false);
            } catch (IllegalAccessException e2) {
                logger.error("Can't read from field",e2);
            }
        }
        return value;
    }

    private Object writeField(Field field, Object obj, Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            try {
                field.setAccessible(true);
                field.set(obj,value);
                field.setAccessible(false);
            } catch (IllegalAccessException e2) {
                logger.error("Can't use reflection to write to field {} of class {}", field.getName(), entityClass.getName());
            }
        }
        return value;
    }

    private Object writeEnum(Field field, Object obj, Object value) {
        try {
            Method m = field.getType().getMethod("valueOf", String.class);
            value = m.invoke(obj,value);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            try {
                field.setAccessible(true);
                field.set(obj, value);
                field.setAccessible(false);
            } catch (IllegalAccessException e2) {
                logger.error("Can't use reflection to write to field {} of class {}", field.getName(), entityClass.getName());
            }
        } catch(NoSuchMethodException e) {
            // svaki enum ima valueOf
            throw new RuntimeException(String.format("Enum %s doesn't have valueOf method", field.getType().getName()), e);
        } catch(InvocationTargetException e) {
            // svaki enum ima valueOf
            throw new RuntimeException(String.format("Error when invoking valueOf method of enum %s", field.getType().getName()), e);
        }
        return value;
    }

    private String generateFileName(Object id) {
        return String.format("%s-%s.txt", id.toString(), Instant.now().getEpochSecond());
    }

    public FileRepositoryFragmentImpl(Class<T> entityClass) {
        this.entityClass = entityClass;

        int position=0;

        for (Field f: entityClass.getDeclaredFields()) {
            if (f.getAnnotationsByType(WriteToFile.class).length>0) {
                fieldSequence.add(f);
                position+=1;
            }
            if (f.getAnnotationsByType(Id.class).length>0) {
                this.idFieldName = f.getName();
            }
        }

        if (position>0 && idFieldName==null) {
            logger.error("Entity class {} doesn't contain Id annotation", entityClass.getName());
            throw new RuntimeException("No id annotation specified for field in class "+entityClass.getName());
        }
        try {
            entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Unable to invoke constructor via reflection", e);
            throw new RuntimeException("Unable to invoke constructor via reflection for entity class {} "+entityClass.getName());
        }
    }

    private File writeFile(T entity, String filename) {
        try {
            try (PrintWriter pw = new PrintWriter(filename, Charset.forName(CHARSET))) {
                boolean first = true;
                for (Field field : fieldSequence) {
                    if (first) {
                        first = false;
                        pw.write(readField(field, entity).toString());
                    } else {
                        pw.write("," + readField(field, entity).toString());
                    }
                }
            }
            return new File(filename);
        } catch(Exception e) {
            logger.error("Unable to write CardApplicant to text file",e);
            throw new CardApplicantFileError("Unable to write CardApplicant to text file", e);
        }
    }

    @Override
    public T readFile(File file) {
        T entity=null;
        try {
            entity = entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // vec smo u konstruktoru provjerili da ovo radi, ali za svaki slucaj...
            logger.error("Unable to invoke constructor via reflection", e);
            return null;
        }

        try (Scanner read = new Scanner(file, Charset.forName(CHARSET))){
            read.useDelimiter(",");
            int position=0;

            while (read.hasNext()) {
                Object value = read.next();
                Field f = fieldSequence.get(position);
                if (f.getType().isEnum()) writeEnum(f, entity, value);
                else writeField(f, entity, value);
                position+=1;
            }

        } catch(Exception e) {
            logger.error("Exception when reading CardApplicant text file", e);
            throw new CardApplicantFileError("Unable to read CardApplicant text file", e);
        }
        return entity;
    }


    @Override
    public File writeFile(T entity) {
        File file;
        try {
            Field id = entity.getClass().getDeclaredField(idFieldName);
            String filename = generateFileName(readField(id, entity));
            file=writeFile(entity, filename);
        } catch(Exception e) {
            logger.error("Unable to write CardApplicant to text file",e);
            throw new CardApplicantFileError("Unable to write CardApplicant to text file", e);
        }
        return file;
    }


    @Override
    public T updateFile(T entity, File file) {
        T memento;
        try {
            memento = readFile(file);
        } catch(Exception e) {
            logger.error("Unable to read file "+file.getName());
            throw new CardApplicantFileError("Unable to read file while updating CardApplicant entity", e);
        }
        try {
            deleteFile(file);
            writeFile(entity,file.getName());
        } catch (Exception e) {
            // rollback i log
            deleteFile(file);
            writeFile(memento, file.getName());

            logger.error("Unable to update file "+file.getName());
            throw new CardApplicantFileError("Unable to update CardApplicant text file inside the update transaction",e);
        }
        return entity;
    }

    @Override
    public void deleteFile(File file) {
        file.delete();
    }

}
