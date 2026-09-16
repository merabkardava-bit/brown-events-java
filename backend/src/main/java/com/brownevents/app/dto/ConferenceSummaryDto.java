package com.brownevents.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Conference summary nested inside session or registration responses.")
public class ConferenceSummaryDto {

    @Schema(description = "Unique identifier of the conference.", example = "1")
    private Long id;

    @Schema(description = "Title of the conference.", example = "Brown Tech Summit 2025")
    private String title;

    @Schema(description = "Detailed description of the conference.", example = "A two-day summit covering the latest in AI, cloud, and open-source engineering.")
    private String description;

    @Schema(description = "Physical or virtual location of the conference.", example = "Providence, RI")
    private String location;

    @Schema(description = "Date the conference begins (ISO 8601).", example = "2025-06-10")
    private LocalDate startDate;

    @Schema(description = "Date the conference ends (ISO 8601).", example = "2025-06-11")
    private LocalDate endDate;

    @Schema(description = "Current lifecycle status of the conference.", example = "UPCOMING",
            allowableValues = {"UPCOMING", "ONGOING", "COMPLETED", "CANCELLED"})
    private String status;

    public ConferenceSummaryDto() {
    }

    public ConferenceSummaryDto(Long id, String title, String description, String location,
                                LocalDate startDate, LocalDate endDate, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
