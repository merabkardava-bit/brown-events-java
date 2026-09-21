package com.brownevents.app.dto.mapper;

import com.brownevents.app.dto.ConferenceRequest;
import com.brownevents.app.dto.ConferenceResponse;
import com.brownevents.app.dto.ConferenceSummaryDto;
import com.brownevents.app.entity.Conference;

public final class ConferenceMapper {

    private ConferenceMapper() {
    }

    public static ConferenceResponse toResponse(Conference conference) {
        if (conference == null) {
            return null;
        }
        return new ConferenceResponse(
                conference.getId(),
                conference.getTitle(),
                conference.getDescription(),
                conference.getLocation(),
                conference.getStartDate(),
                conference.getEndDate(),
                conference.getStatus()
        );
    }

    public static ConferenceSummaryDto toSummary(Conference conference) {
        if (conference == null) {
            return null;
        }
        return new ConferenceSummaryDto(
                conference.getId(),
                conference.getTitle(),
                conference.getDescription(),
                conference.getLocation(),
                conference.getStartDate(),
                conference.getEndDate(),
                conference.getStatus()
        );
    }

    public static Conference toEntity(ConferenceRequest request) {
        if (request == null) {
            return null;
        }
        Conference conference = new Conference();
        conference.setTitle(request.getTitle());
        conference.setDescription(request.getDescription());
        conference.setLocation(request.getLocation());
        conference.setStartDate(request.getStartDate());
        conference.setEndDate(request.getEndDate());
        conference.setStatus(request.getStatus());
        return conference;
    }
}
