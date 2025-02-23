package de.rptu.cs.exclaim.frontend.api;

import de.rptu.cs.exclaim.api.FEExercise;
import de.rptu.cs.exclaim.api.FEExerciseGroup;
import de.rptu.cs.exclaim.api.FEExerciseGroupWithDetails;
import de.rptu.cs.exclaim.api.FEExerciseResultDetails;
import de.rptu.cs.exclaim.api.FEExerciseRoles;
import de.rptu.cs.exclaim.api.FEExerciseSheetAssignments;
import de.rptu.cs.exclaim.api.FEExerciseSheetWithDetails;
import de.rptu.cs.exclaim.api.FEExerciseWithDetails;
import de.rptu.cs.exclaim.api.FEProcessResult;
import de.rptu.cs.exclaim.api.FEStudentData;
import de.rptu.cs.exclaim.api.FETerm;
import de.rptu.cs.exclaim.api.FEUser;
import de.rptu.cs.exclaim.controllers.ControllerUtils;
import de.rptu.cs.exclaim.controllers.ExerciseAdminController;
import de.rptu.cs.exclaim.data.GroupAndTeam;
import de.rptu.cs.exclaim.data.NonNullGroupAndTeam;
import de.rptu.cs.exclaim.data.GroupWithCurrentSizeAndTutors;
import de.rptu.cs.exclaim.data.SheetWithMaxPoints;
import de.rptu.cs.exclaim.data.SheetWithResult;
import de.rptu.cs.exclaim.data.StudentWithSheetResults;
import de.rptu.cs.exclaim.data.interfaces.IGroup;
import de.rptu.cs.exclaim.data.interfaces.ISheet;
import de.rptu.cs.exclaim.data.interfaces.IStudent;
import de.rptu.cs.exclaim.data.interfaces.IUser;
import de.rptu.cs.exclaim.data.records.AssignmentRecord;
import de.rptu.cs.exclaim.data.records.ExerciseRecord;
import de.rptu.cs.exclaim.data.records.GroupRecord;
import de.rptu.cs.exclaim.data.records.SheetRecord;
import de.rptu.cs.exclaim.data.records.TutorRecord;
import de.rptu.cs.exclaim.monitoring.MetricsService;
import de.rptu.cs.exclaim.schema.Keys;
import de.rptu.cs.exclaim.schema.enums.Attendance;
import de.rptu.cs.exclaim.schema.enums.GroupJoin;
import de.rptu.cs.exclaim.schema.enums.Term;
import de.rptu.cs.exclaim.schema.enums.Weekday;
import de.rptu.cs.exclaim.schema.tables.Assignments;
import de.rptu.cs.exclaim.schema.tables.Assistants;
import de.rptu.cs.exclaim.schema.tables.Exercises;
import de.rptu.cs.exclaim.schema.tables.Groups;
import de.rptu.cs.exclaim.schema.tables.Sheets;
import de.rptu.cs.exclaim.schema.tables.Studentresults;
import de.rptu.cs.exclaim.schema.tables.Students;
import de.rptu.cs.exclaim.schema.tables.Teamresults;
import de.rptu.cs.exclaim.schema.tables.TeamresultsAssignment;
import de.rptu.cs.exclaim.schema.tables.Tutors;
import de.rptu.cs.exclaim.schema.tables.Unread;
import de.rptu.cs.exclaim.schema.tables.Uploads;
import de.rptu.cs.exclaim.schema.tables.Users;
import de.rptu.cs.exclaim.security.AccessChecker;
import de.rptu.cs.exclaim.security.ExerciseRoles;
import de.rptu.cs.exclaim.utils.Comparators;
import de.rptu.cs.exclaim.utils.UploadManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.IOUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record13;
import org.jooq.Records;
import org.jooq.ResultQuery;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static de.rptu.cs.exclaim.ExclaimValidationProperties.ASSIGNMENT_ID_LENGTH_MAX;
import static de.rptu.cs.exclaim.ExclaimValidationProperties.ASSIGNMENT_LABEL_LENGTH_MAX;
import static de.rptu.cs.exclaim.ExclaimValidationProperties.GROUP_LOCATION_LENGTH_MAX;
import static de.rptu.cs.exclaim.ExclaimValidationProperties.GROUP_TIME_LENGTH_MAX;
import org.jooq.Record7;

import static de.rptu.cs.exclaim.ExclaimValidationProperties.ID_REGEX;
import static de.rptu.cs.exclaim.ExclaimValidationProperties.SHEET_ID_LENGTH_MAX;
import static de.rptu.cs.exclaim.ExclaimValidationProperties.SHEET_LABEL_LENGTH_MAX;
import static de.rptu.cs.exclaim.controllers.ExerciseAdminController.parseMaxPoints;
import static de.rptu.cs.exclaim.controllers.ExerciseAdminController.parseMaxSize;
import static de.rptu.cs.exclaim.frontend.FrontendData.mapGroupJoin;
import static de.rptu.cs.exclaim.frontend.FrontendData.mapTerm;
import static de.rptu.cs.exclaim.schema.tables.Assignments.ASSIGNMENTS;
import static de.rptu.cs.exclaim.schema.tables.Assistants.ASSISTANTS;
import static de.rptu.cs.exclaim.schema.tables.Exercises.EXERCISES;
import static de.rptu.cs.exclaim.schema.tables.Groups.GROUPS;
import static de.rptu.cs.exclaim.schema.tables.Sheets.SHEETS;
import static de.rptu.cs.exclaim.schema.tables.Studentresults.STUDENTRESULTS;
import static de.rptu.cs.exclaim.schema.tables.Students.STUDENTS;
import static de.rptu.cs.exclaim.schema.tables.Teamresults.TEAMRESULTS;
import static de.rptu.cs.exclaim.schema.tables.TeamresultsAssignment.TEAMRESULTS_ASSIGNMENT;
import static de.rptu.cs.exclaim.schema.tables.Tutors.TUTORS;
import static de.rptu.cs.exclaim.schema.tables.Uploads.UPLOADS;
import static de.rptu.cs.exclaim.schema.tables.Users.USERS;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequiredArgsConstructor

