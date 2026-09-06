package com.brownevents.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.time.LocalDateTime;

@Schema(description = "A registration linking an attendee to a conference.")
@Entity
@Table(name = "registrations")
public class Registration {

    @Schema(description = "Unique identifier of the registration.", example = "7", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Timestamp when the registration was created (ISO 8601).", example = "2025-04-01T14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime registeredAt;

    @Schema(description = "Current status of the registration.", example = "CONFIRMED",
            allowableValues = {"CONFIRMED", "CANCELLED"}, accessMode = Schema.AccessMode.READ_ONLY)
    private String status;

    @Schema(description = "Conference the attendee registered for.")
    @ManyToOne
    @JoinColumn(name = "conference_id")
    private Conference conference;

    @Schema(description = "Attendee who holds this registration.")
    @ManyToOne
    @JoinColumn(name = "attendee_id")
    private Attendee attendee;

    public Registration() {
    }

    public Registration(Long id, LocalDateTime registeredAt, String status,
                        Conference conference, Attendee attendee) {
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

    public Conference getConference() {
        return conference;
    }

    public void setConference(Conference conference) {
        this.conference = conference;
    }

    public Attendee getAttendee() {
        return attendee;
    }

    public void setAttendee(Attendee attendee) {
        this.attendee = attendee;
    }
}
