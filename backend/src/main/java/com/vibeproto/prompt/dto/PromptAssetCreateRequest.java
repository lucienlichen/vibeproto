package com.vibeproto.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PromptAssetCreateRequest(
    @NotNull Long projectId,
    @NotBlank String title,
    @NotBlank String promptType,
    @NotBlank String content,
    Long relatedSourceVersionId,
    Long relatedReleaseId
) {
}
