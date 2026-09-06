package com.brownevents.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.time.LocalDateTime;

@Schema(description = "A scheduled session (talk or workshop) within a conference.")
@Entity
@Table(name = "conference_sessions")
public class Session {

    @Schema(description = "Unique identifier of the session.", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Title of the session.", example = "Intro to Spring Boot 3")
    private String title;

    @Schema(description = "Detailed description of the session.", example = "An introductory walkthrough of Spring Boot 3 features and migration tips.")
    private String description;

    @Schema(description = "Session start date/time (ISO 8601).", example = "2025-06-10T09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "Session end date/time (ISO 8601).", example = "2025-06-10T10:00:00")
    private LocalDateTime endTime;

    @Schema(description = "Maximum number of attendees the session can accommodate.", example = "120")
    private Integer capacity;

    @Schema(description = "The conference this session belongs to. Only the id field is required when creating a session.")
    @ManyToOne
    @JoinColumn(name = "conference_id")
    private Conference conference;

    @Schema(description = "Speaker presenting this session. Only the id field is required when creating a session.")
    @ManyToOne
    @JoinColumn(name = "speaker_id")
    private Speaker speaker;

    @Schema(description = "Room where the session is held. Only the id field is required when creating a session.")
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    public Session() {
    }

    public Session(Long id, String title, String description, LocalDateTime startTime,
                   LocalDateTime endTime, Integer capacity, Conference conference,
                   Speaker speaker, Room room) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.conference = conference;
        this.speaker = speaker;
        this.room = room;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Conference getConference() {
        return conference;
    }

    public void setConference(Conference conference) {
        this.conference = conference;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
