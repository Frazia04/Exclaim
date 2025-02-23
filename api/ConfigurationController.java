package de.rptu.cs.exclaim.frontend.api;

import de.rptu.cs.exclaim.api.FEConfiguration;
import de.rptu.cs.exclaim.frontend.FrontendConfiguration;
import de.rptu.cs.exclaim.security.SecurityConfig.PublicPath;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class ConfigurationController {
    public static final String CONFIGURATION_PATH = "/api/configuration";

    private final FrontendConfiguration frontendConfiguration;

    @GetMapping(CONFIGURATION_PATH)
    @ResponseBody
    public FEConfiguration getConfiguration() {
        return frontendConfiguration.getConfiguration();
    }
}
