package com.brownevents.app.controller;

import com.brownevents.app.entity.Session;
import com.brownevents.app.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Session> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSession(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Session> updateSession(@PathVariable Long id,
                                                  @RequestBody Session session) {
        return ResponseEntity.ok(sessionService.updateSession(id, session));
    }
}
