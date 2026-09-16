package com.brownevents.app.dto.mapper;

import com.brownevents.app.dto.CreateSessionRequest;
import com.brownevents.app.dto.SessionResponse;
import com.brownevents.app.dto.UpdateSessionRequest;
import com.brownevents.app.entity.Room;
import com.brownevents.app.entity.Session;
import com.brownevents.app.entity.Speaker;

public final class SessionMapper {

    private SessionMapper() {
    }

    public static SessionResponse toResponse(Session session) {
        if (session == null) {
            return null;
        }
        return new SessionResponse(
                session.getId(),
                session.getTitle(),
                session.getDescription(),
                session.getStartTime(),
                session.getEndTime(),
                session.getCapacity(),
                ConferenceMapper.toSummary(session.getConference()),
                SpeakerMapper.toResponse(session.getSpeaker()),
                RoomMapper.toResponse(session.getRoom())
        );
    }

    public static Session toEntity(CreateSessionRequest request) {
        if (request == null) {
            return null;
        }
        Session session = new Session();
        session.setTitle(request.getTitle());
        session.setDescription(request.getDescription());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setCapacity(request.getCapacity());

        if (request.getSpeakerId() != null) {
            Speaker speaker = new Speaker();
            speaker.setId(request.getSpeakerId());
            session.setSpeaker(speaker);
        }
        if (request.getRoomId() != null) {
            Room room = new Room();
            room.setId(request.getRoomId());
            session.setRoom(room);
        }
        return session;
    }

    public static Session toEntity(UpdateSessionRequest request) {
        if (request == null) {
            return null;
        }
        Session session = new Session();
        session.setTitle(request.getTitle());
        session.setDescription(request.getDescription());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setCapacity(request.getCapacity());

        if (request.getSpeakerId() != null) {
            Speaker speaker = new Speaker();
            speaker.setId(request.getSpeakerId());
            session.setSpeaker(speaker);
        }
        if (request.getRoomId() != null) {
            Room room = new Room();
            room.setId(request.getRoomId());
            session.setRoom(room);
        }
        return session;
    }
}
