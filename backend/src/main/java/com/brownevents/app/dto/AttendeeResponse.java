package com.brownevents.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Attendee details returned by the API.")
public class AttendeeResponse {

    @Schema(description = "Unique identifier of the attendee.", example = "42")
    private Long id;

    @Schema(description = "First name of the attendee.", example = "Grace")
    private String firstName;

    @Schema(description = "Last name of the attendee.", example = "Hopper")
    private String lastName;

    @Schema(description = "Email address of the attendee.", example = "grace.hopper@example.com")
    private String email;

    public AttendeeResponse() {
    }

    public AttendeeResponse(Long id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
