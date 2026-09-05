package com.brownevents.app.controller;

import com.brownevents.app.entity.Attendee;
import com.brownevents.app.entity.Registration;
import com.brownevents.app.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conferences")
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<Map<String, Object>> registerAttendee(@PathVariable Long id,
                                                                 @RequestBody Attendee attendee) {
        Registration registration = registrationService.registerAttendee(id, attendee);
        Map<String, Object> response = new HashMap<>();
        response.put("data", registration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/registrations")
    public ResponseEntity<List<Registration>> getRegistrations(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.getRegistrations(id));
    }

    @DeleteMapping("/{id}/registrations/{registrationId}")
    public ResponseEntity<Void> deleteRegistration(@PathVariable Long id,
                                                   @PathVariable Long registrationId) {
        registrationService.deleteRegistration(id, registrationId);
        return ResponseEntity.noContent().build();
    }
}
