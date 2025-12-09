package com.company.similarproducts.infrastructure.adapter.rest.dto;

/**
 * DTO for version information response
 */
public record VersionResponse(
    String application,
    String version,
    String buildTime,
    String gitCommit,
    String gitBranch
) {
}
