package rba.zadatak.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import rba.zadatak.dto.CardApplicantDto;
import rba.zadatak.entity.CardApplicant;


/*
** Sucelje na temelju kojeg MapStruct automatski generira metodu za mapiranje entity->dto ili dto->entity
 */

@Mapper(componentModel = "spring")
public interface CardApplicantDtoMapper {
    CardApplicantDtoMapper MAPPER = Mappers.getMapper(CardApplicantDtoMapper.class);

    CardApplicantDto cardToCardDto(CardApplicant entity);

    // sprjecava autobinding ranjivost u kojoj HTTP zahtjev specificira status korisnika kartice iako bi status trebao
    // biti pod iskljucivom kontrolom aplikacije
    @Mapping(target="status", expression="java(rba.zadatak.enums.CardStatus.PENDING)")
    CardApplicant cardDtoToCard(CardApplicantDto dto);
}
