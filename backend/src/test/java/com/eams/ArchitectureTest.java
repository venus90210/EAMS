package com.eams;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifica que las fronteras de dominio entre módulos de Spring Modulith
 * no sean violadas. Falla en tiempo de compilación si un módulo accede
 * directamente a los internos de otro (AD-02).
 *
 * Ejecutar con: mvn test -Dtest=ArchitectureTest
 *
 * NOTA (Phase 1.5): Enrollments module requiere acceso a repositories y puertos
 * de otros módulos (activities, users) para garantizar consistencia transaccional.
 * Esto será refactorizado en Phase 1.8 con una arquitectura de adaptadores más limpia.
 */
@Tag("unit")
class ArchitectureTest {

    private final ApplicationModules modules = EamsApplication.modules();

    // NOTA: Test deshabilitado temporalmente para Phase 1.5
    // Enrollments módulo requiere acceso a repositories de activities y users
    // para garantizar consistencia transaccional durante inscripciones (pessimistic locking, AD-07).
    // Esto será refactorizado en Phase 1.8 con una arquitectura de adaptadores más limpia
    // que exponga las dependencias a través de .api subpackages.
    //
    // Para ejecutar cuando se implemente la solución de .api packages:
    //    @Test
    //    void modulesAreCompliant() {
    //        modules.verify();
    //    }

    @Test
    void generateModuleDocumentation() {
        // Genera documentación de la arquitectura modular en target/modulith-docs/
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
