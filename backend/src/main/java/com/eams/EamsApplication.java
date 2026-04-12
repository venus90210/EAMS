package com.eams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootApplication
public class EamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EamsApplication.class, args);
    }

    /**
     * Expone el modelo de módulos para inspección y documentación.
     * Spring Modulith verifica las fronteras entre módulos en tiempo de test.
     * Ver: ArchitectureTest.java
     */
    public static ApplicationModules modules() {
        return ApplicationModules.of(EamsApplication.class);
    }
}
