package com.brownevents.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for registering an attendee.")
public class AttendeeRequest {

    @Schema(description = "First name of the attendee.", example = "Grace", required = true)
    private String firstName;

    @Schema(description = "Last name of the attendee.", example = "Hopper", required = true)
    private String lastName;

    @Schema(description = "Email address of the attendee.", example = "grace.hopper@example.com", required = true)
    private String email;

    public AttendeeRequest() {
    }

    public AttendeeRequest(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
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