public class ExerciseApiController {


    private static final String EXERCISE_PATH = "/api/exercise";
    private static final String EXERCISE_SHEET_PATH = "/api/exerciseSheet";
    private static final Logger log = LoggerFactory.getLogger(ExerciseApiController.class);

    private final AccessChecker accessChecker;
    private final DSLContext ctx;
    private final MetricsService metricsService;
    private final UploadManager uploadManager;

//    @ModelAttribute
//    public ExerciseRoles exerciseRoles(@PathVariable String exerciseId) {
//        ExerciseRoles exerciseRoles = accessChecker.getExerciseRoles(exerciseId);
//        log.debug("Accessing exercise {} with {}", exerciseId, exerciseRoles);
//        return exerciseRoles;
//    }

    @Value
    public static class CreateSheetForm {
        @NotBlank @Size(max = SHEET_ID_LENGTH_MAX) @Pattern(regexp = ID_REGEX) String sheetId;
        @NotBlank @Size(max = SHEET_LABEL_LENGTH_MAX) String label;
    }

    @Value
    public static class EditSheetForm {
        @NotBlank @Size(max = SHEET_LABEL_LENGTH_MAX) String label;
    }

    @Value
    public static class CreateGroupForm {
        @NotBlank @Size(max = SHEET_ID_LENGTH_MAX) @Pattern(regexp = ID_REGEX) String groupId;
        @NotBlank @Size(max = SHEET_LABEL_LENGTH_MAX) String day;
        @NotBlank @Size(max = SHEET_LABEL_LENGTH_MAX) String time;
        @NotBlank @Size(max = SHEET_LABEL_LENGTH_MAX) String location;
        @NotBlank @Size(max = SHEET_LABEL_LENGTH_MAX) String maxSize;
    }

    @Value
    public static class EditGroupForm {
        @Nullable Weekday day;
        @Nullable @Size(max = GROUP_TIME_LENGTH_MAX) String time;
        @Nullable @Size(max = GROUP_LOCATION_LENGTH_MAX) String location;
        @Nullable String maxSize;
    }

    @Value
    public static class CreateAssignmentForm {
        public CreateAssignmentForm(String assignmentId, String label, @Nullable String maxPoints, @Nullable Boolean showStatistics) {
            this.assignmentId = assignmentId;
            this.label = label;
            this.maxPoints = maxPoints;
            this.showStatistics = showStatistics != null && showStatistics;
        }

        @NotBlank @Size(max = ASSIGNMENT_ID_LENGTH_MAX) @Pattern(regexp = ID_REGEX) String assignmentId;
        @NotBlank @Size(max = ASSIGNMENT_LABEL_LENGTH_MAX) String label;
        @Nullable String maxPoints;
        boolean showStatistics;
    }

    @Value
    public static class EditAssignmentForm {
        public EditAssignmentForm(String label, @Nullable String maxPoints, @Nullable Boolean showStatistics) {
            this.label = label;
            this.maxPoints = maxPoints;
            this.showStatistics = showStatistics != null && showStatistics;
        }

        @NotBlank @Size(max = ASSIGNMENT_LABEL_LENGTH_MAX) String label;
        @Nullable String maxPoints;
        boolean showStatistics;
    }

    @GetMapping(EXERCISE_PATH)
    @ResponseBody
    public List<FEExercise> getExercises() {
        IUser user = accessChecker.getUser();
        return queryExercise(null, user)
            .fetch(Records.mapping(ExerciseApiController::mapExercise));
    }

    private ResultQuery<Record13<String, String, Short, Term, String, Boolean, GroupJoin, Boolean, String, String, Boolean, String[], Boolean>> queryExercise(@Nullable String exerciseId, IUser user) {
        int userId = user.getUserId();
        Exercises e = EXERCISES.as("e");
        Students s = STUDENTS.as("s");
        Tutors t = TUTORS.as("t");
        Assistants a = ASSISTANTS.as("a");
        return ctx
            .select(
                e.ID, e.LECTURE, e.YEAR, e.TERM, e.TERM_COMMENT, e.REGISTRATION_OPEN, e.GROUP_JOIN,
                s.USERID.isNotNull().as("is_student"), s.GROUPID.as("student_group"), s.TEAMID.as("student_team"),
                t.USERID.isNotNull().as("is_tutor"), DSL.arrayAgg(t.GROUPID).as("tutor_groups"),
                a.USERID.isNotNull().as("is_assistant")
            )
            .from(e)
            .leftJoin(s).on(s.USERID.eq(userId), s.EXERCISEID.eq(e.ID))
            .leftJoin(t).on(t.USERID.eq(userId), t.EXERCISEID.eq(e.ID))
            .leftJoin(a).on(a.USERID.eq(userId), a.EXERCISEID.eq(e.ID))
            .where(
                // Filter for the provided exercise id, if any.
                exerciseId == null ? DSL.noCondition() : e.ID.eq(exerciseId),
                // Admins can access all exercises, for other users filter exercises...
                user.getAdmin() ? DSL.noCondition() : DSL.or(
                    // ...that the user has already joined, or...
                    s.USERID.isNotNull(), t.USERID.isNotNull(), a.USERID.isNotNull(),
                    // ...that the user could join.
                    DSL.condition(e.REGISTRATION_OPEN)
                )
            )
            .groupBy(e, s, t, a);
    }

    private static FEExercise mapExercise(
        String exerciseId, String lecture, short year, @Nullable Term term, String termComment, boolean registrationOpen, de.rptu.cs.exclaim.schema.enums.GroupJoin groupJoin,
        boolean isStudent, @Nullable String groupId, @Nullable String teamId,
        boolean isTutor, String[] tutorGroups,
        boolean isAssistant
    ) {
        return new FEExercise(
            exerciseId,
            lecture,
            new FETerm(year, mapTerm(term), termComment),
            registrationOpen,
            mapGroupJoin(groupJoin),
            new FEExerciseRoles(
                isStudent ? new FEStudentData(groupId, teamId) : null,
                isTutor ? List.of(tutorGroups) : Collections.emptyList(),
                isAssistant
            )
        );
    }

