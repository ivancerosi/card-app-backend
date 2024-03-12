package rba.zadatak.repository;

import java.io.File;

public interface FileRepositoryFragment<T, ID> {
    T readFile(File file);
    File writeFile(T entity);
    T updateFile(T entity, File file);
    void deleteFile(File file);
}
