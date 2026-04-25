package com.eams.users.infrastructure.persistence;

import com.eams.users.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA — adaptador técnico interno del módulo Users.
 */
interface SpringDataStudentRepository extends JpaRepository<Student, UUID> {

    List<Student> findByGuardianId(UUID guardianId);

    List<Student> findByGuardianIdAndInstitutionId(UUID guardianId, UUID institutionId);

    boolean existsByGuardianIdAndFirstNameAndLastName(UUID guardianId,
                                                      String firstName,
                                                      String lastName);
}
