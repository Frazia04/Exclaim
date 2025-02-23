package de.rptu.cs.exclaim.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FEExerciseWithDetails {
    @JsonIgnore
    @Delegate(types = FEExercise.class)
    FEExercise exercise;

    List<FEExerciseGroup> groups;
}
