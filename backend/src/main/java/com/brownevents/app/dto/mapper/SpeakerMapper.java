package com.brownevents.app.dto.mapper;

import com.brownevents.app.dto.SpeakerRequest;
import com.brownevents.app.dto.SpeakerResponse;
import com.brownevents.app.entity.Speaker;

public final class SpeakerMapper {

    private SpeakerMapper() {
    }

    public static SpeakerResponse toResponse(Speaker speaker) {
        if (speaker == null) {
            return null;
        }
        return new SpeakerResponse(
                speaker.getId(),
                speaker.getFirstName(),
                speaker.getLastName(),
                speaker.getBio(),
                speaker.getEmail()
        );
    }

    public static Speaker toEntity(SpeakerRequest request) {
        if (request == null) {
            return null;
        }
        Speaker speaker = new Speaker();
        speaker.setFirstName(request.getFirstName());
        speaker.setLastName(request.getLastName());
        speaker.setBio(request.getBio());
        speaker.setEmail(request.getEmail());
        return speaker;
    }
}
