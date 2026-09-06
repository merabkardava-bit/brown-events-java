package com.brownevents.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "A conference event hosted on the platform.")
@Entity
@Table(name = "conferences")
public class Conference {

    @Schema(description = "Unique identifier of the conference.", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @OneToMany(mappedBy = "conference", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Session> sessions;

    @OneToMany(mappedBy = "conference", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Registration> registrations;

    public Conference() {
    }

    public Conference(Long id, String title, String description, String location,
                      LocalDate startDate, LocalDate endDate, String status,
                      List<Session> sessions, List<Registration> registrations) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.sessions = sessions;
        this.registrations = registrations;
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

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }
}
