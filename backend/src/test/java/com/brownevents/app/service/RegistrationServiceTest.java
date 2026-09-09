package com.brownevents.app.service;

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
}
