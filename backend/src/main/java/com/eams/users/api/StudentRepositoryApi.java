package com.eams.users.api;

import com.eams.users.domain.Student;
import com.eams.users.domain.StudentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public re-export of StudentRepository port.
 * Allows other modules to depend on this interface.
 */
public interface StudentRepositoryApi extends StudentRepository {
    @Override
    Student save(Student student);

    @Override
    Optional<Student> findById(UUID studentId);

    @Override
    List<Student> findByGuardianId(UUID guardianId);

    @Override
    List<Student> findByGuardianIdAndInstitutionId(UUID guardianId, UUID institutionId);

    @Override
    boolean existsByGuardianIdAndFirstNameAndLastName(UUID guardianId, String firstName, String lastName);
}
