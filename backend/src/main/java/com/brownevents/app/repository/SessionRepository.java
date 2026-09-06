package com.brownevents.app.repository;

import com.brownevents.app.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // BEVJ-101: JOIN FETCH replaces the old derived query (findByConferenceId).
    // Previously, all three @ManyToOne associations defaulted to EAGER, so
    // Hibernate issued 1 + 3N queries for a list of N sessions (N SELECTs each
    // for conference, speaker, and room). This single JPQL query loads the full
    // object graph in one round-trip. LEFT JOIN FETCH is used for speaker and
    // room because either can be null; JOIN FETCH is safe for conference since
    // we are already filtering by it.
    @Query("SELECT s FROM Session s " +
           "JOIN FETCH s.conference " +
           "LEFT JOIN FETCH s.speaker " +
           "LEFT JOIN FETCH s.room " +
           "WHERE s.conference.id = :conferenceId")
    List<Session> findByConferenceId(@Param("conferenceId") Long conferenceId);
}
