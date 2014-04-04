package org.safehaus.jettyjam.utils;


import java.net.URL;
import java.util.Map;

import org.junit.rules.TestRule;


/**
 * A common interface across the JettyUnitResource and the JettyIntegResource.
 */
public interface JettyResource extends TestRule {
    int getPort();

    URL getServerUrl();

    TestMode getMode();

    @SuppressWarnings( "UnusedDeclaration" )
    boolean isStarted();

    String getHostname();

    boolean isSecure();

    TestParams newTestParams();

    TestParams newTestParams( Map<String, String> queryParams );
}
