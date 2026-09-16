package com.brownevents.app.controller;

import com.brownevents.app.ApiResponse;
import com.brownevents.app.dto.SpeakerRequest;
import com.brownevents.app.dto.SpeakerResponse;
import com.brownevents.app.dto.mapper.SpeakerMapper;
import com.brownevents.app.entity.Speaker;
import com.brownevents.app.service.SpeakerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Speakers", description = "Create and list speakers who can be assigned to conference sessions.")
@RestController
@RequestMapping("/api/speakers")
@CrossOrigin(origins = "*")
public class SpeakerController {

    private final SpeakerService speakerService;

    public SpeakerController(SpeakerService speakerService) {
        this.speakerService = speakerService;
    }

    // ── GET /api/speakers ─────────────────────────────────────────────────────

    @Operation(
            summary = "List all speakers",
            description = "Returns every speaker registered in the system."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Speakers retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SpeakerResponse.class))
                    )
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<SpeakerResponse>>> getAllSpeakers() {
        List<Speaker> speakers = speakerService.getAllSpeakers();
        List<SpeakerResponse> response = speakers.stream()
                .map(SpeakerMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // ── POST /api/speakers ────────────────────────────────────────────────────

    @Operation(
            summary = "Create a speaker",
            description = "Registers a new speaker. The created speaker can then be referenced by ID when adding sessions. "
                    + "The response is wrapped in a `data` envelope."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Speaker created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"data\": {\"id\": 3, \"firstName\": \"Ada\", \"lastName\": \"Lovelace\","
                                            + "\"bio\": \"Ada is a principal engineer at Acme Corp with 10 years of distributed-systems experience.\","
                                            + "\"email\": \"ada.lovelace@example.com\"}}"
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SpeakerResponse>> createSpeaker(
            @RequestBody(
                    description = "Speaker details to register.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpeakerRequest.class),
                            examples = @ExampleObject(
                                    name = "New speaker",
                                    value = "{"
                                            + "\"firstName\": \"Ada\","
                                            + "\"lastName\": \"Lovelace\","
                                            + "\"bio\": \"Ada is a principal engineer at Acme Corp with 10 years of distributed-systems experience.\","
                                            + "\"email\": \"ada.lovelace@example.com\""
                                            + "}"
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody SpeakerRequest speakerRequest) {
        Speaker speaker = SpeakerMapper.toEntity(speakerRequest);
        Speaker created = speakerService.createSpeaker(speaker);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(SpeakerMapper.toResponse(created)));
    }
}
