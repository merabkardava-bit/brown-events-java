package com.brownevents.app.controller;

import com.brownevents.app.entity.Conference;
import com.brownevents.app.entity.Session;
import com.brownevents.app.service.ConferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conferences")
@CrossOrigin(origins = "*")
public class ConferenceController {

    private final ConferenceService conferenceService;

    public ConferenceController(ConferenceService conferenceService) {
        this.conferenceService = conferenceService;
    }

    @GetMapping
    public ResponseEntity<List<Conference>> getAllConferences() {
        return ResponseEntity.ok(conferenceService.getAllConferences());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conference> getConference(@PathVariable Long id) {
        return ResponseEntity.ok(conferenceService.getConference(id));
    }

    @PostMapping
    public ResponseEntity<Conference> createConference(@RequestBody Conference conference) {
        return ResponseEntity.ok(conferenceService.createConference(conference));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Conference> updateConference(@PathVariable Long id,
                                                       @RequestBody Conference conference) {
        return ResponseEntity.ok(conferenceService.updateConference(id, conference));
    }

    @GetMapping("/{id}/sessions")
    public ResponseEntity<Map<String, Object>> getConferenceSessions(@PathVariable Long id) {
        List<Session> sessions = conferenceService.getConferenceSessions(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", sessions);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/sessions")
    public ResponseEntity<Map<String, Object>> createSession(@PathVariable Long id,
                                                              @RequestBody Session session) {
        Session created = conferenceService.createSession(id, session);
        Map<String, Object> response = new HashMap<>();
        response.put("data", created);
        return ResponseEntity.ok(response);
    }
}
