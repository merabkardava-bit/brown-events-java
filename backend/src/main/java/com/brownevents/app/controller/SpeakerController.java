package com.brownevents.app.controller;

import com.brownevents.app.entity.Speaker;
import com.brownevents.app.service.SpeakerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/speakers")
@CrossOrigin(origins = "*")
public class SpeakerController {

    private final SpeakerService speakerService;

    public SpeakerController(SpeakerService speakerService) {
        this.speakerService = speakerService;
    }

    @GetMapping
    public ResponseEntity<List<Speaker>> getAllSpeakers() {
        return ResponseEntity.ok(speakerService.getAllSpeakers());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSpeaker(@RequestBody Speaker speaker) {
        Speaker created = speakerService.createSpeaker(speaker);
        Map<String, Object> response = new HashMap<>();
        response.put("data", created);
        return ResponseEntity.ok(response);
    }
}
