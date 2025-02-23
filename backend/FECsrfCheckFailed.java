package de.rptu.cs.exclaim.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FECsrfCheckFailed {
    public enum ErrorConstant {
        @JsonProperty("csrf") CSRF
    }

    public ErrorConstant getError() {
        return ErrorConstant.CSRF;
    }

    @Nullable String csrf;
}
