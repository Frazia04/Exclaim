package de.rptu.cs.exclaim.frontend.api;

import de.rptu.cs.exclaim.api.FEAddAssisstantRequest;
import de.rptu.cs.exclaim.api.FEAssistant;
import de.rptu.cs.exclaim.api.FEExercise;
import de.rptu.cs.exclaim.api.FEExerciseGroup;
import de.rptu.cs.exclaim.api.FEExerciseWithDetails;
import de.rptu.cs.exclaim.api.FELecture;
import de.rptu.cs.exclaim.api.FETerm;
import de.rptu.cs.exclaim.api.FEUser;
import de.rptu.cs.exclaim.controllers.ControllerUtils;
import de.rptu.cs.exclaim.controllers.NotFoundException;
import de.rptu.cs.exclaim.data.Assistant;
import de.rptu.cs.exclaim.data.interfaces.IUser;
import de.rptu.cs.exclaim.data.records.ExerciseRecord;
import de.rptu.cs.exclaim.schema.Keys;
import de.rptu.cs.exclaim.schema.enums.Term;
import de.rptu.cs.exclaim.schema.tables.Assistants;
import de.rptu.cs.exclaim.schema.tables.Exercises;
import de.rptu.cs.exclaim.schema.tables.Groups;
import de.rptu.cs.exclaim.schema.tables.Tutors;
import de.rptu.cs.exclaim.schema.tables.Users;
import de.rptu.cs.exclaim.security.AccessChecker;
import de.rptu.cs.exclaim.security.UserWithPermissions;
import de.rptu.cs.exclaim.utils.Comparators;
import jakarta.validation.Valid;
import de.rptu.cs.exclaim.data.records.AssistantRecord;
import org.jooq.Record1;
import org.jooq.Records;
import org.jooq.impl.DSL;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;
import de.rptu.cs.exclaim.monitoring.MetricsService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.rptu.cs.exclaim.controllers.ControllerUtils.addRedirectMessage;
import static de.rptu.cs.exclaim.schema.tables.Assistants.ASSISTANTS;
import static de.rptu.cs.exclaim.schema.tables.Exercises.EXERCISES;
import static de.rptu.cs.exclaim.schema.tables.Groups.GROUPS;
import static de.rptu.cs.exclaim.schema.tables.Tutors.TUTORS;
import static de.rptu.cs.exclaim.schema.tables.Users.USERS;

@RestController

public class LectureApiController {

    @Autowired
    private DSLContext ctx;
    @Autowired
    private AccessChecker accessChecker;



    @PostMapping("api/lectures/create")
    @ResponseBody
    public FELecture createLecture(@Valid @RequestBody FELecture request) {

        Optional<UserWithPermissions> userOpt = accessChecker.getUserWithPermissionsOpt();

        // If user is not authenticated, throw an error
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        short year = request.getYear();

        FETerm feTerm = request.getTerm();
        Term term;

        try {
            if (feTerm.getTerm() != null) {
                term = Term.valueOf(feTerm.getTerm().name());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Term cannot be null: " + feTerm);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid term value: " + feTerm, e);
        }

        String lectureName = request.getLectureName();
        String lectureID = request.getLectureId();

        try {
            ExerciseRecord exerciseRecord = ctx.newRecord(Exercises.EXERCISES);
            exerciseRecord.setExerciseId(lectureID);
            exerciseRecord.setLecture(lectureName);
            exerciseRecord.setYear(year);
            exerciseRecord.setTerm(term);
            exerciseRecord.insert();
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate lecture ID detected: " + e.getMessage());
        }

        return request;
    }

