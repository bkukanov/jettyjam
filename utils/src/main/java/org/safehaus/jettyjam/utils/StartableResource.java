package org.safehaus.jettyjam.utils;


import org.junit.rules.TestRule;
import org.junit.runner.Description;


/**
 * A TestRule resource that can be started and stopped, and queried.
 */
public interface StartableResource extends TestRule {
    /**
     * Gets whether or not this StartableResource has started or not.
     *
     * @return true if started, false if stopped
     */
    boolean isStarted();

    /**
     * Stops the StartableResource.
     *
     * @param description the test description
     * @throws Exception if something goes wrong while stopping
     */
    void stop( Description description ) throws Exception;

    /**
     * Starts the StartableResource.
     *
     * @param description the test description
     * @throws Exception if something goes wrong while starting
     */
    void start( Description description ) throws Exception;
}
