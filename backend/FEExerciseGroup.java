package de.rptu.cs.exclaim.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FEExerciseGroup {
    String groupId;
    List<FEUser> tutors;
}
