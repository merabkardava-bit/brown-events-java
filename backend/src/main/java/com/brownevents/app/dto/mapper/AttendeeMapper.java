package com.brownevents.app.dto.mapper;

import com.brownevents.app.dto.AttendeeRequest;
import com.brownevents.app.dto.AttendeeResponse;
import com.brownevents.app.entity.Attendee;

public final class AttendeeMapper {

    private AttendeeMapper() {
    }

    public static AttendeeResponse toResponse(Attendee attendee) {
        if (attendee == null) {
            return null;
        }
        return new AttendeeResponse(
                attendee.getId(),
                attendee.getFirstName(),
                attendee.getLastName(),
                attendee.getEmail()
        );
    }

    public static Attendee toEntity(AttendeeRequest request) {
        if (request == null) {
            return null;
        }
        Attendee attendee = new Attendee();
        attendee.setFirstName(request.getFirstName());
        attendee.setLastName(request.getLastName());
        attendee.setEmail(request.getEmail());
        return attendee;
    }
}
