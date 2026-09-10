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

import com.brownevents.app.entity.Session;
import com.brownevents.app.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    public void getAllConferences_shouldPassFormattedSearchTermToRepository() {
        Conference conf = new Conference();
        conf.setId(1L);
        conf.setTitle("Spring Tech Summit");
        Pageable pageable = PageRequest.of(0, 12);
        Page<Conference> expected = new PageImpl<>(List.of(conf), pageable, 1);
        when(conferenceRepository.findAllFiltered(
                eq("%spring%"), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(expected);

        Page<Conference> result = conferenceService.getAllConferences(pageable, "spring", null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Tech Summit", result.getContent().get(0).getTitle());
        verify(conferenceRepository, times(1))
            .findAllFiltered(eq("%spring%"), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    public void getAllConferences_shouldPassDateRangeToRepository() {
        Conference conf = new Conference();
        conf.setId(2L);
        conf.setTitle("Java Developer Days");
        Pageable pageable = PageRequest.of(0, 12);
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        Page<Conference> expected = new PageImpl<>(List.of(conf), pageable, 1);
        when(conferenceRepository.findAllFiltered(
                isNull(), eq(from), eq(to), isNull(), any(Pageable.class)))
            .thenReturn(expected);

        Page<Conference> result = conferenceService.getAllConferences(pageable, null, from, to, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(conferenceRepository, times(1))
            .findAllFiltered(isNull(), eq(from), eq(to), isNull(), any(Pageable.class));
    }

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
        when(conferenceRepository.findAllFiltered(isNull(), isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(expected);

        Page<Conference> result = conferenceService.getAllConferences(pageable, null, null, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Spring Tech Summit 2024", result.getContent().get(0).getTitle());
        verify(conferenceRepository, times(1)).findAllFiltered(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    public void getAllConferences_shouldEscapeWildcardsInSearchTerm() {
        // CR-003: % and _ in the raw search string must be escaped before wrapping in %...%
        Pageable pageable = PageRequest.of(0, 12);
        Page<Conference> empty = new PageImpl<>(List.of(), pageable, 0);
        when(conferenceRepository.findAllFiltered(
                eq("%50\\%off%"), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(empty);

        conferenceService.getAllConferences(pageable, "50%off", null, null, null);

        verify(conferenceRepository, times(1))
            .findAllFiltered(eq("%50\\%off%"), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    public void getAllConferences_shouldTrimStatusWhitespace() {
        // CR-004: status with surrounding whitespace must be trimmed before querying
        Pageable pageable = PageRequest.of(0, 12);
        Page<Conference> empty = new PageImpl<>(List.of(), pageable, 0);
        when(conferenceRepository.findAllFiltered(
                isNull(), isNull(), isNull(), eq("UPCOMING"), any(Pageable.class)))
            .thenReturn(empty);

        conferenceService.getAllConferences(pageable, null, null, null, "  UPCOMING  ");

        verify(conferenceRepository, times(1))
            .findAllFiltered(isNull(), isNull(), isNull(), eq("UPCOMING"), any(Pageable.class));
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

    @Test
    public void getConference_notFound_shouldThrowResourceNotFoundException() {
        when(conferenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> conferenceService.getConference(99L));
        verify(conferenceRepository, times(1)).findById(99L);
    }

    @Test
    public void updateConference_notFound_shouldThrowResourceNotFoundException() {
        when(conferenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> conferenceService.updateConference(99L, new Conference()));
        verify(conferenceRepository, times(1)).findById(99L);
    }

    @Test
    public void createSession_conferenceNotFound_shouldThrowResourceNotFoundException() {
        when(conferenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> conferenceService.createSession(99L, new Session()));
        verify(conferenceRepository, times(1)).findById(99L);
    }
}
