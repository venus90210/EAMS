package com.eams.activities.application;

import com.eams.activities.domain.*;
import com.eams.shared.exception.DomainException;
import com.eams.shared.tenant.TenantContext;
import com.eams.shared.tenant.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock private ActivityRepository activityRepository;
    @Mock private ActivityCachePort cachePort;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ActivityService service;
    private final UUID institutionId = UUID.randomUUID();
    private final UUID activityId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ActivityService(activityRepository, cachePort, eventPublisher);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_asAdmin_succeeds() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        Activity activity = Activity.create("Futbol", "Futbol Sub-12", 10,
                Schedule.create(DayOfWeek.MONDAY, LocalTime.of(14, 30),
                               LocalTime.of(16, 0), "Cancha 1"),
                institutionId);
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        Activity result = service.create(activity);

        assertThat(result.getName()).isEqualTo("Futbol");
        assertThat(result.getStatus()).isEqualTo(ActivityStatus.DRAFT);
        assertThat(result.getTotalSpots()).isEqualTo(10);
        assertThat(result.getAvailableSpots()).isEqualTo(10);
    }

    @Test
    void create_asGuardian_throwsForbidden() {
        TenantContextHolder.set(new TenantContext(institutionId, "GUARDIAN"));
        Activity activity = Activity.create("Futbol", null, 10, null, institutionId);

        assertThatThrownBy(() -> service.create(activity))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    // ── publish ──────────────────────────────────────────────────────────────

    @Test
    void publish_draftToPublished_succeeds() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        Activity activity = Activity.create("Futbol", null, 10, null, institutionId);
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));

        Activity result = service.publish(activityId);

        assertThat(result.getStatus()).isEqualTo(ActivityStatus.PUBLISHED);
        verify(cachePort).invalidate(activityId);
    }

    @Test
    void publish_publishedToPublished_throwsConflict() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        Activity activity = Activity.create("Futbol", null, 10, null, institutionId);
        activity.transitionTo(ActivityStatus.PUBLISHED);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> service.publish(activityId))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException de = (DomainException) ex;
                    assertThat(de.getErrorCode()).isEqualTo("INVALID_STATUS_TRANSITION");
                    assertThat(de.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    @Test
    void publish_asTeacher_throwsForbidden() {
        TenantContextHolder.set(new TenantContext(institutionId, "TEACHER"));
        Activity activity = Activity.create("Futbol", null, 10, null, institutionId);
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> service.publish(activityId))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    // ── updateStatus ─────────────────────────────────────────────────────────

    @Test
    void updateStatus_publishedToDisabled_succeeds() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        Activity activity = Activity.create("Futbol", null, 10, null, institutionId);
        activity.transitionTo(ActivityStatus.PUBLISHED);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));

        Activity result = service.updateStatus(activityId, ActivityStatus.DISABLED);

        assertThat(result.getStatus()).isEqualTo(ActivityStatus.DISABLED);
        verify(cachePort).invalidate(activityId);
    }

    @Test
    void updateStatus_disabledToPublished_succeeds() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        Activity activity = Activity.create("Futbol", null, 10, null, institutionId);
        activity.transitionTo(ActivityStatus.PUBLISHED);
        activity.transitionTo(ActivityStatus.DISABLED);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));

        Activity result = service.updateStatus(activityId, ActivityStatus.PUBLISHED);

        assertThat(result.getStatus()).isEqualTo(ActivityStatus.PUBLISHED);
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_asAdmin_changesTotalSpots() {
        TenantContextHolder.set(new TenantContext(institutionId, "ADMIN"));
        Activity activity = Activity.create("Futbol", "Desc", 10, null, institutionId);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(i -> i.getArgument(0));

        Activity result = service.update(activityId, "Futbol 2", "Desc 2", 15);

        assertThat(result.getName()).isEqualTo("Futbol 2");
        assertThat(result.getDescription()).isEqualTo("Desc 2");
        assertThat(result.getTotalSpots()).isEqualTo(15);
        verify(cachePort).invalidate(activityId);
    }

    @Test
    void update_asTeacher_changesTotalSpots_throwsForbidden() {
        TenantContextHolder.set(new TenantContext(institutionId, "TEACHER"));
        Activity activity = Activity.create("Futbol", null, 10, null, institutionId);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> service.update(activityId, null, null, 15))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException de = (DomainException) ex;
                    assertThat(de.getErrorCode()).isEqualTo("INSUFFICIENT_ROLE");
                    assertThat(de.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                });
    }

    @Test
    void update_wrongInstitution_throwsForbidden() {
        UUID otherInstitution = UUID.randomUUID();
        TenantContextHolder.set(new TenantContext(otherInstitution, "ADMIN"));
        Activity activity = Activity.create("Futbol", null, 10, null, institutionId);

        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> service.update(activityId, "X", null, null))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    // ── listForRole ──────────────────────────────────────────────────────────

    @Test
    void listForRole_guardian_seesOnlyPublished() {
        TenantContextHolder.set(new TenantContext(institutionId, "GUARDIAN"));
        Activity published = Activity.create("Futbol", null, 10, null, institutionId);
        published.transitionTo(ActivityStatus.PUBLISHED);

        when(activityRepository.findByInstitutionId(institutionId, ActivityStatus.PUBLISHED))
                .thenReturn(List.of(published));

        List<Activity> result = service.listForRole(institutionId, ActivityStatus.PUBLISHED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(ActivityStatus.PUBLISHED);
    }

    @Test
    void listForRole_teacher_seesAll() {
        TenantContextHolder.set(new TenantContext(institutionId, "TEACHER"));
        Activity draft = Activity.create("Futbol", null, 10, null, institutionId);
        Activity published = Activity.create("Voley", null, 8, null, institutionId);
        published.transitionTo(ActivityStatus.PUBLISHED);

        when(activityRepository.findByInstitutionId(institutionId, null))
                .thenReturn(List.of(draft, published));

        List<Activity> result = service.listForRole(institutionId, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void listForRole_superadmin_seesOtherInstitution() {
        UUID otherInstitution = UUID.randomUUID();
        TenantContextHolder.set(new TenantContext(null, "SUPERADMIN"));
        Activity activity = Activity.create("Futbol", null, 10, null, otherInstitution);

        when(activityRepository.findByInstitutionId(otherInstitution, null))
                .thenReturn(List.of(activity));

        List<Activity> result = service.listForRole(otherInstitution, null);

        assertThat(result).hasSize(1);
    }

    // ── getAvailableSpots ────────────────────────────────────────────────────

    @Test
    void getAvailableSpots_fromCache_returnsCached() {
        when(cachePort.getAvailableSpots(activityId)).thenReturn(Optional.of(5));

        Integer spots = service.getAvailableSpots(activityId);

        assertThat(spots).isEqualTo(5);
        verify(activityRepository, never()).findById(any());
    }

    @Test
    void getAvailableSpots_cacheEmpty_fetchesFromDb() {
        Activity activity = Activity.create("Futbol", null, 10, null, institutionId);
        when(cachePort.getAvailableSpots(activityId)).thenReturn(Optional.empty());
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));

        Integer spots = service.getAvailableSpots(activityId);

        assertThat(spots).isEqualTo(10);
        verify(cachePort).setAvailableSpots(activityId, 10);
    }
}
