package com.brownevents.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Speaker details returned by the API.")
public class SpeakerResponse {

    @Schema(description = "Unique identifier of the speaker.", example = "3")
    private Long id;

    @Schema(description = "First name of the speaker.", example = "Ada")
    private String firstName;

    @Schema(description = "Last name of the speaker.", example = "Lovelace")
    private String lastName;

    @Schema(description = "Speaker biography and professional background.",
            example = "Ada is a principal engineer at Acme Corp with 10 years of distributed-systems experience.")
    private String bio;

    @Schema(description = "Contact email of the speaker.", example = "ada.lovelace@example.com")
    private String email;

    public SpeakerResponse() {
    }

    public SpeakerResponse(Long id, String firstName, String lastName, String bio, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
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
