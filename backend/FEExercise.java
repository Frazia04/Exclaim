package de.rptu.cs.exclaim.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FEExercise {
    public enum GroupJoin {
        @JsonProperty("none") NONE,
        @JsonProperty("group") GROUP,
        @JsonProperty("preferences") PREFERENCES
    }

    String exerciseId;
    String lecture;
    FETerm term;
    boolean registrationOpen;
    GroupJoin groupJoin;
    FEExerciseRoles roles;
}