    @GetMapping(EXERCISE_PATH + "/{exerciseId}")
    @ResponseBody
    public Optional<FEExerciseWithDetails> getExercise(@PathVariable String exerciseId) {
        IUser user = accessChecker.getUser();
        return queryExercise(exerciseId, user)
            .fetchOptional(Records.mapping(ExerciseApiController::mapExercise))
            .map(this::fetchExerciseDetails);
    }


    private FEExerciseWithDetails fetchExerciseDetails(FEExercise exercise) {
        Groups g = GROUPS.as("g");
        Tutors t = TUTORS.as("t");
        Users tu = t.user().as("tu");
        return new FEExerciseWithDetails(
            exercise,
            ctx
                .select(
                    g.GROUPID,
                    DSL.multisetAgg(tu.USERID, tu.FIRSTNAME, tu.LASTNAME, tu.EMAIL)
                        .convertFrom(tutorsResult -> tutorsResult.stream()
                            .filter(tutorRecord -> tutorRecord.value1() != null)
                            .map(Records.mapping(FEUser::new))
                            .toList()
                        )
                )
                .from(g)
                .leftJoin(t).onKey(Keys.FK__TUTORS__GROUPS)
                .where(g.EXERCISEID.eq(exercise.getExerciseId()))
                .groupBy(g)
                .fetch(Records.mapping(FEExerciseGroup::new))
        );
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/join")
    @ResponseBody
    public FEExerciseWithDetails joinExercise(@PathVariable String exerciseId) {
        IUser user = accessChecker.getUser();
        FEExercise exercise = queryExercise(exerciseId, user)
            .fetchOptional(Records.mapping(ExerciseApiController::mapExercise))
            .orElseThrow(NotFoundException::new);

        // Only join if not already joined
        if (exercise.getRoles().getStudent() == null) {
            // Check that joining is allowed
            if (!exercise.getRegistrationOpen()) {
                throw new ForbiddenException();
            }

            // Perform join in database
            ctx
                .insertInto(STUDENTS)
                .columns(STUDENTS.EXERCISEID, STUDENTS.USERID)
                .values(exerciseId, user.getUserId())
                .execute();

            // Perform join in result
            exercise.getRoles().setStudent(new FEStudentData(null, null));
        }

        return fetchExerciseDetails(exercise);
    }

    @GetMapping(EXERCISE_SHEET_PATH + "/{exerciseId}")
    @ResponseBody
    public List<FEExerciseSheetWithDetails> getExerciseSheets(@PathVariable String exerciseId) {
        IUser user = accessChecker.getUser();
        Sheets s = SHEETS.as("s");
        Assignments a = ASSIGNMENTS.as("a");
        Studentresults sr = STUDENTRESULTS.as("sr");
        Teamresults tr = TEAMRESULTS.as("tr");
        TeamresultsAssignment tra = TEAMRESULTS_ASSIGNMENT.as("tra");
        Unread u = Unread.UNREAD.as("u");
        Uploads upl = u.upload().as("upl");
        List<SheetWithResult> sheets = ctx
            .select(
                s,
                DSL.sum(a.MAXPOINTS).as("maxpoints"),
                DSL.if_(
                    // Do not consider any points when hidden
                    DSL.condition(DSL.ifnull(tr.HIDEPOINTS, false)), DSL.val((BigDecimal) null),
                    // Sum of team and delta points. If everything is null, then null.
                    DSL.coalesce(DSL.sum(tra.POINTS).plus(DSL.ifnull(sr.DELTAPOINTS, BigDecimal.ZERO)), sr.DELTAPOINTS)
                ).as("points"),
                sr.ATTENDED,
                DSL.field(DSL.and(
                    DSL.not(DSL.condition(DSL.ifnull(tr.HIDECOMMENTS, false))),
                    DSL.exists(DSL
                        .selectOne()
                        .from(u)
                        .where(upl.EXERCISE.eq(s.EXERCISE), upl.SHEET.eq(s.ID), u.USERID.eq(user.getUserId()))
                    )
                )).as("unread")
            )
            .from(s)
            .leftJoin(a).onKey(Keys.FK__ASSIGNMENTS__SHEETS)
            .leftJoin(sr).onKey(Keys.FK__STUDENTRESULTS__SHEETS).and(sr.USERID.eq(user.getUserId()))
            .leftJoin(tr).onKey(Keys.FK__TEAMRESULTS__SHEETS).and(tr.GROUPID.eq(sr.GROUPID)).and(tr.TEAMID.eq(sr.TEAMID))
            .leftJoin(tra).onKey(Keys.FK__TEAMRESULTS_ASSIGNMENT__TEAMRESULTS).and(tra.ASSIGNMENT.eq(a.ID))
            .where(s.EXERCISE.eq(exerciseId))
            .groupBy(s)
            .fetch(Records.mapping(SheetWithResult::new));
        sheets.sort(Comparator.comparing(SheetWithResult::getSheetId, Comparators.IDENTIFIER));
        BigDecimal maxPointsTotal = BigDecimal.ZERO;
        BigDecimal maxPointsGraded = BigDecimal.ZERO;
        BigDecimal achievedPoints = BigDecimal.ZERO;
        int totalAbsent = 0;
        for (SheetWithResult sheet : sheets) {
            BigDecimal maxPoints = sheet.getMaxPoints();
            BigDecimal points = sheet.getPoints();
            maxPointsTotal = maxPointsTotal.add(maxPoints);
            if (points != null) {
                maxPointsGraded = maxPointsGraded.add(maxPoints);
                achievedPoints = achievedPoints.add(points);
            }
            if (sheet.getAttended() == Attendance.ABSENT) {
                totalAbsent++;
            }
        }

        List<FEExerciseSheetWithDetails> sheetWithDetailsList = new ArrayList<>();
        for (SheetWithResult sheet : sheets) {
            FEExerciseSheetWithDetails sheetWithDetails = new FEExerciseSheetWithDetails(sheet.getSheetId(),
                sheet.getExerciseId(), sheet.getLabel(), new ArrayList<FEExerciseSheetAssignments>(),sheet.getPoints()!=null ?
                sheet.getPoints().toString() : "0", maxPointsTotal.toString(),
                maxPointsGraded.toString(), maxPointsTotal.subtract(maxPointsGraded).toString(),
                achievedPoints.toString(), String.valueOf(totalAbsent));
            sheetWithDetailsList.add(sheetWithDetails);
        }
        return sheetWithDetailsList;
    }

    @PostMapping(EXERCISE_SHEET_PATH + "/{exerciseId}/admin/sheets/create")
    @ResponseBody
    public FEProcessResult saveExerciseSheet(@PathVariable String exerciseId, @Valid CreateSheetForm createSheetForm) {
        metricsService.registerAccess();
        try {
            SheetRecord sheetRecord = ctx.newRecord(SHEETS);
            sheetRecord.setExerciseId(exerciseId);
            sheetRecord.setSheetId(createSheetForm.getSheetId());
            sheetRecord.setLabel(createSheetForm.getLabel());

            sheetRecord.insert();
            return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
        } catch (Exception e) {
            System.out.println("saveExerciseSheet exeption" + e);
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
        }

    }

    @GetMapping(EXERCISE_PATH + "/{exerciseId}/groups")
    @ResponseBody
    public List<FEExerciseGroupWithDetails> getExerciseGroups(@PathVariable String exerciseId) {
        metricsService.registerAccess();

        Groups g = GROUPS.as("g");
        Students s = STUDENTS.as("s");
        Tutors t = TUTORS.as("t");
        Users u = t.user().as("u");
        Field<Integer> currentSize = DSL.field(DSL
            .select(DSL.count(s.USERID))
            .from(s)
            .where(s.EXERCISEID.eq(g.EXERCISEID), s.GROUPID.eq(g.GROUPID))
        ).as("currentSize");
        Field<List<IUser>> tutors = DSL
            .multisetAgg(u)
            .orderBy(u.LASTNAME, u.FIRSTNAME)
            .as("tutors")
            .convertFrom(result ->
                // no tutors -> a single record with all null values is produced by multisetAgg
                (result.size() == 1 && result.get(0).value1().getUserId() == null)
                    ? Collections.emptyList()
                    : result.map(Record1::value1)
            );
        List<GroupWithCurrentSizeAndTutors> groups = ctx
            .select(g, currentSize, tutors)
            .from(g)
            .leftJoin(t).onKey(Keys.FK__TUTORS__GROUPS)
            .where(g.EXERCISEID.eq(exerciseId))
            .groupBy(g)
            .fetch(Records.mapping(GroupWithCurrentSizeAndTutors::new));
        groups.sort(Comparator.comparing(IGroup::getGroupId, Comparators.IDENTIFIER));
        return mapGroupData(groups);
    }

    private List<FEExerciseGroupWithDetails> mapGroupData(List<GroupWithCurrentSizeAndTutors> groups) {
        List<FEExerciseGroupWithDetails> groupsDetails = new ArrayList<>();

        if (groups != null && !groups.isEmpty()) {
            for (GroupWithCurrentSizeAndTutors group : groups) {

                List<FEUser> userTutors = new ArrayList<>();
                AtomicReference<String> tutorNames = new AtomicReference<>(" ");
                List<? extends IUser> tutors = group.getTutors();
                if (tutors != null && !tutors.isEmpty()) {
                    tutors.forEach(t -> {
                        userTutors.add(new FEUser(t.getUserId(), t.getFirstname(), t.getLastname(),
                            t.getEmail()));
                        tutorNames.set(tutorNames + " ," + t.getFirstname() + " " + t.getLastname());
                    });
                }

                FEExerciseGroupWithDetails detail = new FEExerciseGroupWithDetails
                    (group.getExerciseId(), group.getGroupId(),
                        group.getDay() != null ? group.getDay().getLiteral() : Weekday.MONDAY.getLiteral(),
                        group.getTime(), group.getLocation(), group.getMaxSize()!=null ? group.getMaxSize() : 0,
                        group.getCurrentSize(), userTutors, tutorNames.get()!=null ? tutorNames.get() : "");

                groupsDetails.add(detail);
            }
        }
        return groupsDetails;
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/admin/groups/create")
    @ResponseBody
    public FEProcessResult saveNewGroup(@PathVariable String exerciseId, @Valid CreateGroupForm createGroupForm) {
        metricsService.registerAccess();
        Integer maxSize;
        try {
            maxSize = parseMaxSize(createGroupForm.maxSize);
        } catch (NumberFormatException e) {
            maxSize = 0;
            System.out.println(e.getMessage());
        }

        try {
            GroupRecord groupRecord = ctx.newRecord(GROUPS);
            groupRecord.setExerciseId(exerciseId);
            groupRecord.setGroupId(createGroupForm.groupId);
            groupRecord.setDay(Weekday.valueOf(createGroupForm.day));
            groupRecord.setTime(StringUtils.defaultString(createGroupForm.time));
            groupRecord.setLocation(StringUtils.defaultString(createGroupForm.location));
            groupRecord.setMaxSize(maxSize);

            groupRecord.insert();
            return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
        } catch (DuplicateKeyException e) {
            System.out.println(e.getMessage());
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
        }
    }


    @GetMapping(EXERCISE_PATH + "/{exerciseId}/sheet/{sheetId}/zip")
    @ResponseBody
    public ZipOutputStream downloadSheetFileZip(@PathVariable String exerciseId, @PathVariable String sheetId, ExerciseRoles exerciseRoles,
                                                HttpServletResponse response) throws IOException {
        metricsService.registerAccess();

        response.setContentType("application/zip");
        response.setStatus(HttpServletResponse.SC_OK);
        String zipFilename = exerciseId + "_" + sheetId + ".zip";
        response.addHeader("Content-Disposition", "attachment; filename=\"" + zipFilename + "\"");

        // groupId -> teamId -> assignmentId -> [files]
        Map<String, Map<String, Map<String, List<Tuple2<LocalDateTime, String>>>>> uploads = new HashMap<>();
        ctx
            .select(UPLOADS.GROUPID, UPLOADS.TEAMID, UPLOADS.ASSIGNMENT, UPLOADS.UPLOAD_DATE, UPLOADS.FILENAME)
            .from(UPLOADS)
            .where(
                UPLOADS.EXERCISE.eq(exerciseId),
                UPLOADS.SHEET.eq(sheetId),
                exerciseRoles.applyGroupIdRestriction(UPLOADS.GROUPID),
                UPLOADS.DELETE_DATE.isNull()
            )
            .forEach(r -> uploads
                .computeIfAbsent(r.value1(), groupId -> new HashMap<>())
                .computeIfAbsent(r.value2(), teamId -> new HashMap<>())
                .computeIfAbsent(r.value3(), assignmentId -> new ArrayList<>())
                .add(new Tuple2<>(r.value4(), r.value5()))
            );

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()))) {
            for (Map.Entry<String, Map<String, Map<String, List<Tuple2<LocalDateTime, String>>>>> group : uploads.entrySet()) {
                String groupId = group.getKey();
                String groupFolder = groupId + "/";
                zipOutputStream.putNextEntry(new ZipEntry(groupFolder));
                for (Map.Entry<String, Map<String, List<Tuple2<LocalDateTime, String>>>> team : group.getValue().entrySet()) {
                    String teamId = team.getKey();
                    String teamFolder = groupFolder + teamId + "/";
                    zipOutputStream.putNextEntry(new ZipEntry(teamFolder));
                    for (Map.Entry<String, List<Tuple2<LocalDateTime, String>>> assignment : team.getValue().entrySet()) {
                        String assignmentId = assignment.getKey();
                        String assignmentFolder = teamFolder + assignmentId + "/";
                        zipOutputStream.putNextEntry(new ZipEntry(assignmentFolder));
                        Set<String> existingEntries = new HashSet<>();
                        for (Tuple2<LocalDateTime, String> file : assignment.getValue()) {
                            LocalDateTime uploadDate = file.v1;
                            String filename = file.v2;
                            if (existingEntries.add(filename)) {
                                zipOutputStream.putNextEntry(new ZipEntry(assignmentFolder + filename));
                                Path filePath = uploadManager.getUploadPath(exerciseId, sheetId, assignmentId, groupId, teamId, uploadDate, filename);
                                try (InputStream fileInputStream = Files.newInputStream(filePath)) {
                                    IOUtils.copy(fileInputStream, zipOutputStream);
                                }
                                zipOutputStream.closeEntry();
                            }
                        }
                        return zipOutputStream;
                    }
                }
            }
        }
        return new ZipOutputStream(null);
    }

    @GetMapping(EXERCISE_PATH + "/{exerciseId}/sheets/{sheetId}/delete")
    @ResponseBody
    public FEProcessResult deleteSheet(@PathVariable String exerciseId, @PathVariable String sheetId) {
        metricsService.registerAccess();
        try {
            if (ctx
                .deleteFrom(SHEETS)
                .where(SHEETS.EXERCISE.eq(exerciseId), SHEETS.ID.eq(sheetId))
                .execute() == 1
            ) {
                return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
            }
        } catch (DataIntegrityViolationException e) {
            System.out.println(e.getMessage());
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
        }
        return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), "ERROR");
    }

    @GetMapping(EXERCISE_PATH + "/{exerciseId}/sheets/{sheetId}/info")
    @ResponseBody
    public FEExerciseSheetWithDetails getSheetDataWithAssignments(@PathVariable String exerciseId, @PathVariable String sheetId) {
        metricsService.registerAccess();
        try {

            SheetRecord sheetRecord = ctx
                .fetchOptional(SHEETS, SHEETS.EXERCISE.eq(exerciseId), SHEETS.ID.eq(sheetId))
                .orElseThrow(de.rptu.cs.exclaim.controllers.NotFoundException::new);
            if (sheetRecord != null && sheetRecord.getSheetId() != null) {

                List<FEExerciseSheetAssignments> assignmentList = new ArrayList<>();

                List<AssignmentRecord> assignmentRecordList =
                    ctx.fetch(ASSIGNMENTS, ASSIGNMENTS.EXERCISE.eq(exerciseId), ASSIGNMENTS.SHEET.eq(sheetId));

                if (assignmentRecordList != null && !assignmentRecordList.isEmpty()) {
                    for (AssignmentRecord record : assignmentRecordList) {
                        assignmentList.add(new FEExerciseSheetAssignments
                            (record.getAssignmentId(), record.getLabel(), record.getMaxpoints().toString(),
                                record.getShowStatistics()));
                    }
                }

                FEExerciseSheetWithDetails sheet = new FEExerciseSheetWithDetails
                    (sheetRecord.getSheetId(), sheetRecord.getExerciseId(), sheetRecord.getLabel(), assignmentList, "",                        "", "", "", "", "");
                return sheet;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new FEExerciseSheetWithDetails(sheetId, exerciseId, "", new ArrayList<>(),"", "", "", "", "", "");
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/sheets/{sheetId}/edit")
    @ResponseBody
    public FEProcessResult editSheet(@PathVariable String exerciseId, @PathVariable String sheetId, @Valid EditSheetForm editSheetForm) {
        metricsService.registerAccess();
        SheetRecord sheetRecord = ctx
            .fetchOptional(SHEETS, SHEETS.EXERCISE.eq(exerciseId), SHEETS.ID.eq(sheetId))
            .orElseThrow(NotFoundException::new);
        try {
            sheetRecord.setLabelIfChanged(editSheetForm.label);
            if (sheetRecord.changed()) {
                sheetRecord.update();
                return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
        }
        return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), "ERROR");
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/sheets/{sheetId}/assignments/create")
    @ResponseBody
    public FEProcessResult createAssignment(@PathVariable String exerciseId, @PathVariable String sheetId, @Valid CreateAssignmentForm createAssignmentForm) {
        metricsService.registerAccess();
        BigDecimal maxPoints = BigDecimal.ZERO;
        try {
            maxPoints = parseMaxPoints(createAssignmentForm.getMaxPoints());
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }
        AssignmentRecord assignmentRecord = ctx.newRecord(ASSIGNMENTS);
        assignmentRecord.setExerciseId(exerciseId);
        assignmentRecord.setSheetId(sheetId);
        assignmentRecord.setAssignmentId(createAssignmentForm.getAssignmentId());
        assignmentRecord.setLabel(createAssignmentForm.getLabel());
        assignmentRecord.setMaxpoints(maxPoints);
        assignmentRecord.setShowStatistics(createAssignmentForm.getShowStatistics());
        try {
            assignmentRecord.insert();
            return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
        } catch (DuplicateKeyException e) {
            System.out.println(e.getMessage());
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
        }
    }

    @GetMapping(EXERCISE_PATH +"/{exerciseId}/sheets/{sheetId}/assignments/{assignmentId}/info")
    @ResponseBody
    public FEExerciseSheetAssignments getAssignmentInfo(@PathVariable String exerciseId, @PathVariable String sheetId, @PathVariable String assignmentId) {
        metricsService.registerAccess();

        List<AssignmentRecord> assignmentRecordList =
            ctx.fetch(ASSIGNMENTS, ASSIGNMENTS.EXERCISE.eq(exerciseId), ASSIGNMENTS.SHEET.eq(sheetId), ASSIGNMENTS.ID.eq(assignmentId));

        if (assignmentRecordList != null && !assignmentRecordList.isEmpty()) {
            return new FEExerciseSheetAssignments
                    (assignmentRecordList.get(0).getAssignmentId(), assignmentRecordList.get(0).getLabel(),
                        assignmentRecordList.get(0).getMaxpoints().toString(),
                        assignmentRecordList.get(0).getShowStatistics());
        }
       return (FEExerciseSheetAssignments) new Object();
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/sheets/{sheetId}/assignments/{assignmentId}/edit")
    @ResponseBody
    public FEProcessResult editAssignment(@PathVariable String exerciseId, @PathVariable String sheetId, @PathVariable String assignmentId, @Valid EditAssignmentForm editAssignmentForm) {
        metricsService.registerAccess();
        BigDecimal maxPoints = BigDecimal.ZERO;
        try {
            maxPoints = parseMaxPoints(editAssignmentForm.getMaxPoints());
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }
        try {
            AssignmentRecord assignmentRecord = ctx
                .fetchOptional(ASSIGNMENTS, ASSIGNMENTS.EXERCISE.eq(exerciseId), ASSIGNMENTS.SHEET.eq(sheetId), ASSIGNMENTS.ID.eq(assignmentId))
                .orElseThrow(de.rptu.cs.exclaim.controllers.NotFoundException::new);
            assignmentRecord.setLabelIfChanged(editAssignmentForm.getLabel());
            assignmentRecord.setMaxpointsIfChanged(maxPoints);
            assignmentRecord.setShowStatisticsIfChanged(editAssignmentForm.getShowStatistics());
            if (assignmentRecord.changed()) {
                assignmentRecord.update();
            }
            return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
        }
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/sheets/{sheetId}/assignments/{assignmentId}/delete")
    @ResponseBody
    public FEProcessResult deleteAssignment(@PathVariable String exerciseId, @PathVariable String sheetId, @PathVariable String assignmentId) {
        metricsService.registerAccess();
        try {
            if (ctx
                .deleteFrom(ASSIGNMENTS)
                .where(ASSIGNMENTS.EXERCISE.eq(exerciseId), ASSIGNMENTS.SHEET.eq(sheetId), ASSIGNMENTS.ID.eq(assignmentId))
                .execute() == 1
            ) {
                return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
        }
        return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), "ERROR");
    }

    @GetMapping(EXERCISE_PATH + "/{exerciseId}/groups/{groupId}/info")
    @ResponseBody
    public EditGroupForm getEditGroupPage(@PathVariable String exerciseId, @PathVariable String groupId) {
        metricsService.registerAccess();
        GroupRecord groupRecord = ctx
            .fetchOptional(GROUPS, GROUPS.EXERCISEID.eq(exerciseId), GROUPS.GROUPID.eq(groupId))
            .orElseThrow(de.rptu.cs.exclaim.controllers.NotFoundException::new);
        if (groupRecord != null && groupRecord.getGroupId() != null) {
            return new EditGroupForm(
                groupRecord.getDay(),
                groupRecord.getTime(),
                groupRecord.getLocation(),
                Optional.ofNullable(groupRecord.getMaxSize()).map(Object::toString).orElse("")
            );
        }
        return (EditGroupForm) new Object();
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/groups/{groupId}/edit")
    @ResponseBody
    public FEProcessResult editGroup(@PathVariable String exerciseId, @PathVariable String groupId, @Valid EditGroupForm editGroupForm) {
        metricsService.registerAccess();
        Integer maxSize = 0;
        try {
            maxSize = parseMaxSize(editGroupForm.getMaxSize());
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }
        try {
            GroupRecord groupRecord = ctx
                .fetchOptional(GROUPS, GROUPS.EXERCISEID.eq(exerciseId), GROUPS.GROUPID.eq(groupId))
                .orElseThrow(de.rptu.cs.exclaim.controllers.NotFoundException::new);
            groupRecord.setDayIfChanged(editGroupForm.getDay());
            groupRecord.setTimeIfChanged(StringUtils.defaultString(editGroupForm.getTime()));
            groupRecord.setLocationIfChanged(StringUtils.defaultString(editGroupForm.getLocation()));
            groupRecord.setMaxSizeIfChanged(maxSize);
            if (groupRecord.changed()) {
                groupRecord.update();
            }
            return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
        }
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/groups/{groupId}/delete")
    @ResponseBody
    public FEProcessResult deleteGroup(@PathVariable String exerciseId, @PathVariable String groupId) {
        metricsService.registerAccess();
        try {
            if (ctx
                .deleteFrom(GROUPS)
                .where(GROUPS.EXERCISEID.eq(exerciseId), GROUPS.GROUPID.eq(groupId))
                .execute() == 1
            ) {
                return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
        }
        return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), "ERROR");
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/groupRegistrationEdit/{registrationOpen}/{groupJoin}")
    @ResponseBody
    public FEProcessResult editGroupRegistration(@PathVariable String exerciseId, @PathVariable boolean registrationOpen, @PathVariable String groupJoin) {
        metricsService.registerAccess();
        try {
            ExerciseRecord exerciseRecord = ctx.fetchOptional(EXERCISES, EXERCISES.ID.eq(exerciseId)).orElseThrow(de.rptu.cs.exclaim.controllers.NotFoundException::new);
            exerciseRecord.setRegistrationOpenIfChanged(registrationOpen);
            exerciseRecord.setGroupJoinIfChanged(GroupJoin.valueOf(groupJoin));
            if (exerciseRecord.changed()) {
                exerciseRecord.update();
            }
            return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
        } catch (Exception e){
            System.out.println(e.getMessage());
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
        }
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/groups/{groupId}/join")
    @ResponseBody
    public ResponseEntity<String> joinGroup(
        @PathVariable String exerciseId,
        @PathVariable String groupId) {

        metricsService.registerAccess();

        try {

            IUser user = accessChecker.getUser();
            ExerciseRoles exerciseRoles = accessChecker.getExerciseRoles(exerciseId);

            // Check if the group can be joined
            GroupAndTeam groupAndTeam = exerciseRoles.getGroupAndTeam();
            if (groupAndTeam != null && groupId != null && groupAndTeam.getGroupId() == null) {
                GroupJoin groupJoin = ctx
                    .select(EXERCISES.GROUP_JOIN)
                    .from(EXERCISES)
                    .where(EXERCISES.ID.eq(exerciseId))
                    .fetchSingle(Record1::value1);

                if (groupJoin == GroupJoin.GROUP) {
                    Integer maxSize = ctx
                        .select(GROUPS.MAX_SIZE)
                        .from(GROUPS)
                        .where(GROUPS.EXERCISEID.eq(exerciseId), GROUPS.GROUPID.eq(groupId))
                        .fetchOneInto(Integer.class);

                    Integer currentSize = ctx
                        .select(DSL.count(STUDENTS.USERID))
                        .from(STUDENTS)
                        .where(STUDENTS.EXERCISEID.eq(exerciseId), STUDENTS.GROUPID.eq(groupId))
                        .fetchOneInto(Integer.class);

                    if (currentSize == null) {
                        log.warn("Failed to fetch current size; currentSize is null.");
                        // You could either throw an exception or assign a default value
                        currentSize = 0;  // Use 0 if null implies no students
                    }
                    if (maxSize == null || currentSize < maxSize) {
                        // Proceed to join the group
                        int updatedRows = ctx
                            .update(STUDENTS)
                            .set(STUDENTS.GROUPID, groupId)
                            .where(STUDENTS.USERID.eq(user.getUserId()), STUDENTS.EXERCISEID.eq(exerciseId))
                            .execute();

                        if (updatedRows == 1) {
                            log.info("Successfully joined the group for userId: {}", user.getUserId());
                            return ResponseEntity.ok("Successfully joined the group.");
                        } else {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Failed to join the group. Please try again.");
                        }
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("The group is full. Unable to join.");
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid group or exercise details. Unable to join.");

        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error has occurred. Please try again later.");
        }
    }
    
    @GetMapping(EXERCISE_PATH + "/{exerciseId}/groups/{groupId}/tutors")
    @ResponseBody
    public List<FEUser> getTutorsPage(@PathVariable String exerciseId, @PathVariable String groupId) {
        List<FEUser> tutorList = new ArrayList<>();
        metricsService.registerAccess();
        Tutors t = TUTORS.as("t");
        Users u = t.user().as("u");
        List<IUser> tutors = ctx
            .select(u)
            .from(t)
            .where(t.EXERCISEID.eq(exerciseId), t.GROUPID.eq(groupId))
            .fetch(Record1::value1);
        tutors.sort(Comparators.USER_BY_NAME);

        for (IUser tutor : tutors) {
            tutorList.add(new FEUser(tutor.getUserId(), tutor.getFirstname(), tutor.getLastname(), tutor.getEmail()));
        }
        return tutorList;
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/groups/{groupId}/tutors/{userId}/delete")
    @ResponseBody
    @Transactional
    public FEProcessResult deleteTutor(@PathVariable String exerciseId, @PathVariable String groupId, @PathVariable int userId) {
        metricsService.registerAccess();
        try {
            if (ctx
                .deleteFrom(TUTORS)
                .where(TUTORS.USERID.eq(userId), TUTORS.EXERCISEID.eq(exerciseId), TUTORS.GROUPID.eq(groupId))
                .execute() == 1
            ) {
                return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
            } else {
                return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), "ERROR");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
        }
    }

    @PostMapping(EXERCISE_PATH + "/{exerciseId}/groups/{groupId}/tutors/{username}/add")
    @ResponseBody
    @Transactional
    public FEProcessResult addTutor(@PathVariable String exerciseId, @PathVariable String groupId, @PathVariable String username) {
        metricsService.registerAccess();
        GroupRecord groupRecord = ctx
            .fetchOptional(GROUPS, GROUPS.EXERCISEID.eq(exerciseId), GROUPS.GROUPID.eq(groupId))
            .orElseThrow(de.rptu.cs.exclaim.controllers.NotFoundException::new);
        Integer tutorUserId = ctx
            .select(USERS.USERID)
            .from(USERS)
            .where(USERS.USERNAME.eq(username))
            .fetchOne(Record1::value1);
        if (tutorUserId != null) {
            TutorRecord tutor = ctx.newRecord(TUTORS);
            tutor.setExerciseId(groupRecord.getExerciseId());
            tutor.setGroupId(groupRecord.getGroupId());
            tutor.setUserId(tutorUserId);
            try {
                tutor.insert();
                return new FEProcessResult(ControllerUtils.MessageType.SUCCESS.name(), "OK");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), e.getMessage()!=null ? e.getMessage() : "ERROR");
            }
        } else {
            return new FEProcessResult(ControllerUtils.MessageType.ERROR.name(), "ERROR");
        }
    }

    @GetMapping(EXERCISE_PATH + "/{exerciseId}/results")
    @ResponseBody
    public List<FEExerciseResultDetails> getExerciseResult(@PathVariable String exerciseId) {

        IUser user = accessChecker.getUser();
        Optional<FEExerciseWithDetails> exerciseWithDetails =  queryExercise(exerciseId, user)
            .fetchOptional(Records.mapping(ExerciseApiController::mapExercise))
            .map(this::fetchExerciseDetails);

        if (exerciseWithDetails!= null && exerciseWithDetails.isPresent() &&
            exerciseWithDetails.get().getRoles()!=null) {

            FEExerciseRoles feExerciseRoles = exerciseWithDetails.get().getRoles();
            GroupAndTeam groupAndTeam;
            if (feExerciseRoles.getStudent()!=null) {
                groupAndTeam = new GroupAndTeam(feExerciseRoles.getStudent().getGroupId(),
                    feExerciseRoles.getStudent().getTeamId());
            }else {
                groupAndTeam = new GroupAndTeam("","");
            }


            Set<String> tutorsGroups = new HashSet<String>(feExerciseRoles.getTutorGroups());
            ExerciseRoles exerciseRoles = new ExerciseRoles(groupAndTeam, tutorsGroups, feExerciseRoles.getAssistant());

            Sheets s = SHEETS.as("s");
            Students stud = STUDENTS.as("stud");
            Users u = stud.user().as("u");
            Studentresults sr = STUDENTRESULTS.as("sr");
            Teamresults tr = TEAMRESULTS.as("tr");
            TeamresultsAssignment tra = TEAMRESULTS_ASSIGNMENT.as("tra");
            List<StudentWithSheetResults> students = ctx
                .select(
                    stud,
                    u,
                    DSL.multisetAgg(
                            s.ID,
                            sr.GROUPID,
                            sr.TEAMID,
                            // teampoints
                            DSL.field(DSL
                                .select(DSL.sum(tra.POINTS))
                                .from(tra)
                                .where(tra.EXERCISE.eq(tr.EXERCISE), tra.SHEET.eq(tr.SHEET), tra.GROUPID.eq(tr.GROUPID), tra.TEAMID.eq(tr.TEAMID))
                            ),
                            sr.DELTAPOINTS,
                            tr.HIDEPOINTS,
                            sr.ATTENDED
                        )
                        .convertFrom(r -> r.intoMap(Record7::value1, r2 -> new StudentWithSheetResults.SheetResult(
                            /* groupId */ r2.value2(),
                            /* teamId */ r2.value3(),
                            /* teampoints */ r2.value4(),
                            /* deltapoints */ r2.value5(),
                            /* hidePoints */ Boolean.TRUE.equals(r2.value6()),
                            /* attended */ r2.value7()
                        ))).as("sheetResults")
                )
                .from(stud)
                .leftJoin(s).on(s.EXERCISE.eq(exerciseId))
                .leftJoin(sr).onKey(Keys.FK__STUDENTRESULTS__SHEETS).and(sr.USERID.eq(u.USERID))
                .leftJoin(tr).onKey(Keys.FK__TEAMRESULTS__SHEETS).and(tr.GROUPID.eq(sr.GROUPID)).and(tr.TEAMID.eq(sr.TEAMID))
                .where(
                    stud.EXERCISEID.eq(exerciseId),
                    exerciseRoles.applyGroupIdRestriction(stud.GROUPID)
                )
                .groupBy(stud, u)
                .fetch(Records.mapping(StudentWithSheetResults::new));

            Assignments a = ASSIGNMENTS.as("a");
            List<SheetWithMaxPoints> sheets = ctx
                .select(s, DSL.sum(a.MAXPOINTS).as("maxpoints"))
                .from(s)
                .leftJoin(a).onKey(Keys.FK__ASSIGNMENTS__SHEETS)
                .where(s.EXERCISE.eq(exerciseId))
                .groupBy(s)
                .fetch(Records.mapping(SheetWithMaxPoints::new));
            sheets.sort(Comparator.comparing(SheetWithMaxPoints::getSheetId, Comparators.IDENTIFIER));
            BigDecimal maxPointsTotal = BigDecimal.ZERO;
            for (SheetWithMaxPoints sheet : sheets) {
                maxPointsTotal = maxPointsTotal.add(sheet.getMaxPoints());
            }

            int numSheets = sheets.size();


            return mapStudentDataToResults(students, numSheets, sheets, maxPointsTotal);
        }

        return new ArrayList<>();
    }

    private List<FEExerciseResultDetails> mapStudentDataToResults(List<StudentWithSheetResults> studentsList, int numSheets, List<SheetWithMaxPoints> sheets, BigDecimal maxPointsTotal) {

        List<FEExerciseResultDetails> resultList = new ArrayList<>();
        for (StudentWithSheetResults student : studentsList) {

            Map<String, StudentWithSheetResults.SheetResult> sheetResults = student.getSheetResults();

            BigDecimal[] points = new BigDecimal[numSheets];
            Attendance[] attendance = new Attendance[numSheets];

            int i = 0;
            BigDecimal achivedPoint = BigDecimal.valueOf(0);
            String resultAttendance = "";

            for (ISheet sheet : sheets) {
                StudentWithSheetResults.SheetResult sheetResult = sheetResults.get(sheet.getSheetId());
                if (sheetResult != null) {
                    points[i] = sheetResult.getPoints();
                    if (sheetResult.getPoints()!=null) {
                        achivedPoint = achivedPoint.add(sheetResult.getPoints());
                    }
                    attendance[i] = sheetResult.getAttended();
                    resultAttendance = sheetResult.getAttended()!=null ? sheetResult.getAttended().getLiteral() : "";
                }
                i++;
            }


            FEExerciseResultDetails result = new FEExerciseResultDetails(
                student.getStudent() !=null && student.getStudent().getGroupId()!=null ? student.getStudent().getGroupId() : ""
                , student.getStudent() !=null && student.getStudent().getTeamId()!=null ? student.getStudent().getTeamId() : "",
                student.getUser().getUserId().toString(),
                student.getUser().getStudentId() !=null ? student.getUser().getStudentId() : ""
                , student.getUser().getFirstname(),
                student.getUser().getLastname(), maxPointsTotal.toString() ,achivedPoint.toString() , resultAttendance);
            resultList.add(result);
        }
        return resultList;
    }
}

