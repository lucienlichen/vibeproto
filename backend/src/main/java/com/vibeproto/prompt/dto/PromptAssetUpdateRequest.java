package com.vibeproto.prompt.dto;

import jakarta.validation.constraints.NotBlank;

public record PromptAssetUpdateRequest(
    @NotBlank String title,
    @NotBlank String promptType,
    @NotBlank String content,
    Long relatedSourceVersionId,
    Long relatedReleaseId
) {
}
