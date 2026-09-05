package com.brownevents.app.service;

import com.brownevents.app.entity.Conference;
import com.brownevents.app.entity.Session;
import com.brownevents.app.repository.ConferenceRepository;
import com.brownevents.app.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private ConferenceRepository conferenceRepository;

    @InjectMocks
    private SessionService sessionService;

    @Test
    public void getSession_shouldReturnSession() {
        Session session = new Session();
        session.setId(1L);
        session.setTitle("Keynote: The Future of Spring");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Session result = sessionService.getSession(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Keynote: The Future of Spring", result.getTitle());
        verify(sessionRepository, times(1)).findById(1L);
    }

    @Test
    public void createSession_shouldSetConferenceAndSave() {
        Long conferenceId = 5L;

        Conference conference = new Conference();
        conference.setId(conferenceId);
        conference.setTitle("Spring Tech Summit 2024");

        Session inputSession = new Session();
        inputSession.setTitle("Advanced Spring Security");
        inputSession.setCapacity(80);

        Session savedSession = new Session();
        savedSession.setId(100L);
        savedSession.setTitle("Advanced Spring Security");
        savedSession.setCapacity(80);
        savedSession.setConference(conference);

        when(conferenceRepository.findById(conferenceId)).thenReturn(Optional.of(conference));
        when(sessionRepository.save(inputSession)).thenReturn(savedSession);

        Session result = sessionService.createSession(conferenceId, inputSession);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertNotNull(result.getConference());
        assertEquals(conferenceId, result.getConference().getId());
        verify(conferenceRepository, times(1)).findById(conferenceId);
        verify(sessionRepository, times(1)).save(inputSession);
    }
}
