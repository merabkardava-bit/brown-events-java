package com.brownevents.app.controller;

import com.brownevents.app.entity.Attendee;
import com.brownevents.app.entity.Registration;
import com.brownevents.app.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Registrations", description = "Register attendees for conferences and manage existing registrations.")
@RestController
@RequestMapping("/api/conferences")
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    // ── POST /api/conferences/{id}/register ───────────────────────────────────

    @Operation(
            summary = "Register an attendee for a conference",
            description = "Creates a new registration with status CONFIRMED. "
                    + "If an attendee with the supplied email already exists, the existing record is reused (upsert by email). "
                    + "The response is wrapped in a `data` envelope."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Attendee registered successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"data\": {\"id\": 7, \"registeredAt\": \"2025-04-01T14:30:00\", \"status\": \"CONFIRMED\","
                                            + "\"conference\": {\"id\": 1, \"title\": \"Brown Tech Summit 2025\", \"status\": \"UPCOMING\"},"
                                            + "\"attendee\": {\"id\": 42, \"firstName\": \"Grace\", \"lastName\": \"Hopper\", \"email\": \"grace.hopper@example.com\"}}}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Conference not found.", content = @Content)
    })
    @PostMapping("/{id}/register")
    public ResponseEntity<com.brownevents.app.ApiResponse<Registration>> registerAttendee(
            @Parameter(description = "Numeric ID of the conference to register for.", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody(
                    description = "Attendee details. If the email matches an existing attendee, their record is reused.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Attendee.class),
                            examples = @ExampleObject(
                                    name = "Register attendee",
                                    value = "{"
                                            + "\"firstName\": \"Grace\","
                                            + "\"lastName\": \"Hopper\","
                                            + "\"email\": \"grace.hopper@example.com\""
                                            + "}"
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody Attendee attendee) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new com.brownevents.app.ApiResponse<>(registrationService.registerAttendee(id, attendee)));
    }

    // ── GET /api/conferences/{id}/registrations ───────────────────────────────

    @Operation(
            summary = "List registrations for a conference",
            description = "Returns all registrations (including attendee details) for the specified conference."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registrations retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Registration.class))
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Conference not found.", content = @Content)
    })
    @GetMapping("/{id}/registrations")
    public ResponseEntity<com.brownevents.app.ApiResponse<List<Registration>>> getRegistrations(
            @Parameter(description = "Numeric ID of the conference.", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(new com.brownevents.app.ApiResponse<>(registrationService.getRegistrations(id)));
    }

    // ── DELETE /api/conferences/{id}/registrations/{registrationId} ───────────

    @Operation(
            summary = "Cancel a registration",
            description = "Permanently deletes the registration. "
                    + "Returns 400 if the registrationId does not belong to the specified conferenceId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registration cancelled successfully.", content = @Content),
            @ApiResponse(responseCode = "400", description = "Registration does not belong to this conference.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conference or registration not found.", content = @Content)
    })
    @DeleteMapping("/{id}/registrations/{registrationId}")
    public ResponseEntity<Void> deleteRegistration(
            @Parameter(description = "Numeric ID of the conference.", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Numeric ID of the registration to cancel.", example = "7", required = true)
            @PathVariable Long registrationId) {
        registrationService.deleteRegistration(id, registrationId);
        return ResponseEntity.noContent().build();
    }
}
