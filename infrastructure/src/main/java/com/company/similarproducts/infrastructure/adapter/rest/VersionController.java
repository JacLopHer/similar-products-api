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

    private static final String UNKNOWN = "unknown";

    private final Optional<BuildProperties> buildProperties;
    private final Optional<GitProperties> gitProperties;
    private final VersionInfoContributor versionInfoContributor;


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
            .map(build -> build.getTime() != null ? build.getTime().toString() : UNKNOWN)
            .orElse(UNKNOWN);
        String gitCommit = gitProperties.map(GitProperties::getShortCommitId).orElse(UNKNOWN);
        String gitBranch = gitProperties.map(git -> git.get("branch")).orElse(UNKNOWN);

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
