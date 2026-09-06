package com.brownevents.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;

@Schema(description = "A room that can host conference sessions. Rooms are pre-seeded and are referenced by ID when creating sessions.")
@Entity
@Table(name = "rooms")
public class Room {

    @Schema(description = "Unique identifier of the room.", example = "2", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Name of the room.", example = "Auditorium A")
    private String name;

    @Schema(description = "Maximum seating capacity of the room.", example = "300")
    private Integer capacity;

    @Schema(description = "Physical location of the room within the venue.", example = "Building 1, Floor 2")
    private String location;

    public Room() {
    }

    public Room(Long id, String name, Integer capacity, String location) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
