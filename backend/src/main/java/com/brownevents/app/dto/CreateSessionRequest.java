package com.brownevents.app.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Request payload for creating a session under a conference.")
public class CreateSessionRequest {

    @Schema(description = "Title of the session.", example = "Intro to Spring Boot 3", required = true)
    private String title;

    @Schema(description = "Detailed description of the session.", example = "An introductory walkthrough of Spring Boot 3 features and migration tips.")
    private String description;

    @Schema(description = "Session start date/time (ISO 8601).", example = "2025-06-10T09:00:00", required = true)
    private LocalDateTime startTime;

    @Schema(description = "Session end date/time (ISO 8601).", example = "2025-06-10T10:00:00", required = true)
    private LocalDateTime endTime;

    @Schema(description = "Maximum number of attendees the session can accommodate.", example = "120")
    private Integer capacity;

    @Schema(description = "Speaker reference. Accepts an object `{\"id\": 3}` or numeric id.", example = "{\"id\": 3}")
    private EntityReferenceDto speaker;

    @Schema(description = "Room reference. Accepts an object `{\"id\": 2}` or numeric id.", example = "{\"id\": 2}")
    private EntityReferenceDto room;

    private Long speakerId;
    private Long roomId;

    public CreateSessionRequest() {
    }

    public CreateSessionRequest(String title, String description, LocalDateTime startTime,
                                LocalDateTime endTime, Integer capacity,
                                EntityReferenceDto speaker, EntityReferenceDto room) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.speaker = speaker;
        this.room = room;
        if (speaker != null) this.speakerId = speaker.getId();
        if (room != null) this.roomId = room.getId();
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

    public EntityReferenceDto getSpeaker() {
        return speaker;
    }

    public void setSpeaker(EntityReferenceDto speaker) {
        this.speaker = speaker;
        if (speaker != null && speaker.getId() != null) {
            this.speakerId = speaker.getId();
        }
    }

    public EntityReferenceDto getRoom() {
        return room;
    }

    public void setRoom(EntityReferenceDto room) {
        this.room = room;
        if (room != null && room.getId() != null) {
            this.roomId = room.getId();
        }
    }

    public Long getSpeakerId() {
        if (speakerId != null) return speakerId;
        return speaker != null ? speaker.getId() : null;
    }

    @JsonSetter("speakerId")
    public void setSpeakerId(Long speakerId) {
        this.speakerId = speakerId;
        if (this.speaker == null) {
            this.speaker = new EntityReferenceDto(speakerId);
        }
    }

    public Long getRoomId() {
        if (roomId != null) return roomId;
        return room != null ? room.getId() : null;
    }

    @JsonSetter("roomId")
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
        if (this.room == null) {
            this.room = new EntityReferenceDto(roomId);
        }
    }
}
