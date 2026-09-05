package com.brownevents.app.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conference_sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer capacity;

    @ManyToOne
    @JoinColumn(name = "conference_id")
    private Conference conference;

    @ManyToOne
    @JoinColumn(name = "speaker_id")
    private Speaker speaker;

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
