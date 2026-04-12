package com.eams.activities.infrastructure.persistence;

import com.eams.activities.domain.Activity;
import com.eams.activities.domain.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA — adaptador técnico interno del módulo Activities.
 */
interface SpringDataActivityRepository extends JpaRepository<Activity, UUID> {

    @Query("SELECT a FROM Activity a WHERE a.institutionId = :institutionId AND a.status = :status")
    List<Activity> findByInstitutionIdAndStatus(
            @Param("institutionId") UUID institutionId,
            @Param("status") ActivityStatus status);

    @Query("SELECT a FROM Activity a WHERE a.institutionId = :institutionId")
    List<Activity> findByInstitutionId(@Param("institutionId") UUID institutionId);
}
