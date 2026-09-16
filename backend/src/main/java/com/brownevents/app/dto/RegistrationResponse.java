package com.brownevents.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Registration details linking an attendee to a conference.")
public class RegistrationResponse {

    @Schema(description = "Unique identifier of the registration.", example = "7")
    private Long id;

    @Schema(description = "Timestamp when the registration was created (ISO 8601).", example = "2025-04-01T14:30:00")
    private LocalDateTime registeredAt;

    @Schema(description = "Current status of the registration.", example = "CONFIRMED",
            allowableValues = {"CONFIRMED", "CANCELLED"})
    private String status;

    @Schema(description = "Conference the attendee registered for.")
    private ConferenceSummaryDto conference;

    @Schema(description = "Attendee who holds this registration.")
    private AttendeeResponse attendee;

    public RegistrationResponse() {
    }

    public RegistrationResponse(Long id, LocalDateTime registeredAt, String status,
                                ConferenceSummaryDto conference, AttendeeResponse attendee) {
        this.id = id;
        this.registeredAt = registeredAt;
        this.status = status;
        this.conference = conference;
        this.attendee = attendee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ConferenceSummaryDto getConference() {
        return conference;
    }

    public void setConference(ConferenceSummaryDto conference) {
        this.conference = conference;
    }

    public AttendeeResponse getAttendee() {
        return attendee;
    }

    public void setAttendee(AttendeeResponse attendee) {
        this.attendee = attendee;
    }
}
