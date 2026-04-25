package com.eams.users.infrastructure.persistence;

import com.eams.users.domain.Student;
import com.eams.users.domain.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del puerto de salida StudentRepository (AD-03).
 */
@Repository
@RequiredArgsConstructor
public class JpaStudentRepository implements StudentRepository {

    private final SpringDataStudentRepository spring;

    @Override
    public Student save(Student student) {
        return spring.save(student);
    }

    @Override
    public Optional<Student> findById(UUID studentId) {
        return spring.findById(studentId);
    }

    @Override
    public List<Student> findByGuardianId(UUID guardianId) {
        return spring.findByGuardianId(guardianId);
    }

    @Override
    public List<Student> findByGuardianIdAndInstitutionId(UUID guardianId, UUID institutionId) {
        return spring.findByGuardianIdAndInstitutionId(guardianId, institutionId);
    }

    @Override
    public boolean existsByGuardianIdAndFirstNameAndLastName(UUID guardianId,
                                                             String firstName,
                                                             String lastName) {
        return spring.existsByGuardianIdAndFirstNameAndLastName(guardianId, firstName, lastName);
    }
}
