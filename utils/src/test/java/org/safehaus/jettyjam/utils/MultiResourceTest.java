package org.safehaus.jettyjam.utils;


import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;


/**
 * Tests that many JettyResources can be run in the same test class.
 */
public class MultiResourceTest {
    private static final Logger LOG = LoggerFactory.getLogger( MultiResourceTest.class );

    @JettyContext
    @ClassRule
    public static JettyResource res0 = new JettyUnitResource( MultiResourceTest.class );

    @JettyContext
    @ClassRule
    public static JettyResource res1 = new JettyUnitResource( MultiResourceTest.class );

    @JettyContext
    @ClassRule
    public static JettyResource res2 = new JettyUnitResource( MultiResourceTest.class );


    @Test
    public void testSimple() {
        LOG.debug( "{} for res0 testField name = {}", res0.getServerUrl(), res0.getTestField().getName() );
        assertEquals( "res0", res0.getTestField().getName() );
        assertNotNull( res0 );

        LOG.debug( "{} for res1 testField name = {}", res1.getServerUrl(), res1.getTestField().getName() );
        assertEquals( "res1", res1.getTestField().getName() );
        assertNotNull( res1 );

        LOG.debug( "{} for res2 testField name = {}", res2.getServerUrl(), res2.getTestField().getName() );
        assertEquals( "res2", res2.getTestField().getName() );
        assertNotNull( res2 );
    }
}
