package de.rptu.cs.exclaim.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FEAccountData {
    int userId;
    String firstname;
    String lastname;
    String email;
    @Nullable String username;
    @Nullable String studentId;
    @Nullable FELanguage language;
    boolean isTutor;
    boolean isAssistant;
    boolean isAdmin;
}
