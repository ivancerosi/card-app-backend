package rba.zadatak;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import rba.zadatak.dto.CardApplicantDto;
import rba.zadatak.entity.CardApplicant;
import rba.zadatak.enums.CardStatus;
import rba.zadatak.repository.FileRepositoryFragment;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ControllerTest {
    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    Controller controller;

    @Autowired
    FileRepositoryFragment<CardApplicant, String> fileRepositoryFragment;

    @Value("${spring.port}")
    int port;

    private File[] dirContent;

    @Test
    void folder() {
        File local = new File(".");
        for (var file:local.listFiles()) {
            System.out.println(file.getName());
        }
    }

    Set<File> fileDiff(File[] before, File[] after) {
        Set<File> beforeSet = new HashSet<>(Arrays.asList(before));
        Set<File> afterSet = new HashSet<>(Arrays.asList(after));

        afterSet.removeAll(beforeSet);
        return afterSet;
    }

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
    void contextLoads() {
        assertNotNull(controller);
    }

    @Test
    void testCreate() {
        File local = new File(".");
        File[] before = local.listFiles();

        CardApplicantDto dto = new CardApplicantDto();
        dto.setFirstName("ime");
        dto.setLastName("prezime");
        dto.setPin("12345678900");
        dto.setStatus(CardStatus.ACCEPTED);

        ResponseEntity<CardApplicantDto> resp = controller.save(dto);

        assertEquals(HttpStatus.OK,resp.getStatusCode());
        assertEquals(dto.getFirstName(),resp.getBody().getFirstName());
        assertEquals(dto.getLastName(),resp.getBody().getLastName());
        assertEquals(dto.getPin(),resp.getBody().getPin());
        assertEquals(CardStatus.PENDING,resp.getBody().getStatus());

        resp = controller.getApplicant(dto.getPin());

        assertEquals(HttpStatus.OK,resp.getStatusCode());
        assertEquals(dto.getFirstName(),resp.getBody().getFirstName());
        assertEquals(dto.getLastName(),resp.getBody().getLastName());
        assertEquals(dto.getPin(),resp.getBody().getPin());
        assertEquals(CardStatus.ACCEPTED,resp.getBody().getStatus());

        File[] after = local.listFiles();
        Set<File> diff = fileDiff(before,after);

        assertEquals(diff.size(), 1);

        File createdFile = (File) diff.toArray()[0];

        assertTrue(createdFile.getName().contains(dto.getPin()));

        CardApplicant applicant = fileRepositoryFragment.readFile(createdFile);

        assertEquals(dto.getFirstName(),applicant.getFirstName());
        assertEquals(dto.getFirstName(),applicant.getFirstName());
        assertEquals(dto.getFirstName(),applicant.getFirstName());
        assertEquals(applicant.getStatus(), CardStatus.ACCEPTED);

    }

    @Test
    void testDelete() {
        CardApplicantDto dto = new CardApplicantDto();
        dto.setFirstName("ime");
        dto.setLastName("prezime");
        dto.setPin("12345678900");
        dto.setStatus(CardStatus.ACCEPTED);

        controller.save(dto);

        ResponseEntity resp = controller.delete(dto.getPin());
        assertEquals(HttpStatus.OK,resp.getStatusCode());

        resp = controller.getApplicant(dto.getPin());

        assertEquals(HttpStatus.NOT_FOUND,resp.getStatusCode());
    }

}
