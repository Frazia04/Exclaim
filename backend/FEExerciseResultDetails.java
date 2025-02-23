package de.rptu.cs.exclaim.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FEExerciseResultDetails {

    String groupId ;
    String teamId ;
    String userId ;
    String studentId;
    String firstName ;
    String lastName ;
    String maxPointsTotal;
    String grade ;
    String attendance ;
}
