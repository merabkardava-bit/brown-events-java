package com.brownevents.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;

@Schema(description = "A speaker who can be assigned to conference sessions.")
@Entity
@Table(name = "speakers")
public class Speaker {

    @Schema(description = "Unique identifier of the speaker.", example = "3", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Speaker's first name.", example = "Ada")
    private String firstName;

    @Schema(description = "Speaker's last name.", example = "Lovelace")
    private String lastName;

    @Schema(description = "Short biography of the speaker.", example = "Ada is a principal engineer at Acme Corp with 10 years of distributed-systems experience.")
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Schema(description = "Speaker's contact email address.", example = "ada.lovelace@example.com")
    private String email;

    public Speaker() {
    }

    public Speaker(Long id, String firstName, String lastName, String bio, String email) {
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
