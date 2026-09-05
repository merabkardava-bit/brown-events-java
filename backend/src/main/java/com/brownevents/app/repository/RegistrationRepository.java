package com.brownevents.app.repository;

import com.brownevents.app.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByConferenceId(Long conferenceId);

    boolean existsByIdAndConferenceId(Long id, Long conferenceId);
}
