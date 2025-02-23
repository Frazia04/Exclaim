package de.rptu.cs.exclaim.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FEExerciseSheetWithDetails {
    String sheetId;
    String exerciseId;
    String label;
    List<FEExerciseSheetAssignments> assignments;
    String points;
    String maxPointsTotal;
    String maxPointsGraded;
    String maxPointsUngraded;
    String achievedPoints;
    String totalAbsent;

}
