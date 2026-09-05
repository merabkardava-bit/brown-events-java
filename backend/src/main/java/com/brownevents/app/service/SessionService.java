package com.brownevents.app.service;

import com.brownevents.app.entity.Conference;
import com.brownevents.app.entity.Session;
import com.brownevents.app.repository.ConferenceRepository;
import com.brownevents.app.repository.SessionRepository;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ConferenceRepository conferenceRepository;

    public SessionService(SessionRepository sessionRepository, ConferenceRepository conferenceRepository) {
        this.sessionRepository = sessionRepository;
        this.conferenceRepository = conferenceRepository;
    }

    public Session createSession(Long conferenceId, Session session) {
        Conference conference = conferenceRepository.findById(conferenceId).get();
        session.setConference(conference);
        return sessionRepository.save(session);
    }

    public Session getSession(Long id) {
        return sessionRepository.findById(id).get();
    }

    public Session updateSession(Long id, Session session) {
        Session existing = sessionRepository.findById(id).get();
        existing.setTitle(session.getTitle());
        existing.setDescription(session.getDescription());
        existing.setStartTime(session.getStartTime());
        existing.setEndTime(session.getEndTime());
        existing.setCapacity(session.getCapacity());
        existing.setSpeaker(session.getSpeaker());
        existing.setRoom(session.getRoom());
        return sessionRepository.save(existing);
    }
}
