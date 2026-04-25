package com.eams.functional;

import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber Test Runner
 *
 * Ejecuta todos los .feature files en src/test/resources/features/
 * Configuración via cucumber.properties
 *
 * Comando:
 *   mvn test -Dtest=CucumberRunner
 */
@Suite
@SelectClasspathResource("features")
public class CucumberRunner {
}
