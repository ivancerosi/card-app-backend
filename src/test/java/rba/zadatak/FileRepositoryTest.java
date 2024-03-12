package rba.zadatak;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rba.zadatak.entity.CardApplicant;
import rba.zadatak.enums.CardStatus;
import rba.zadatak.repository.FileRepositoryFragment;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SpringBootTest
public class FileRepositoryTest {

    @Autowired
    FileRepositoryFragment<CardApplicant, String> fileRepositoryFragment;

    Set<File> fileDiff(File[] before, File[] after) {
        Set<File> beforeSet = new HashSet<>(Arrays.asList(before));
        Set<File> afterSet = new HashSet<>(Arrays.asList(after));

        afterSet.removeAll(beforeSet);
        return afterSet;
    }

    private File[] dirContent;

    @BeforeEach
    void scanDirectory() {
        dirContent=new File(".").listFiles();
    }

    @AfterEach
    void cleanDirectory() {
        Set<File> toClean = fileDiff(dirContent,new File(".").listFiles());
        toClean.forEach(it->it.delete());
    }

    @Test
    void createFile() {
        CardApplicant entity = new CardApplicant();
        entity.setPin("12345678900");
        entity.setFirstName("ime");
        entity.setLastName("prezime");
        entity.setStatus(CardStatus.ACCEPTED);


        File file = fileRepositoryFragment.writeFile(entity);

        assertTrue(file.exists());
        assertTrue(file.getName().contains(entity.getPin()));
    }

    @Test
    void readFile() {
        CardApplicant entity = new CardApplicant();
        entity.setPin("12345678900");
        entity.setFirstName("ime");
        entity.setLastName("prezime");
        entity.setStatus(CardStatus.ACCEPTED);
        File file = fileRepositoryFragment.writeFile(entity);

        CardApplicant readEntity = fileRepositoryFragment.readFile(file);

        assertEquals(entity, readEntity);
    }

    @Test
    void updateFile() {
        CardStatus updatedStatus = CardStatus.OUTDATED;

        CardApplicant entity = new CardApplicant();
        entity.setPin("12345678900");
        entity.setFirstName("ime");
        entity.setLastName("prezime");

        entity.setStatus(CardStatus.ACCEPTED);
        File file = fileRepositoryFragment.writeFile(entity);

        entity.setStatus(updatedStatus);
        fileRepositoryFragment.updateFile(entity, file);

        CardApplicant readEntity = fileRepositoryFragment.readFile(file);

        assertEquals(updatedStatus, readEntity.getStatus());
    }

}
