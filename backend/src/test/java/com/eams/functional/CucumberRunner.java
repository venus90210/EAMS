package com.eams.functional;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber Test Runner
 *
 * Ejecuta todos los .feature files en src/test/resources/features/
 *
 * Comando:
 *   mvn test -Dtest=CucumberRunner
 */
@Suite
@ConfigurationParameter(
    key = Constants.FEATURES_PROPERTY_NAME,
    value = "classpath:features"
)
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = "com.eams.functional"
)
@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "pretty, html:target/cucumber-report.html"
)
public class CucumberRunner {
}
