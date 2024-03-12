package rba.zadatak.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum CardStatus {
    PENDING("pending"),DELETED("deleted"),ACCEPTED("accepted"),OUTDATED("outdated");

    private String text;

    CardStatus(String text) {
        this.text=text;
    }

    public String getText() {
        return this.text;
    }

    @JsonCreator
    public static CardStatus forValue(String text) {
        if (text.isBlank()) return null;

        for (CardStatus cs : CardStatus.values()) {
            if (cs.getText().equals(text)) {
                return cs;
            }
        }

        String message=String.format("Valid values are %s",
                Arrays.stream(CardStatus.values()).map(it->it.toString()).collect(Collectors.joining(" | ")));
        throw new IllegalArgumentException(message);

    }
}

