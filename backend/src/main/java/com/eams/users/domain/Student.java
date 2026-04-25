package com.eams.users.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entidad de dominio: estudiante vinculado a un acudiente.
 *
 * Invariantes:
 *   - institution_id obligatorio (multi-tenancy AD-08)
 *   - guardian_id referencia al User de rol GUARDIAN (validado en StudentService)
 */
@Entity
@Table(name = "students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "grade")
    private String grade;

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    @Column(name = "guardian_id", nullable = false)
    private UUID guardianId;

    // ── Factory method ──────────────────────────────────────────────────────

    public static Student create(String firstName,
                                 String lastName,
                                 String grade,
                                 UUID institutionId,
                                 UUID guardianId) {
        Student s = new Student();
        s.id            = UUID.randomUUID();
        s.firstName     = firstName.strip();
        s.lastName      = lastName.strip();
        s.grade         = grade != null ? grade.strip() : null;
        s.institutionId = institutionId;
        s.guardianId    = guardianId;
        return s;
    }
}
