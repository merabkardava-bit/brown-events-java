package com.brownevents.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class EntityReferenceDto {

    @Schema(description = "Numeric ID of the referenced entity.", example = "1")
    private Long id;

    public EntityReferenceDto() {
    }

    public EntityReferenceDto(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
