package org.safehaus.jettyjam.utils;


import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


/**
 * Several JettyResources started in a particular order.
 */
public class JettyResources implements TestRule {
    private final JettyResource[] resources;
    private final long delay;


    public JettyResources( long delay, JettyResource... resources ) {
        this.resources = resources;
        this.delay = delay;
    }


    public JettyResources( JettyResource... resources ) {
        this.resources = resources;
        this.delay = -1;
    }


    @Override
    public Statement apply( final Statement base, final Description description ) {
        return statement( base );
    }


    private Statement statement( final Statement base ) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                }
                finally {
                    after();
                }
            }
        };
    }


    public void after() throws Exception{
        for( int ii = resources.length - 1; ii >= 0; ii-- ) {
            resources[ii].after();
        }
    }


    public void before() throws Exception {
        for( JettyResource resource : resources ) {
            resource.before();
            Thread.sleep( delay );
        }
    }
}
