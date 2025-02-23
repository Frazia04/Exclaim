package de.rptu.cs.exclaim.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
//import org.springframework.lang.Nullable;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FEUserAdminDetails {
    int userId;
    String username;
    String studentId;
    String firstname;
    String lastname;
    String email;
  //  @Nullable FELanguage language;
    String language;
    int isStudent;
    int isTutor;
    int isAssistant;
    boolean isAdmin;
}


    // Constructor that matches the order and types of selected fields
//    public FEUserAdminDetails(int userId,  String username, String studentId, String firstname, String lastname, String email, Object studentRolesCount, Object tutorRolesCount, Object assistantRolesCount, int isAdmin) {
//        this.userId = userId;
//        this.username = username;
//        this.studentId = studentId;
//        this.firstname = firstname;
//        this.lastname = lastname;
//        this.email = email;
//        this.isStudent = studentRolesCount;
//        this.isTutor = tutorRolesCount;
//        this.isAssistant = assistantRolesCount;
//        this.isAdmin = isAdmin;
    //}




