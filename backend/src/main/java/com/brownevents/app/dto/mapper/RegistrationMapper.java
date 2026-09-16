package com.brownevents.app.dto.mapper;

import com.brownevents.app.dto.RegistrationResponse;
import com.brownevents.app.entity.Registration;

public final class RegistrationMapper {

    private RegistrationMapper() {
    }

    public static RegistrationResponse toResponse(Registration registration) {
        if (registration == null) {
            return null;
        }
        return new RegistrationResponse(
                registration.getId(),
                registration.getRegisteredAt(),
                registration.getStatus(),
                ConferenceMapper.toSummary(registration.getConference()),
                AttendeeMapper.toResponse(registration.getAttendee())
        );
    }
}
