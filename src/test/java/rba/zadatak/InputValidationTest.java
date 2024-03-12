package rba.zadatak;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import rba.zadatak.dto.CardApplicantDto;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InputValidationTest {
    @Autowired
    Controller controller;

    TestRestTemplate restTemplate = new TestRestTemplate();

    @LocalServerPort
    public Integer port;

    @Test
    void rejectInvalidPin() {

        CardApplicantDto dto = new CardApplicantDto();
        dto.setFirstName("ime");
        dto.setLastName("prezime");
        dto.setPin("1234567890");


        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:" + port + "/api/save", dto, String.class);
        // OIB mora imati 11 znamenki
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());

    }

    @Test
    void rejectInvalidName() {
        CardApplicantDto dto = new CardApplicantDto();
        dto.setFirstName("ime'); DROP TABLE CardApplicant; --");
        dto.setLastName("prezime");
        dto.setPin("12345678900");

        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:" + port + "/api/save", dto, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

}
