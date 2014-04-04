package org.safehaus.jettyjam.utils;


/**
 * Various kinds of test modes. The web testing resources and supporting classes
 * will automatically inject this property as a query parameter with the value of
 * the mode as well as inject that into the system properties when launching
 * externally.
 */
public enum TestMode {
    INTEG, UNIT, UNDEFINED;

    public static final String TEST_MODE_PROPERTY = "test.mode";
}
