package com.brownevents.app.repository;

import com.brownevents.app.entity.Conference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ConferenceRepository extends JpaRepository<Conference, Long> {

    @Query(value = "SELECT c FROM Conference c WHERE " +
        "(:search IS NULL OR LOWER(c.title) LIKE :search ESCAPE '\\' OR LOWER(c.description) LIKE :search ESCAPE '\\') AND " +
        "(cast(:from as date) IS NULL OR (c.startDate IS NOT NULL AND c.startDate >= :from)) AND " +
        "(cast(:to as date) IS NULL OR (c.startDate IS NOT NULL AND c.startDate <= :to)) AND " +
        "(:status IS NULL OR UPPER(c.status) = UPPER(:status))",
        countQuery = "SELECT COUNT(c) FROM Conference c WHERE " +
        "(:search IS NULL OR LOWER(c.title) LIKE :search ESCAPE '\\' OR LOWER(c.description) LIKE :search ESCAPE '\\') AND " +
        "(cast(:from as date) IS NULL OR (c.startDate IS NOT NULL AND c.startDate >= :from)) AND " +
        "(cast(:to as date) IS NULL OR (c.startDate IS NOT NULL AND c.startDate <= :to)) AND " +
        "(:status IS NULL OR UPPER(c.status) = UPPER(:status))")
    Page<Conference> findAllFiltered(
        @Param("search") String search,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to,
        @Param("status") String status,
        Pageable pageable);
}
