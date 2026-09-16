package com.brownevents.app.dto.mapper;

import com.brownevents.app.dto.*;
import com.brownevents.app.entity.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DtoMapperTest {

    @Test
    public void conferenceMapper_toResponse_shouldMapFieldsCorrectly() {
        Conference conf = new Conference();
        conf.setId(1L);
        conf.setTitle("Test Conf");
        conf.setDescription("Description");
        conf.setLocation("City");
        conf.setStartDate(LocalDate.of(2025, 6, 10));
        conf.setEndDate(LocalDate.of(2025, 6, 11));
        conf.setStatus("UPCOMING");

        ConferenceResponse resp = ConferenceMapper.toResponse(conf);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("Test Conf", resp.getTitle());
        assertEquals("Description", resp.getDescription());
        assertEquals("City", resp.getLocation());
        assertEquals(LocalDate.of(2025, 6, 10), resp.getStartDate());
        assertEquals(LocalDate.of(2025, 6, 11), resp.getEndDate());
        assertEquals("UPCOMING", resp.getStatus());
    }

    @Test
    public void conferenceMapper_toEntity_shouldMapRequestFields() {
        ConferenceRequest req = new ConferenceRequest(
                "New Conf", "Desc", "Loc",
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 2), "UPCOMING"
        );

        Conference entity = ConferenceMapper.toEntity(req);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals("New Conf", entity.getTitle());
        assertEquals("Desc", entity.getDescription());
        assertEquals("Loc", entity.getLocation());
        assertEquals(LocalDate.of(2025, 7, 1), entity.getStartDate());
        assertEquals(LocalDate.of(2025, 7, 2), entity.getEndDate());
        assertEquals("UPCOMING", entity.getStatus());
    }

    @Test
    public void sessionMapper_toResponse_shouldMapNestedSummaryDtos() {
        Conference conf = new Conference();
        conf.setId(1L);
        conf.setTitle("Summit");

        Speaker spk = new Speaker();
        spk.setId(2L);
        spk.setFirstName("Ada");
        spk.setLastName("Lovelace");
        spk.setEmail("ada@example.com");

        Room room = new Room();
        room.setId(3L);
        room.setName("Hall A");
        room.setCapacity(100);

        LocalDateTime now = LocalDateTime.now();
        Session session = new Session(10L, "Talk", "Desc", now, now.plusHours(1), 50, conf, spk, room);

        SessionResponse resp = SessionMapper.toResponse(session);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertEquals("Talk", resp.getTitle());
        assertEquals(50, resp.getCapacity());
        assertNotNull(resp.getConference());
        assertEquals(1L, resp.getConference().getId());
        assertEquals("Summit", resp.getConference().getTitle());
        assertNotNull(resp.getSpeaker());
        assertEquals(2L, resp.getSpeaker().getId());
        assertEquals("Ada", resp.getSpeaker().getFirstName());
        assertNotNull(resp.getRoom());
        assertEquals(3L, resp.getRoom().getId());
        assertEquals("Hall A", resp.getRoom().getName());
    }

    @Test
    public void sessionMapper_toEntity_fromCreateSessionRequest() {
        CreateSessionRequest req = new CreateSessionRequest(
                "New Talk", "Desc", LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                80, new EntityReferenceDto(5L), new EntityReferenceDto(8L)
        );

        Session entity = SessionMapper.toEntity(req);

        assertNotNull(entity);
        assertEquals("New Talk", entity.getTitle());
        assertEquals(80, entity.getCapacity());
        assertNotNull(entity.getSpeaker());
        assertEquals(5L, entity.getSpeaker().getId());
        assertNotNull(entity.getRoom());
        assertEquals(8L, entity.getRoom().getId());
    }

    @Test
    public void registrationMapper_toResponse_shouldMapNestedConferenceAndAttendee() {
        Conference conf = new Conference();
        conf.setId(1L);
        conf.setTitle("DevConf");

        Attendee attendee = new Attendee();
        attendee.setId(99L);
        attendee.setFirstName("Grace");
        attendee.setLastName("Hopper");
        attendee.setEmail("grace@example.com");

        LocalDateTime regTime = LocalDateTime.of(2025, 4, 1, 10, 0);
        Registration reg = new Registration(7L, regTime, "CONFIRMED", conf, attendee);

        RegistrationResponse resp = RegistrationMapper.toResponse(reg);

        assertNotNull(resp);
        assertEquals(7L, resp.getId());
        assertEquals("CONFIRMED", resp.getStatus());
        assertEquals(regTime, resp.getRegisteredAt());
        assertNotNull(resp.getConference());
        assertEquals(1L, resp.getConference().getId());
        assertNotNull(resp.getAttendee());
        assertEquals(99L, resp.getAttendee().getId());
        assertEquals("Grace", resp.getAttendee().getFirstName());
        assertEquals("grace@example.com", resp.getAttendee().getEmail());
    }
}
