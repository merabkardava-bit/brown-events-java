package com.brownevents.app.service;

import com.brownevents.app.entity.Conference;
import com.brownevents.app.repository.ConferenceRepository;
import com.brownevents.app.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConferenceServiceTest {

    @Mock
    private ConferenceRepository conferenceRepository;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private ConferenceService conferenceService;

    @Test
    public void getAllConferences_shouldReturnAllConferences() {
        Conference conf1 = new Conference();
        conf1.setId(1L);
        conf1.setTitle("Spring Tech Summit 2024");

        Conference conf2 = new Conference();
        conf2.setId(2L);
        conf2.setTitle("Java Developer Days");

        List<Conference> expected = Arrays.asList(conf1, conf2);
        when(conferenceRepository.findAll()).thenReturn(expected);

        List<Conference> result = conferenceService.getAllConferences();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Spring Tech Summit 2024", result.get(0).getTitle());
        verify(conferenceRepository, times(1)).findAll();
    }

    @Test
    public void createConference_shouldCallSaveAndReturnResult() {
        Conference input = new Conference();
        input.setTitle("New Conference");
        input.setLocation("Boston");

        Conference saved = new Conference();
        saved.setId(10L);
        saved.setTitle("New Conference");
        saved.setLocation("Boston");

        when(conferenceRepository.save(input)).thenReturn(saved);

        Conference result = conferenceService.createConference(input);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("New Conference", result.getTitle());
        verify(conferenceRepository, times(1)).save(input);
    }
}
