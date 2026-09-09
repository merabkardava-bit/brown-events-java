package com.brownevents.app.controller;

import com.brownevents.app.entity.Session;
import com.brownevents.app.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Sessions", description = "Retrieve and update individual conference sessions.")
@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // ── GET /api/sessions/{id} ────────────────────────────────────────────────

    @Operation(
            summary = "Get a session by ID",
            description = "Returns a single conference session identified by its numeric ID, including the linked speaker and room."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Session found and returned.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Session.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Session not found.", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<com.brownevents.app.ApiResponse<Session>> getSession(
            @Parameter(description = "Numeric ID of the session.", example = "10", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(new com.brownevents.app.ApiResponse<>(sessionService.getSession(id)));
    }

    // ── PUT /api/sessions/{id} ────────────────────────────────────────────────

    @Operation(
            summary = "Update a session",
            description = "Merges the supplied fields into an existing session. "
                    + "Updatable fields: title, description, startTime, endTime, capacity, speaker (by id), room (by id). "
                    + "The session's parent conference cannot be changed via this endpoint."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Session updated successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Session.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Session not found.", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<com.brownevents.app.ApiResponse<Session>> updateSession(
            @Parameter(description = "Numeric ID of the session to update.", example = "10", required = true)
            @PathVariable Long id,
            @RequestBody(
                    description = "Fields to update on the session. Supply speaker and room by ID only.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Session.class),
                            examples = @ExampleObject(
                                    name = "Update session capacity and room",
                                    value = "{"
                                            + "\"title\": \"Intro to Spring Boot 3\","
                                            + "\"description\": \"An introductory walkthrough of Spring Boot 3 features and migration tips.\","
                                            + "\"startTime\": \"2025-06-10T09:00:00\","
                                            + "\"endTime\": \"2025-06-10T10:30:00\","
                                            + "\"capacity\": 150,"
                                            + "\"speaker\": {\"id\": 3},"
                                            + "\"room\": {\"id\": 4}"
                                            + "}"
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody Session session) {
        return ResponseEntity.ok(new com.brownevents.app.ApiResponse<>(sessionService.updateSession(id, session)));
    }
}
