package de.rptu.cs.exclaim.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FELecture {
    String lectureId;
    String lectureName;
    FETerm term;
    Short year;


}


