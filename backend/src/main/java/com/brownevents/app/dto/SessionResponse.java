package com.brownevents.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Session details returned by the API.")
public class SessionResponse {

    @Schema(description = "Unique identifier of the session.", example = "10")
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

    @Schema(description = "The conference this session belongs to.")
    private ConferenceSummaryDto conference;

    @Schema(description = "Speaker presenting this session.")
    private SpeakerResponse speaker;

    @Schema(description = "Room where the session is held.")
    private RoomResponse room;

    public SessionResponse() {
    }

    public SessionResponse(Long id, String title, String description, LocalDateTime startTime,
                           LocalDateTime endTime, Integer capacity, ConferenceSummaryDto conference,
                           SpeakerResponse speaker, RoomResponse room) {
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

    public ConferenceSummaryDto getConference() {
        return conference;
    }

    public void setConference(ConferenceSummaryDto conference) {
        this.conference = conference;
    }

    public SpeakerResponse getSpeaker() {
        return speaker;
    }

    public void setSpeaker(SpeakerResponse speaker) {
        this.speaker = speaker;
    }

    public RoomResponse getRoom() {
        return room;
    }

    public void setRoom(RoomResponse room) {
        this.room = room;
    }
}
