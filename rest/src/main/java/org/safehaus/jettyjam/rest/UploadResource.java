package org.safehaus.jettyjam.rest;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.sun.jersey.multipart.FormDataParam;


/** REST operation to upload (a.k.a. deploy) a project war file. */
@Singleton
@Produces( MediaType.APPLICATION_JSON )
@Path( UploadResource.ENDPOINT_URL )
public class UploadResource {
    public final static String BUILD_DIR_KEY = "project.build.directory";
    public final static String ENDPOINT_URL = "/rest/upload";
    private final static Logger LOG = LoggerFactory.getLogger( UploadResource.class );
    public static final String FILENAME_PARAM = "file";
    public static final String CONTENT = "content";


    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response upload( @FormDataParam( FILENAME_PARAM ) String filename,
                            @FormDataParam( CONTENT ) InputStream in )
    {
        File file = new File( getDownloadDir(), filename );
        writeToFile( in, file.getAbsolutePath() );
        return Response.status( Response.Status.CREATED ).entity( file.getAbsoluteFile() ).build();
    }


    public static String getDownloadDir() {
        InputStream in = UploadResource.class.getResourceAsStream( "/UploadResource.properties" );
        if ( in != null ) {
            Properties properties = new Properties();
            try {
                properties.load( in );
            }
            catch ( IOException e ) {
                LOG.warn( "Failed to read UploadResource.properties" );
                return "target";
            }
            return properties.getProperty( BUILD_DIR_KEY );
        }

        LOG.info( "UploadResource.properties does not exist on the CP: returning \"target\" for build directory." );
        return "target";
    }


    private void writeToFile( InputStream in, String fileLocation ) {
        LOG.info( "writing uploaded file to fileLocation {}", fileLocation );

        OutputStream out = null;

        try {
            int read;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream( fileLocation );

            while ( ( read = in.read( bytes ) ) != -1 ) {
                out.write( bytes, 0, read );
            }
            out.flush();
        }
        catch ( IOException e ) {
            LOG.error( "Failed to write out file: " + fileLocation, e );
        }
        finally {
            if ( out != null ) {
                try {
                    out.close();
                }
                catch ( IOException e ) {
                    LOG.error( "Failed while trying to close output stream for {}", fileLocation );
                }
            }
        }
    }
}
