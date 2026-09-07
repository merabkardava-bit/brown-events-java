package com.brownevents.app.service;

import com.brownevents.app.entity.Conference;
import com.brownevents.app.repository.ConferenceRepository;
import com.brownevents.app.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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

        List<Conference> conferenceList = Arrays.asList(conf1, conf2);
        Pageable pageable = PageRequest.of(0, 12);
        Page<Conference> expected = new PageImpl<>(conferenceList, pageable, conferenceList.size());
        when(conferenceRepository.findAll(any(Pageable.class))).thenReturn(expected);

        Page<Conference> result = conferenceService.getAllConferences(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Spring Tech Summit 2024", result.getContent().get(0).getTitle());
        verify(conferenceRepository, times(1)).findAll(any(Pageable.class));
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
