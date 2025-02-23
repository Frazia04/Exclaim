package de.rptu.cs.exclaim.frontend.api;
//import de.rptu.cs.exclaim.ExclaimProperties;
import de.rptu.cs.exclaim.data.records.UserRecord;

import de.rptu.cs.exclaim.schema.tables.Assistants;
import de.rptu.cs.exclaim.schema.tables.Students;
import de.rptu.cs.exclaim.schema.tables.Tutors;
import de.rptu.cs.exclaim.schema.tables.Users;

import jakarta.validation.Valid;
import de.rptu.cs.exclaim.api.FEUserAdminDetails;
import de.rptu.cs.exclaim.controllers.NotFoundException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static de.rptu.cs.exclaim.ExclaimValidationProperties.EMAIL_LENGTH_MAX;
import static de.rptu.cs.exclaim.ExclaimValidationProperties.FIRSTNAME_LENGTH_MAX;
import static de.rptu.cs.exclaim.ExclaimValidationProperties.LASTNAME_LENGTH_MAX;
import static de.rptu.cs.exclaim.ExclaimValidationProperties.USERNAME_LENGTH_MAX;

import static de.rptu.cs.exclaim.schema.tables.Assistants.ASSISTANTS;
import static de.rptu.cs.exclaim.schema.tables.Students.STUDENTS;
import static de.rptu.cs.exclaim.schema.tables.Tutors.TUTORS;
import static de.rptu.cs.exclaim.schema.tables.Users.USERS;

@RestController
@Slf4j
public class UserAdminApiController {

    private final DSLContext ctx;

    public UserAdminApiController(DSLContext ctx) {
        this.ctx = ctx;
    }

    public static class EditUserForm {
        public EditUserForm(String username, String firstname, String lastname, @Nullable String studentId, String email, @Nullable String language, @Nullable String verified, @Nullable String admin) {
            this.username = username;
            this.firstname = firstname;
            this.lastname = lastname;
            this.studentId = studentId;
            this.email = email;
            this.language = language;
            //assert verified != null;
            this.verified = verified;
            //assert admin != null;
            this.admin = admin;
        }

        @NotNull /*@Pattern(regexp = USERNAME_REGEX)*/ @Size(/*min = USERNAME_LENGTH_MIN,*/ max = USERNAME_LENGTH_MAX) String username;
        @NotBlank @Size(max = FIRSTNAME_LENGTH_MAX) String firstname;
        @NotBlank @Size(max = LASTNAME_LENGTH_MAX) String lastname;
        @Nullable String studentId;
        @NotBlank @Email @Size(max = EMAIL_LENGTH_MAX) String email;
        @Nullable String language;
        @Nullable String verified;
        @Nullable String admin;
    }

    @GetMapping("api/users")
    @ResponseBody
    public List<FEUserAdminDetails> getUsers() {

        Assistants a = ASSISTANTS.as("a");
        Students s = STUDENTS.as("s");
        Users u = USERS.as("u");
        Tutors t = TUTORS.as("t");
        //List<IUser> users = ctx
        return ctx
            //.select
            .select(
                u.USERID,
                u.USERNAME,
                u.STUDENTID,
                u.FIRSTNAME,
                u.LASTNAME,
                u.EMAIL,
                u.LANGUAGE,
                // admin-only:

                DSL.count(s.EXERCISEID).filterWhere(s.USERID.isNotNull()).as("studentRolesCount"),
                DSL.count(t.EXERCISEID).filterWhere(t.USERID.isNotNull()).as("tutorRolesCount"),
                DSL.count(a.EXERCISEID).filterWhere(a.USERID.isNotNull()).as("assistantRolesCount"),
                u.ADMIN.convertFrom(admin -> admin ? 1 : 0)
            )
            .from(u)
            .leftJoin(s).on(u.USERID.eq(s.USERID))
            .leftJoin(t).on(u.USERID.eq(t.USERID))
            .leftJoin(a).on(u.USERID.eq(a.USERID))
            .groupBy(u.USERID, u.USERNAME, u.STUDENTID, u.FIRSTNAME, u.LASTNAME, u.EMAIL, u.LANGUAGE, u.ADMIN)


            .fetch(r -> new FEUserAdminDetails(
                r.value1(),
                r.value2(),
                r.value3(),
                r.value4(),
                r.value5(),
                r.value6(),
                r.value7(),
                r.value8(),
                r.value9() ,
                r.value10(),
                r.value11() != null && r.value11().equals(1)));


    }


