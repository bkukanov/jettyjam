package org.safehaus.jettyjam.utils;


import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;


/**
 * A common interface across the JettyUnitResource and the JettyIntegResource.
 */
public interface JettyResource extends StartableResource {
    int getPort();

    URL getServerUrl();

    TestMode getMode();

    String getHostname();

    Field getTestField();

    boolean isSecure();

    TestParams newTestParams();

    TestParams newTestParams( Map<String, String> queryParams );
}
