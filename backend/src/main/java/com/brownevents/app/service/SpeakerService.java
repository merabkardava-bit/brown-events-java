package com.brownevents.app.service;

import com.brownevents.app.entity.Speaker;
import com.brownevents.app.repository.SpeakerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpeakerService {

    private final SpeakerRepository speakerRepository;

    public SpeakerService(SpeakerRepository speakerRepository) {
        this.speakerRepository = speakerRepository;
    }

    public List<Speaker> getAllSpeakers() {
        return speakerRepository.findAll();
    }

    public Speaker createSpeaker(Speaker speaker) {
        return speakerRepository.save(speaker);
    }
}