    @PostMapping("api/users/{userId}")
    @PreAuthorize("@accessChecker.isAdmin()")
    @Transactional
    public String editUser(@PathVariable int userId, @Valid UserAdminApiController.EditUserForm editUserForm) {
        //metricsService.registerAccess();
        UserRecord userRecord = ctx
            .selectFrom(USERS)
            .where(USERS.USERID.eq(userId))
            .forUpdate()
            .fetchOptional()
            .orElseThrow(NotFoundException::new);

        // Convert empty String to null
        //String username = StringUtils.defaultIfEmpty(editUserForm.username, null);
        String studentId = StringUtils.defaultIfEmpty(editUserForm.studentId, null);




//        if (studentId != null && !studentId.equals(userRecord.getStudentId())
//            && !exclaimProperties.getValidation().getStudentIdRegex().matcher(studentId).matches()
//        ) {
//            bindingResult.rejectValue("studentId", "StudentId");
//        }
//        if (language != null && !language.equals(userRecord.getLanguage())
//            && !msg.getSupportedLanguages().containsKey(language)
//        ) {
//            bindingResult.rejectValue("language", "Invalid");
//        }
//
//        if (bindingResult.hasErrors()) {
//            redirectBindingResult(editUserForm, bindingResult, redirectAttributes);
//            addRedirectMessage(ControllerUtils.MessageType.ERROR, msg.getMessage("common.invalid-form-not-saved"), redirectAttributes);
//        } else {
        try {
            userRecord.setUsernameIfChanged(editUserForm.username);
            userRecord.setFirstnameIfChanged(editUserForm.firstname);
            userRecord.setLastnameIfChanged(editUserForm.lastname);
            userRecord.setStudentIdIfChanged(studentId);
            userRecord.setEmailIfChanged(editUserForm.email);

            userRecord.setAdminIfChanged(Boolean.valueOf(editUserForm.admin));

            if (userRecord.changed()) {
                userRecord.update();
                return "true";
            }
            // userRecord.update();
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: " + e.getMessage());
        }

//            if (userRecord.changed()) {
//                // Get original values for logging, since .original() is set to new values after successful .update()
//                UserRecord originalUserRecord = userRecord.original();
//
//                try {
//                    userRecord.update();
//                    log.info("User data for user {} has been changed to {} (by admin {})", originalUserRecord, userRecord, accessChecker.getUser());
//                    addRedirectMessage(ControllerUtils.MessageType.SUCCESS, msg.getMessage("common.saved"), redirectAttributes);
//                } catch (DuplicateKeyException e) {
//                    if (!Objects.equals(editUserForm.username, originalUserRecord.getUsername())
//                        && ctx.selectOne().from(USERS).where(USERS.USERNAME.eq(editUserForm.username)).fetchOne() != null
//                    ) {
//                        bindingResult.rejectValue("username", "Unique");
//                    }
//                    if (studentId != null && !studentId.equals(originalUserRecord.getStudentId())
//                        && ctx.selectOne().from(USERS).where(USERS.STUDENTID.eq(studentId)).fetchOne() != null
//                    ) {
//                        bindingResult.rejectValue("studentId", "Unique");
//                    }
//
//                    redirectBindingResult(editUserForm, bindingResult, redirectAttributes);
//                    if (bindingResult.hasErrors()) {
//                        addRedirectMessage(ControllerUtils.MessageType.ERROR, msg.getMessage("common.invalid-form-not-saved"), redirectAttributes);
//                    } else {
//                        // Unexpected, just show the exception message to the admin user.
//                        addRedirectMessage(ControllerUtils.MessageType.ERROR, e.toString(), redirectAttributes);
//                    }
//                }
//            }
//        }
//        return "redirect:/user/{userId}";
        //  }

        return "true";
    }

    @PostMapping("api/users/{userId}/delete")
    @PreAuthorize("@accessChecker.isAdmin()")
    @Transactional
    public String deleteUser(@PathVariable int userId) {
        //metricsService.registerAccess();
        UserRecord userRecord = ctx
            .selectFrom(USERS)
            .where(USERS.USERID.eq(userId))
            .forUpdate()
            .fetchOptional()
            .orElseThrow(NotFoundException::new);
        try {
            userRecord.delete();
            return "true";
            //log.info("User {} has been deleted by admin {}", userRecord, accessChecker.getUser());
        }catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: " + e.getMessage());

        }

    }

}










