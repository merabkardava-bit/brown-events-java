package com.brownevents.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for creating a speaker.")
public class SpeakerRequest {

    @Schema(description = "First name of the speaker.", example = "Ada", required = true)
    private String firstName;

    @Schema(description = "Last name of the speaker.", example = "Lovelace", required = true)
    private String lastName;

    @Schema(description = "Speaker biography and professional background.",
            example = "Ada is a principal engineer at Acme Corp with 10 years of distributed-systems experience.")
    private String bio;

    @Schema(description = "Contact email of the speaker.", example = "ada.lovelace@example.com", required = true)
    private String email;

    public SpeakerRequest() {
    }

    public SpeakerRequest(String firstName, String lastName, String bio, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
