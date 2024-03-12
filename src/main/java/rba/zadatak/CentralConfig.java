package rba.zadatak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import rba.zadatak.deserializer.CardApplicantDtoDeserializer;
import rba.zadatak.dto.CardApplicantDto;
import rba.zadatak.entity.CardApplicant;
import rba.zadatak.repository.FileRepositoryFragment;
import rba.zadatak.repository.FileRepositoryFragmentImpl;


;

/*
** Singleton moze po potrebi sadrzavati konfiguracijske podatke za aplikaciju
 */

@Getter
@Configuration
public class CentralConfig {
    @Bean
    public FileRepositoryFragment<CardApplicant, String> fileRepositoryFragment() {
        return new FileRepositoryFragmentImpl<CardApplicant,String>(CardApplicant.class);
    }

    @Bean
    public ObjectMapper objectMapper(Validator validator) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule().addDeserializer(CardApplicantDto.class,
                new CardApplicantDtoDeserializer(validator)));
        return objectMapper;
    }

}
