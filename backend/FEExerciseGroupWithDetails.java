package de.rptu.cs.exclaim.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;


@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FEExerciseGroupWithDetails {
    String exerciseId;
    String groupId;
    String day;
    String time;
    String location;
    Integer maxSize;
    int currentSize;
    List<FEUser> tutors;
    String tutorNames;

}