    @PostMapping("api/lectures/{exerciseId}/delete")
    @Transactional
    public ResponseEntity<String> deleteLecture(@PathVariable String exerciseId) {
//        Optional<UserWithPermissions> userOpt = accessChecker.getUserWithPermissionsOpt();
//
//        // If user is not authenticated, throw an error
//        if (userOpt.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
//        }
            if (ctx
                .deleteFrom(EXERCISES)
                .where(EXERCISES.ID.eq(exerciseId))
                .execute() == 1
            ) {
                return ResponseEntity.status(HttpStatus.OK).body("Lecture Deleted Successfully!");
            }
         else
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lecture Cannot be Deleted.");    }

    @PostMapping("api/lectures/{exerciseId}/edit")
    @ResponseBody
    public FELecture editLecture(@Valid @RequestBody FELecture request) {

        Optional<UserWithPermissions> userOpt = accessChecker.getUserWithPermissionsOpt();

        // If user is not authenticated, throw an error
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        short year = request.getYear();

        FETerm feTerm = request.getTerm();
        Term term;

        try {
            if (feTerm.getTerm() != null) {
                term = Term.valueOf(feTerm.getTerm().name());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Term cannot be null: " + feTerm);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid term value: " + feTerm, e);
        }

        String lectureName = request.getLectureName();
        String lectureID = request.getLectureId();

        ExerciseRecord exerciseRecord = ctx.fetchOptional(EXERCISES, EXERCISES.ID.eq(lectureID)).orElseThrow(NotFoundException::new);

        try {

            exerciseRecord.setLectureIfChanged(lectureName);
            exerciseRecord.setYearIfChanged(year);
            exerciseRecord.setTermIfChanged(term);
            exerciseRecord.update();
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate lecture ID detected: " + e.getMessage());
        }

        return request;
    }

    @GetMapping("api/lectures/{exerciseId}/assistants")
    @ResponseBody
    public List<FEAssistant> getAssistants(@PathVariable String exerciseId) {
//        metricsService.registerAccess();

        Assistants a = ASSISTANTS.as("a");
        Users u = a.user().as("u");
        List<IUser> assistants = ctx
            .select(u)
            .from(a)
            .where(a.USERID.eq(u.USERID), a.EXERCISEID.eq(exerciseId))
            .fetch(Record1::value1);
        assistants.sort(Comparators.USER_BY_NAME);

        return assistants.stream()
            .map(user -> {
                // Ensure safe handling of potentially null username
                String username = user.getUsername();
                if (username == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be null for user: " + user.getUserId());
                }
                return new FEAssistant(user.getUserId(), user.getFirstname(), user.getLastname(), username);
            })
            .collect(Collectors.toList());
    }

    @PostMapping("/api/lectures/{exerciseId}/assistants/{userId}/remove")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> removeAssistant(@PathVariable String exerciseId, @PathVariable int userId) {
        int deletedRows = ctx
            .deleteFrom(ASSISTANTS)
            .where(ASSISTANTS.USERID.eq(userId), ASSISTANTS.EXERCISEID.eq(exerciseId))
            .execute();

        if (deletedRows == 1) {
            return ResponseEntity.status(HttpStatus.OK).body("Assistant Removed Successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to delete assistant");
        }
    }

    @PostMapping("/api/lectures/{exerciseId}/assistants/add")
    @Transactional
    @ResponseBody
    public ResponseEntity<String> addAssistant(@PathVariable String exerciseId, @RequestBody FEAddAssisstantRequest request) {
        // Fetch the exercise record
        ExerciseRecord exerciseRecord = ctx.fetchOptional(EXERCISES, EXERCISES.ID.eq(exerciseId)).orElseThrow(NotFoundException::new);

        // Fetch the user ID of the assistant to be added
        Integer assistantUserId = ctx
            .select(USERS.USERID)
            .from(USERS)
            .where(USERS.USERNAME.eq(request.getUsername()))
            .fetchOne(Record1::value1);

        // Check if the user ID was found
        if (assistantUserId != null) {
            // Create a new assistant record
            AssistantRecord assistant = ctx.newRecord(ASSISTANTS);
            assistant.setExerciseId(exerciseRecord.getExerciseId());
            assistant.setUserId(assistantUserId);

            try {
                // Insert the assistant record
                assistant.insert();
                return ResponseEntity.status(HttpStatus.OK).body("Assistant added successfully");
            } catch (DuplicateKeyException e) {
                // Handle duplicate assistant role
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Assistant role already exists");
            }
        } else {
            // Handle the case where the username was not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Username not found");
        }
    }
}


