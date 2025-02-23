package de.rptu.cs.exclaim.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FETerm {


    public enum SummerWinter {
        @JsonProperty("summer") SUMMER,
        @JsonProperty("winter") WINTER
    }

    short year;
    @Nullable SummerWinter term;
    String comment;

    @JsonCreator
    public static FETerm fromString(String value) {
        SummerWinter summerWinter;
        switch (value) {
            case "SUMMER":
                summerWinter = SummerWinter.SUMMER;
                break;
            case "WINTER":
                summerWinter = SummerWinter.WINTER;
                break;
            default:
                throw new IllegalArgumentException("Invalid value for SummerWinter enum: " + value);
        }
        return new FETerm((short) 0, summerWinter, "");
    }
}
