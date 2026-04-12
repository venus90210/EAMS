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
 */
@Tag("unit")
class ArchitectureTest {

    private final ApplicationModules modules = EamsApplication.modules();

    @Test
    void modulesAreCompliant() {
        // Verifica que ningún módulo accede a los paquetes internos de otro
        modules.verify();
    }

    @Test
    void generateModuleDocumentation() {
        // Genera documentación de la arquitectura modular en target/modulith-docs/
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
