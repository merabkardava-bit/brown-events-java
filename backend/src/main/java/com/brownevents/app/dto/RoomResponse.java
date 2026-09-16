package com.brownevents.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Room details returned in session responses.")
public class RoomResponse {

    @Schema(description = "Unique identifier of the room.", example = "2")
    private Long id;

    @Schema(description = "Name or label of the room.", example = "Auditorium A")
    private String name;

    @Schema(description = "Maximum seating capacity of the room.", example = "300")
    private Integer capacity;

    @Schema(description = "Location or floor where the room is situated.", example = "Building 1, Floor 2")
    private String location;

    public RoomResponse() {
    }

    public RoomResponse(Long id, String name, Integer capacity, String location) {
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
