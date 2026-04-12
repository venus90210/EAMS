package com.eams.users.domain;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida del módulo Users — abstracción de persistencia de estudiantes.
 */
public interface StudentRepository {

    Student save(Student student);

    List<Student> findByGuardianId(UUID guardianId);

    List<Student> findByGuardianIdAndInstitutionId(UUID guardianId, UUID institutionId);

    boolean existsByGuardianIdAndFirstNameAndLastName(UUID guardianId, String firstName, String lastName);
}
