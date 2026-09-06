package com.brownevents.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;

@Schema(description = "A person who registers for one or more conferences.")
@Entity
@Table(name = "attendees")
public class Attendee {

    @Schema(description = "Unique identifier of the attendee.", example = "42", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Attendee's first name.", example = "Grace")
    private String firstName;

    @Schema(description = "Attendee's last name.", example = "Hopper")
    private String lastName;

    @Schema(description = "Attendee's email address. Used to de-duplicate attendees — existing records are reused on re-registration.", example = "grace.hopper@example.com")
    private String email;

    public Attendee() {
    }

    public Attendee(Long id, String firstName, String lastName, String email) {
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
