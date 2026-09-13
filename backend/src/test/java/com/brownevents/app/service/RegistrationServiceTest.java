package com.brownevents.app.service;

import com.brownevents.app.entity.Attendee;
import com.brownevents.app.entity.Conference;
import com.brownevents.app.entity.Registration;
import com.brownevents.app.exception.RegistrationClosedException;
import com.brownevents.app.exception.RegistrationMismatchException;
import com.brownevents.app.exception.ResourceNotFoundException;
import com.brownevents.app.repository.AttendeeRepository;
import com.brownevents.app.repository.ConferenceRepository;
import com.brownevents.app.repository.RegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private ConferenceRepository conferenceRepository;

    @Mock
    private AttendeeRepository attendeeRepository;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    public void deleteRegistration_registrationNotFound_shouldThrowResourceNotFoundException() {
        when(registrationRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> registrationService.deleteRegistration(1L, 99L));
        verify(registrationRepository, times(1)).existsById(99L);
        verify(registrationRepository, never()).existsByIdAndConferenceId(anyLong(), anyLong());
        verify(registrationRepository, never()).deleteById(anyLong());
    }

    @Test
    public void deleteRegistration_registrationBelongsToDifferentConference_shouldThrowRegistrationMismatchException() {
        when(registrationRepository.existsById(5L)).thenReturn(true);
        when(registrationRepository.existsByIdAndConferenceId(5L, 1L)).thenReturn(false);

        assertThrows(RegistrationMismatchException.class,
                () -> registrationService.deleteRegistration(1L, 5L));
        verify(registrationRepository, times(1)).existsById(5L);
        verify(registrationRepository, times(1)).existsByIdAndConferenceId(5L, 1L);
        verify(registrationRepository, never()).deleteById(anyLong());
    }

    @Test
    public void getRegistrations_conferenceNotFound_shouldThrowResourceNotFoundException() {
        when(conferenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> registrationService.getRegistrations(99L));
        verify(conferenceRepository, times(1)).findById(99L);
        verify(registrationRepository, never()).findByConferenceId(anyLong());
    }

    @Test
    public void registerAttendee_completedConference_shouldThrowRegistrationClosedException() {
        Conference conference = new Conference();
        conference.setId(1L);
        conference.setStatus("COMPLETED");

        Attendee attendee = new Attendee();
        attendee.setEmail("attendee@example.com");

        when(conferenceRepository.findById(1L)).thenReturn(Optional.of(conference));

        assertThrows(RegistrationClosedException.class,
                () -> registrationService.registerAttendee(1L, attendee));
        verify(registrationRepository, never()).save(any());
        verify(attendeeRepository, never()).save(any());
    }

    @Test
    public void registerAttendee_cancelledConference_shouldThrowRegistrationClosedException() {
        Conference conference = new Conference();
        conference.setId(1L);
        conference.setStatus("CANCELLED");

        Attendee attendee = new Attendee();
        attendee.setEmail("attendee@example.com");

        when(conferenceRepository.findById(1L)).thenReturn(Optional.of(conference));

        assertThrows(RegistrationClosedException.class,
                () -> registrationService.registerAttendee(1L, attendee));
        verify(registrationRepository, never()).save(any());
        verify(attendeeRepository, never()).save(any());
    }

    @Test
    public void registerAttendee_lowercaseCompletedStatus_shouldThrowRegistrationClosedException() {
        Conference conference = new Conference();
        conference.setId(1L);
        conference.setStatus("completed");

        Attendee attendee = new Attendee();
        attendee.setEmail("attendee@example.com");

        when(conferenceRepository.findById(1L)).thenReturn(Optional.of(conference));

        assertThrows(RegistrationClosedException.class,
                () -> registrationService.registerAttendee(1L, attendee));
        verify(registrationRepository, never()).save(any());
        verify(attendeeRepository, never()).save(any());
    }

    @Test
    public void registerAttendee_nullStatus_shouldSaveRegistration() {
        Conference conference = new Conference();
        conference.setId(1L);
        conference.setStatus(null);

        Attendee attendee = new Attendee();
        attendee.setEmail("attendee@example.com");

        when(conferenceRepository.findById(1L)).thenReturn(Optional.of(conference));
        when(attendeeRepository.findByEmail("attendee@example.com")).thenReturn(Optional.of(attendee));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Registration result = registrationService.registerAttendee(1L, attendee);

        assertNotNull(result);
        verify(registrationRepository, times(1)).save(any(Registration.class));
    }

    @Test
    public void registerAttendee_activeConference_shouldSaveRegistration() {
        Conference conference = new Conference();
        conference.setId(1L);
        conference.setStatus("ACTIVE");

        Attendee attendee = new Attendee();
        attendee.setEmail("attendee@example.com");

        when(conferenceRepository.findById(1L)).thenReturn(Optional.of(conference));
        when(attendeeRepository.findByEmail("attendee@example.com")).thenReturn(Optional.of(attendee));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Registration result = registrationService.registerAttendee(1L, attendee);

        assertNotNull(result);
        verify(registrationRepository, times(1)).save(any(Registration.class));
    }
}
