package com.company.similarproducts.infrastructure.adapter.rest;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * Provides application version and build information
 */
@Component
public class VersionInfoContributor {

    private final Optional<BuildProperties> buildProperties;
    private final Optional<GitProperties> gitProperties;
    
    private static final String UNKNOWN = "unknown";

    @Autowired
    public VersionInfoContributor(
            Optional<BuildProperties> buildProperties,
            Optional<GitProperties> gitProperties) {
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
    }

    public Map<String, Object> getVersionInfo() {
        Map<String, Object> info = new HashMap<>();

        if (buildProperties.isPresent()) {
            Map<String, Object> buildInfo = getStringObjectMap();
            info.put("build", buildInfo);
        }

        if (gitProperties.isPresent()) {
            GitProperties git = gitProperties.get();
            Map<String, Object> gitInfo = new HashMap<>();
            gitInfo.put("commit.id", git.getShortCommitId() != null ? git.getShortCommitId() : UNKNOWN);
            gitInfo.put("commit.time", git.getCommitTime() != null ? git.getCommitTime().toString() : UNKNOWN);
            gitInfo.put("branch", git.getBranch() != null ? git.getBranch() : UNKNOWN);
            info.put("git", gitInfo);
        }

        info.put("application", "Similar Products API");
        info.put("description", "Hexagonal Architecture Multi-module Project");

        return info;
    }

    private Map<String, Object> getStringObjectMap() {
        BuildProperties build = buildProperties.get();
        Map<String, Object> buildInfo = new HashMap<>();
        buildInfo.put("version", build.getVersion() != null ? build.getVersion() : "1.0.0");
        buildInfo.put("time", build.getTime() != null ? build.getTime().toString() : UNKNOWN);
        buildInfo.put("name", build.getName() != null ? build.getName() : "Similar Products API");
        buildInfo.put("group", build.getGroup() != null ? build.getGroup() : "com.company");
        buildInfo.put("artifact", build.getArtifact() != null ? build.getArtifact() : "bootstrap");
        return buildInfo;
    }
}
