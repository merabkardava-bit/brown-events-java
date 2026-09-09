package com.brownevents.app.controller;

import com.brownevents.app.entity.Conference;
import com.brownevents.app.service.ConferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConferenceController.class)
public class ConferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConferenceService conferenceService;

    @Test
    public void createConference_shouldReturn201WithDataEnvelope() throws Exception {
        Conference created = new Conference();
        created.setId(1L);
        created.setTitle("Brown Tech Summit 2025");

        when(conferenceService.createConference(any(Conference.class))).thenReturn(created);

        mockMvc.perform(post("/api/conferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Brown Tech Summit 2025\",\"status\":\"UPCOMING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Brown Tech Summit 2025"));
    }

    @Test
    public void getAllConferences_withIsoDateParams_shouldReturn200() throws Exception {
        when(conferenceService.getAllConferences(
                any(Pageable.class), isNull(), any(LocalDate.class), any(LocalDate.class), isNull()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/conferences")
                        .param("from", "2026-09-22")
                        .param("to", "2026-12-31"))
                .andExpect(status().isOk());
    }
}
