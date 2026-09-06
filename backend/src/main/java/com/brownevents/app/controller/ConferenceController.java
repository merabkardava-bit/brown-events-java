package com.brownevents.app.controller;

import com.brownevents.app.entity.Conference;
import com.brownevents.app.entity.Session;
import com.brownevents.app.service.ConferenceService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Conferences", description = "Create and manage conferences, including their sessions.")
@RestController
@RequestMapping("/api/conferences")
@CrossOrigin(origins = "*")
public class ConferenceController {

    private final ConferenceService conferenceService;

    public ConferenceController(ConferenceService conferenceService) {
        this.conferenceService = conferenceService;
    }

    // ── GET /api/conferences ──────────────────────────────────────────────────

    @Operation(
            summary = "List all conferences",
            description = "Returns every conference stored in the system, regardless of status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conferences retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Conference.class))
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<Conference>> getAllConferences() {
        return ResponseEntity.ok(conferenceService.getAllConferences());
    }

    // ── GET /api/conferences/{id} ─────────────────────────────────────────────

    @Operation(
            summary = "Get a conference by ID",
            description = "Returns a single conference identified by its numeric ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conference found and returned.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Conference.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Conference not found.", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Conference> getConference(
            @Parameter(description = "Numeric ID of the conference.", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(conferenceService.getConference(id));
    }

    // ── POST /api/conferences ─────────────────────────────────────────────────

    @Operation(
            summary = "Create a conference",
            description = "Creates a new conference. The `id` field is ignored if supplied — it is assigned by the database."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conference created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Conference.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<Conference> createConference(
            @RequestBody(
                    description = "Conference details to create.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Conference.class),
                            examples = @ExampleObject(
                                    name = "New conference",
                                    value = "{"
                                            + "\"title\": \"Brown Tech Summit 2025\","
                                            + "\"description\": \"A two-day summit covering AI, cloud, and open-source engineering.\","
                                            + "\"location\": \"Providence, RI\","
                                            + "\"startDate\": \"2025-06-10\","
                                            + "\"endDate\": \"2025-06-11\","
                                            + "\"status\": \"UPCOMING\""
                                            + "}"
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody Conference conference) {
        return ResponseEntity.ok(conferenceService.createConference(conference));
    }

    // ── PUT /api/conferences/{id} ─────────────────────────────────────────────

    @Operation(
            summary = "Update a conference",
            description = "Replaces all editable fields of an existing conference. Supply the full conference object — omitted fields are overwritten with null."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conference updated successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Conference.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Conference not found.", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Conference> updateConference(
            @Parameter(description = "Numeric ID of the conference to update.", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody(
                    description = "Updated conference details.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Conference.class),
                            examples = @ExampleObject(
                                    name = "Update status to ONGOING",
                                    value = "{"
                                            + "\"title\": \"Brown Tech Summit 2025\","
                                            + "\"description\": \"A two-day summit covering AI, cloud, and open-source engineering.\","
                                            + "\"location\": \"Providence, RI\","
                                            + "\"startDate\": \"2025-06-10\","
                                            + "\"endDate\": \"2025-06-11\","
                                            + "\"status\": \"ONGOING\""
                                            + "}"
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody Conference conference) {
        return ResponseEntity.ok(conferenceService.updateConference(id, conference));
    }

    // ── GET /api/conferences/{id}/sessions ────────────────────────────────────

    @Operation(
            summary = "List sessions for a conference",
            description = "Returns all sessions belonging to the specified conference, wrapped in a `data` envelope."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sessions retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"data\": [{\"id\": 10, \"title\": \"Intro to Spring Boot 3\","
                                            + "\"description\": \"An introductory walkthrough of Spring Boot 3.\","
                                            + "\"startTime\": \"2025-06-10T09:00:00\", \"endTime\": \"2025-06-10T10:00:00\","
                                            + "\"capacity\": 120,"
                                            + "\"speaker\": {\"id\": 3, \"firstName\": \"Ada\", \"lastName\": \"Lovelace\", \"bio\": \"...\", \"email\": \"ada@example.com\"},"
                                            + "\"room\": {\"id\": 2, \"name\": \"Auditorium A\", \"capacity\": 300, \"location\": \"Building 1, Floor 2\"}}]}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Conference not found.", content = @Content)
    })
    @GetMapping("/{id}/sessions")
    public ResponseEntity<Map<String, Object>> getConferenceSessions(
            @Parameter(description = "Numeric ID of the conference.", example = "1", required = true)
            @PathVariable Long id) {
        List<Session> sessions = conferenceService.getConferenceSessions(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", sessions);
        return ResponseEntity.ok(response);
    }

    // ── POST /api/conferences/{id}/sessions ───────────────────────────────────

    @Operation(
            summary = "Add a session to a conference",
            description = "Creates a new session under the specified conference. "
                    + "Reference an existing speaker and room by passing only their id. "
                    + "The conference field in the body is ignored — the path {id} is the authoritative source."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Session created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"data\": {\"id\": 10, \"title\": \"Intro to Spring Boot 3\","
                                            + "\"description\": \"An introductory walkthrough of Spring Boot 3.\","
                                            + "\"startTime\": \"2025-06-10T09:00:00\", \"endTime\": \"2025-06-10T10:00:00\","
                                            + "\"capacity\": 120,"
                                            + "\"speaker\": {\"id\": 3, \"firstName\": \"Ada\", \"lastName\": \"Lovelace\", \"bio\": \"...\", \"email\": \"ada@example.com\"},"
                                            + "\"room\": {\"id\": 2, \"name\": \"Auditorium A\", \"capacity\": 300, \"location\": \"Building 1, Floor 2\"}}}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Conference not found.", content = @Content)
    })
    @PostMapping("/{id}/sessions")
    public ResponseEntity<Map<String, Object>> createSession(
            @Parameter(description = "Numeric ID of the conference that will own this session.", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody(
                    description = "Session details. Reference speaker and room by their IDs only.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Session.class),
                            examples = @ExampleObject(
                                    name = "New session",
                                    value = "{"
                                            + "\"title\": \"Intro to Spring Boot 3\","
                                            + "\"description\": \"An introductory walkthrough of Spring Boot 3 features and migration tips.\","
                                            + "\"startTime\": \"2025-06-10T09:00:00\","
                                            + "\"endTime\": \"2025-06-10T10:00:00\","
                                            + "\"capacity\": 120,"
                                            + "\"speaker\": {\"id\": 3},"
                                            + "\"room\": {\"id\": 2}"
                                            + "}"
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody Session session) {
        Session created = conferenceService.createSession(id, session);
        Map<String, Object> response = new HashMap<>();
        response.put("data", created);
        return ResponseEntity.ok(response);
    }
}
