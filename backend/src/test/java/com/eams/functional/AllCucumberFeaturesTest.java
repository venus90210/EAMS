package com.eams.functional;

import com.eams.BaseIntegrationTest;
import io.cucumber.junit.platform.engine.Constants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;

/**
 * All Cucumber Features Test - Standalone
 *
 * Ejecuta todas las 43 escenarios Gherkin con step definitions BD-integrados
 *
 * Alternativa a CucumberRunner que evita problemas de discovery con JUnit Platform Suite
 *
 * Comando:
 *   mvn test -Pintegration -Dtest=AllCucumberFeaturesTest
 */
@DisplayName("All Cucumber Features (43 scenarios)")
public class AllCucumberFeaturesTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Execute all 43 Cucumber scenarios")
    void runAllCucumberFeatures() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(
                selectClasspathResource("features/F1-inscripcion.feature"),
                selectClasspathResource("features/F2-asistencia.feature"),
                selectClasspathResource("features/F3-consulta-offline.feature"),
                selectClasspathResource("features/F4-autenticacion.feature"),
                selectClasspathResource("features/F5-estado-actividad.feature")
            )
            .configurationParameter(Constants.GLUE_PROPERTY_NAME, "com.eams.functional")
            .configurationParameter(Constants.PLUGIN_PROPERTY_NAME, "pretty,html:target/cucumber-report.html")
            .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        long testsFailed = listener.getSummary().getTestsFailedCount();
        System.out.println("✅ Cucumber scenarios executed");
        System.out.println("📊 Found: " + listener.getSummary().getTestsFoundCount());
        System.out.println("✓ Passed: " + (listener.getSummary().getTestsFoundCount() - testsFailed));
        System.out.println("✗ Failed: " + testsFailed);

        if (testsFailed > 0) {
            throw new AssertionError(testsFailed + " Cucumber scenarios failed");
        }
    }
}
