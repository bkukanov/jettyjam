package org.safehaus.jettyjam.utils;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An abstract base class for JettyResources.
 */
public abstract class AbstractJettyResource implements JettyResource {
    private static final Logger LOG = LoggerFactory.getLogger( AbstractJettyResource.class );

    protected final Class testClass;
    protected final Object testInstance;
    protected final TestMode mode;

    protected int port;
    protected URL serverUrl;
    protected String hostname;
    private Field testField;
    protected boolean secure;
    protected boolean started;
    private boolean needsPreparation = true;


    /**
     * For use with static members in a test class used with the @ClassRule annotation.
     *
     * @param testClass the testClass with this static member.
     * @param mode the test mode: INTEG or UNIT
     */
    protected AbstractJettyResource( Class testClass, TestMode mode ) {
        this.testInstance = null;
        this.testClass = testClass;
        this.mode = mode;
    }


    /**
     * For non-static test class member which requires the test class instance.
     *
     * @param testInstance the test class instance
     * @param mode the test mode: INTEG or UNIT
     */
    protected AbstractJettyResource( Object testInstance, TestMode mode ) {
        this.testInstance = testInstance;
        this.testClass = testInstance.getClass();
        this.mode = mode;
    }


    @Override
    public int getPort() {
        return port;
    }


    @Override
    public URL getServerUrl() {
        return serverUrl;
    }


    @Override
    public TestMode getMode() {
        return mode;
    }


    @Override
    public String getHostname() {
        return hostname;
    }


    @Override
    public Field getTestField() {
        return testField;
    }


    @Override
    public boolean isSecure() {
        return secure;
    }


    @Override
    public TestParams newTestParams() {
        if ( ! started ) {
            throw new IllegalStateException( "This JettyUnitResource not started." );
        }

        return new TestParams( this );
    }


    @Override
    public TestParams newTestParams( final Map<String, String> queryParams ) {
        if ( ! started ) {
            throw new IllegalStateException( "This JettyUnitResource not started." );
        }

        return new TestParams( this, queryParams );
    }


    @Override
    public boolean isStarted() {
        return started;
    }


    public void start( Description description ) throws Exception {
        if ( needsPreparation ) {
            prepare();
        }
    }


    protected boolean isPreparationNeeded() {
        return needsPreparation;
    }


    protected void prepare() {
        try {
            testField = findFieldInTest();
        }
        catch ( IllegalAccessException e ) {
            throw new IllegalStateException( "Access modifier must be public." );
        }

        if ( testField == null ) {
            throw new RuntimeException( "Could not bind the testField" );
        }

        needsPreparation = false;
    }


    /**
     * Finds the JettyResource field (static or non-static) who's value is equal to this JettyUnitResource.
     *
     * @return the field in the testClass for this JettyUnitResource
     * @throws IllegalAccessException if there are access modifier issues
     */
    private Field findFieldInTest() throws IllegalAccessException {
        for ( Field field : testClass.getDeclaredFields() ) {
            LOG.debug( "Looking at {} field of {} test class", field.getName(), testClass );

            if ( JettyResource.class.isAssignableFrom( field.getType() ) ) {
                LOG.debug( "Found JettyResource for {} field of {} test class", field.getName(), testClass );
                field.setAccessible( true );

                if ( testInstance == null && ! Modifier.isStatic( field.getModifiers() ) ) {
                    throw new IllegalStateException( "A test object instance constructor argument must be provided, " +
                            "for a non-static " + JettyUnitResource.class + " member." );
                }

                if ( testInstance != null && Modifier.isStatic( field.getModifiers() ) ) {
                    throw new IllegalStateException( "The Class constructor argument for the test must be provided, " +
                            "for static " + JettyUnitResource.class + " members." );
                }

                Object obj;
                if ( Modifier.isStatic( field.getModifiers() ) ) {
                    obj = field.get( null );
                }
                else {
                    obj = field.get( testInstance );
                }

                if ( obj == null ) {
                    String msg = "Field " + field.getName() + " in test class " + testClass + " is null.";
                    LOG.error( msg );
                    throw new RuntimeException( msg );
                }

                if ( obj == this ) {
                    return field;
                }
            }
        }

        return null;
    }


    @Override
    public final Statement apply( final Statement base, final Description description ) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                prepare();
                start( description );
                try {
                    base.evaluate();
                }
                finally {
                    stop( description );
                }
            }
        };
    }
}
