package com.brownevents.app.service;

import com.brownevents.app.entity.Attendee;
import com.brownevents.app.entity.Conference;
import com.brownevents.app.entity.Registration;
import com.brownevents.app.repository.AttendeeRepository;
import com.brownevents.app.repository.ConferenceRepository;
import com.brownevents.app.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final ConferenceRepository conferenceRepository;
    private final AttendeeRepository attendeeRepository;

    public RegistrationService(RegistrationRepository registrationRepository,
                               ConferenceRepository conferenceRepository,
                               AttendeeRepository attendeeRepository) {
        this.registrationRepository = registrationRepository;
        this.conferenceRepository = conferenceRepository;
        this.attendeeRepository = attendeeRepository;
    }

    public Registration registerAttendee(Long conferenceId, Attendee attendee) {
        Attendee savedAttendee = attendeeRepository.findByEmail(attendee.getEmail())
                .orElseGet(() -> attendeeRepository.save(attendee));
        Conference conference = conferenceRepository.findById(conferenceId).get();
        Registration registration = new Registration();
        registration.setAttendee(savedAttendee);
        registration.setConference(conference);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setStatus("CONFIRMED");
        return registrationRepository.save(registration);
    }

    public List<Registration> getRegistrations(Long conferenceId) {
        return registrationRepository.findByConferenceId(conferenceId);
    }

    public void deleteRegistration(Long conferenceId, Long registrationId) {
        if (!registrationRepository.existsByIdAndConferenceId(registrationId, conferenceId)) {
            throw new IllegalArgumentException("Registration not found for this conference");
        }
        registrationRepository.deleteById(registrationId);
    }
}
