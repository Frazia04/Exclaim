package de.rptu.cs.exclaim.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FEConfiguration {
    Set<String> samlRegistrationIds;
}
