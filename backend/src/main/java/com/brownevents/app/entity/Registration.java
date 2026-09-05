package com.brownevents.app.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime registeredAt;

    private String status;

    @ManyToOne
    @JoinColumn(name = "conference_id")
    private Conference conference;

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
