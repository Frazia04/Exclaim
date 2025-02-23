package de.rptu.cs.exclaim.frontend.api;

import de.rptu.cs.exclaim.api.FEAccountData;
import de.rptu.cs.exclaim.api.FEChangePasswordRequest;
import de.rptu.cs.exclaim.api.FESetLanguageRequest;
import de.rptu.cs.exclaim.frontend.FrontendData;
import de.rptu.cs.exclaim.security.AccessChecker;
import de.rptu.cs.exclaim.security.ExclaimPasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;
import java.util.Optional;

import static de.rptu.cs.exclaim.schema.tables.Users.USERS;

@Controller
@RequiredArgsConstructor
public class AccountController {
    private final AccessChecker accessChecker;
    private final DSLContext ctx;
    private final ExclaimPasswordEncoder passwordEncoder;

    @GetMapping("/api/account")
    @ResponseBody
    public Optional<FEAccountData> getAccount() {
        return accessChecker.getUserWithPermissionsOpt().map(FrontendData::accountData);
    }

    @PostMapping("/api/account/language")
    @ResponseBody
    public void setLanguage(@RequestBody FESetLanguageRequest request) {
        // Use unchecked method to avoid overhead of fetching the user
        int userId = accessChecker.getUserIdUnchecked();
        ctx
            .update(USERS)
            .set(USERS.LANGUAGE, FrontendData.mapLanguage(request.getLanguage()))
            .where(USERS.USERID.eq(userId))
            .execute();
    }

    @PostMapping("/api/account/password")
    @ResponseBody
    public ResponseEntity<String> changePassword(@RequestBody FEChangePasswordRequest request) {
        // Use unchecked method to avoid overhead of fetching the user
        int userId = accessChecker.getUserIdUnchecked();
        String currentPasswordHash = ctx
            .select(USERS.PASSWORD)
            .from(USERS)
            .where(USERS.USERID.eq(userId))
            .fetchOne(USERS.PASSWORD);

        if (currentPasswordHash == null || !passwordEncoder.matches(request.getOldPassword(), currentPasswordHash)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect Password");
        }
        else{
            String newPasswordHash = passwordEncoder.encode(request.getPassword());
            ctx
                .update(USERS)
                .set(USERS.PASSWORD, newPasswordHash)
                .where(USERS.USERID.eq(userId))
                .execute();
            return ResponseEntity.status(HttpStatus.OK).body("Password Changed Successfully!");
        }

    }
}
