package org.safehaus.jettyjam.rest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

/**
 * REST operation to upload (a.k.a. deploy) a project war file.
 */
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Path(UploadResource.ENDPOINT_URL)
public class UploadResource {

    public final static String ENDPOINT_URL = "/upload";
    private final static Logger LOG = LoggerFactory.getLogger(UploadResource.class);
    public static final String FILENAME_PARAM = "file";
    public static final String CONTENT = "content";


    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response upload(
            @FormDataParam( FILENAME_PARAM ) String filename,
            @FormDataParam( CONTENT ) InputStream in )
    {
        writeToFile( in, filename );
        return Response.status( Response.Status.CREATED ).entity( filename ).build();
    }


    private void writeToFile(InputStream in, String fileLocation) {
        OutputStream out = null;

        try {
            int read;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(fileLocation);

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            LOG.error("Failed to write out file: " + fileLocation, e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.error("Failed while trying to close output stream for {}", fileLocation);
                }
            }
        }
    }
}
