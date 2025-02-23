package de.rptu.cs.exclaim.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FEUser {
    int userId;
    String firstname;
    String lastname;
    String email;
}
