package com.company.similarproducts.infrastructure.adapter.rest;

import com.company.similarproducts.infrastructure.adapter.rest.dto.VersionResponse;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for exposing version and build information
 */
@RestController
@RequestMapping("/api/v1")
public class VersionController {

    private final Optional<BuildProperties> buildProperties;
    private final Optional<GitProperties> gitProperties;
    private final VersionInfoContributor versionInfoContributor;

    private static final String UNKONWN = "unknown";

    @Autowired
    public VersionController(
            Optional<BuildProperties> buildProperties,
            Optional<GitProperties> gitProperties,
            VersionInfoContributor versionInfoContributor) {
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
        this.versionInfoContributor = versionInfoContributor;
    }

    @GetMapping("/version")
    public VersionResponse getVersion() {
        String version = buildProperties.map(BuildProperties::getVersion).orElse("1.0.0");
        String buildTime = buildProperties
            .map(build -> build.getTime() != null ? build.getTime().toString() : UNKONWN)
            .orElse(UNKONWN);
        String gitCommit = gitProperties.map(GitProperties::getShortCommitId).orElse(UNKONWN);
        String gitBranch = gitProperties.map(git -> git.get("branch")).orElse(UNKONWN);

        return new VersionResponse(
            "Similar Products API",
            version,
            buildTime,
            gitCommit,
            gitBranch
        );
    }

    @GetMapping("/info")
    public Map<String, Object> getFullInfo() {
        return versionInfoContributor.getVersionInfo();
    }
}
