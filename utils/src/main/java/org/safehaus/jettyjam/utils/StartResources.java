package org.safehaus.jettyjam.utils;


import org.eclipse.jetty.util.MultiException;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Several StartResources started in a particular order.
 */
public class StartResources implements StartableResource {
    private static final Logger LOG = LoggerFactory.getLogger( StartResources.class );
    private final StartableResource[] resources;
    private final long delay;
    private boolean started;


    public StartResources( long delay, StartableResource... resources ) {
        this.resources = resources;
        this.delay = delay;
    }


    public StartResources( StartableResource... resources ) {
        this.resources = resources;
        this.delay = -1;
    }


    @Override
    public Statement apply( final Statement base, final Description description ) {
        return statement( base, description );
    }


    private Statement statement( final Statement base, final Description description ) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
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


    public void stop( Description description ) throws Exception {
        stop( description, resources.length - 1 );
    }


    public void stop( Description description, int ii ) throws Exception {
        boolean throwException = false;
        MultiException exceptions = new MultiException();

        for( /* .. */; ii >= 0; ii-- ) {
            StartableResource resource = resources[ii];

            if ( ! resource.isStarted() ) {
                continue;
            }

            try {
                LOG.info( "Test class {}: stopping resource {}", description.getTestClass().getName(), resource );
                resource.stop( description );
                Thread.sleep( delay );
            }
            catch ( Exception e ) {
                throwException = true;
                LOG.warn( "Got exception while stopping resource, but will " +
                        "continue to stop all: resource = {}", resource );
                exceptions.add( e );
            }
        }

        if ( throwException ) {
            throw exceptions;
        }
    }


    public void start( Description description ) throws Exception {
        for( int ii = 0; ii < resources.length; ii++ ) {
            StartableResource resource = resources[ii];

            if ( resource.isStarted() ) {
                continue;
            }

            LOG.info( "Test class: " + description.getTestClass().getName()
                    + "Waiting {} milliseconds before starting resource {}.", delay, resource );
            Thread.sleep( delay );

            LOG.info( "Test class {}: starting resource {}", description.getTestClass().getName(), resource );

            try {
                resource.start( description );
            }
            catch ( Exception e ) {
                LOG.warn( "Got exception while starting resource at index {}: {}", ii, resource );
                LOG.warn( "Stopping everything from index {}", ii - 1 );
                stop( description, ii );
                throw e;
            }

            // in case resource starts asynchronously
            while ( ! resource.isStarted() ) {
                Thread.sleep( 250 );
            }

            LOG.info( "In test class {}, resource has started: {}", description.getTestClass().getName(), resource );
        }

        started = true;
    }


    @Override
    public boolean isStarted() {
        return started;
    }
}
