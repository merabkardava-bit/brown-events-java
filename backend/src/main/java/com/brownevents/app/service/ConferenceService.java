package com.brownevents.app.service;

import com.brownevents.app.entity.Conference;
import com.brownevents.app.entity.Session;
import com.brownevents.app.repository.ConferenceRepository;
import com.brownevents.app.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConferenceService {

    private final ConferenceRepository conferenceRepository;
    private final SessionRepository sessionRepository;

    public ConferenceService(ConferenceRepository conferenceRepository,
                             SessionRepository sessionRepository) {
        this.conferenceRepository = conferenceRepository;
        this.sessionRepository = sessionRepository;
    }

    public List<Conference> getAllConferences() {
        return conferenceRepository.findAll();
    }

    public Conference getConference(Long id) {
        return conferenceRepository.findById(id).get();
    }

    public Conference createConference(Conference conf) {
        return conferenceRepository.save(conf);
    }

    public Conference updateConference(Long id, Conference conf) {
        Conference existing = conferenceRepository.findById(id).get();
        existing.setTitle(conf.getTitle());
        existing.setDescription(conf.getDescription());
        existing.setLocation(conf.getLocation());
        existing.setStartDate(conf.getStartDate());
        existing.setEndDate(conf.getEndDate());
        existing.setStatus(conf.getStatus().toUpperCase());
        return conferenceRepository.save(existing);
    }

    public List<Session> getConferenceSessions(Long id) {
        return sessionRepository.findByConferenceId(id);
    }

    public Session createSession(Long conferenceId, Session session) {
        Conference conference = conferenceRepository.findById(conferenceId).get();
        session.setConference(conference);
        return sessionRepository.save(session);
    }
}
