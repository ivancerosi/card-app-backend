package rba.zadatak.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import rba.zadatak.dto.CardApplicantDto;
import rba.zadatak.exceptions.InvalidInputException;

import java.io.IOException;

public class CardApplicantDtoDeserializer extends StdDeserializer<CardApplicantDto> {
    Validator validator;

    public CardApplicantDtoDeserializer(Validator validator) {
        super(CardApplicantDto.class);
        this.validator=validator;
    }

    /*
    * Custom deserializer koji validira DTO
    * */
    @Override
    public CardApplicantDto deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        CardApplicantDto dto = new CardApplicantDto();

        ObjectCodec codec = jsonParser.getCodec();
        JsonNode tree = codec.readTree(jsonParser);

        dto.setFirstName(tree.get("firstName").textValue());
        dto.setLastName(tree.get("lastName").textValue());
        dto.setPin(tree.get("pin").textValue());


        Errors errors = validator.validateObject(dto);
        if (errors.hasErrors()) {
            throw new InvalidInputException(errors.getAllErrors());
        }

        return dto;
    }
}
