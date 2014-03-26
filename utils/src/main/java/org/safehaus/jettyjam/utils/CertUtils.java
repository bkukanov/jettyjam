package org.safehaus.jettyjam.utils;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * General useful cert utility methods.
 */
public class CertUtils {

    static {
        System.setProperty( "javax.net.ssl.trustStore", "jssecacerts" );
    }

    private static final Logger LOG = LoggerFactory.getLogger( CertUtils.class );
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
    private static final Set<String> trustedHosts = new HashSet<String>();
    private static final Object lock = new Object();
    private static File certStore;


    public static void preparations( String hostname, int port ) {
        if ( ! isTrusted( hostname ) ) {
            try {
                installHostKey( null, hostname );
            }
            catch ( Exception e ) {
                LOG.error( "Failed to install certificate for host " + hostname, e );

                try {
                    CertUtils.installCert( hostname, port, null );
                }
                catch ( Exception e2 ) {
                    LOG.error( "Failed to get certificate from server {} on port {}: dumping stack trace!",
                            hostname, port );
                    e.printStackTrace();
                }
            }
        }
    }


    public static boolean isTrusted( String hostname ) {
        synchronized ( lock ) {
            return trustedHosts.contains( hostname );
        }
    }


    public static boolean isStoreInitialized() {
        return certStore != null;
    }


    public static void installHostKey( char[] passphrase, String... hostnames ) throws Exception {
        if ( passphrase == null ) {
            passphrase = "changeit".toCharArray();
        }

        File file;
        if ( certStore != null ) {
            file = certStore;
        }
        else {
            file = new File( "jssecacerts" );
        }

        if ( ! file.isFile() ) {
            char SEP = File.separatorChar;
            File dir = new File( System.getProperty( "java.home" ) + SEP + "lib" + SEP + "security" );
            file = new File( dir, "jssecacerts" );
            if ( !file.isFile() ) {
                file = new File( dir, "cacerts" );
            }
        }

        certStore = file;

        LOG.debug( "Loading KeyStore {}", file );

        InputStream in = new FileInputStream( file );
        KeyStore ks = KeyStore.getInstance( KeyStore.getDefaultType() );
        ks.load( in, passphrase );
        in.close();

        CertificateFactory cf = CertificateFactory.getInstance( "X.509" );
        Certificate cert =  cf.generateCertificate( getCertificateStream() );

        for ( String hostname : hostnames ) {
            ks.setCertificateEntry( hostname, cert );
            LOG.debug( "Added certificate to keystore 'jssecacerts' using alias '" + hostname + "'" );
        }

        OutputStream out = new FileOutputStream( "jssecacerts" );
        ks.store( out, passphrase );
        out.close();

        synchronized ( lock ) {
            Collections.addAll( trustedHosts, hostnames );
        }
    }


    private static InputStream getCertificateStream () throws IOException {
        InputStream in = CertUtils.class.getClassLoader().getResourceAsStream( "default-key.cer" );
        DataInputStream dis = new DataInputStream( in );
        byte[] bytes = new byte[ dis.available() ];
        dis.readFully( bytes );
        return new ByteArrayInputStream( bytes );
    }



    /**
     * Installs a certificate from the server into a local certificate store.
     *
     * @param host the HTTPS base server host to get the certificate from
     * @param port the port of the server
     * @param passphrase the passphrase to access/set the cert store if it does not
     * exist, defaults to "changeit" if null is provided
     * @throws Exception if something goes wrong
     */
    public static void installCert( String host, int port, char[] passphrase ) throws Exception {

        if ( passphrase == null ) {
            passphrase = "changeit".toCharArray();
        }

        File file;
        if ( certStore != null ) {
            file = certStore;
        }
        else {
            file = new File( "jssecacerts" );
        }

        if ( ! file.isFile() ) {
            char SEP = File.separatorChar;
            File dir = new File( System.getProperty( "java.home" ) + SEP + "lib" + SEP + "security" );
            file = new File( dir, "jssecacerts" );
            if ( !file.isFile() ) {
                file = new File( dir, "cacerts" );
            }
        }

        certStore = file;

        LOG.debug( "Loading KeyStore {}", file );
        InputStream in = new FileInputStream( file );
        KeyStore ks = KeyStore.getInstance( KeyStore.getDefaultType() );
        ks.load( in, passphrase );
        in.close();

        SSLContext context = SSLContext.getInstance( "TLS" );
        TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
        tmf.init( ks );
        X509TrustManager defaultTrustManager = ( X509TrustManager ) tmf.getTrustManagers()[0];
        SavingTrustManager tm = new SavingTrustManager( defaultTrustManager );
        context.init( null, new TrustManager[] { tm }, null );
        SSLSocketFactory factory = context.getSocketFactory();

        // Try to reconnect in case there are newly launched instances and they're not fully up yet
        SSLSocket socket = null;
        int trial = 0;
        boolean success = false;
        ConnectException connectException = null;
        do {
            try {
                LOG.info( "Opening connection to {}:{}", host, port );
                socket = ( SSLSocket ) factory.createSocket( host, port );
                socket.setSoTimeout( 10000 );
                success = true;
            }
            catch ( ConnectException e ) {
                connectException = e;
                Thread.sleep( 1500 );
            }
        }
        while ( !success && trial++ < 10 );

        if( !success ) {
            throw connectException;
        }

        try {
            LOG.debug( "Starting SSL handshake..." );
            socket.startHandshake();
            socket.close();
            LOG.debug( "No errors, certificate is already trusted" );
        }
        catch ( SSLException e ) {
            LOG.debug( "Cert is NOT trusted: {}", e.getMessage() );
        }

        X509Certificate[] chain = tm.chain;
        if ( chain == null ) {
            LOG.warn( "Could not obtain server certificate chain" );
            return;
        }

        LOG.debug( "Server sent " + chain.length + " certificate(s):" );
        MessageDigest sha1 = MessageDigest.getInstance( "SHA1" );
        MessageDigest md5 = MessageDigest.getInstance( "MD5" );
        for ( int i = 0; i < chain.length; i++ ) {
            X509Certificate cert = chain[i];
            LOG.debug( " " + ( i + 1 ) + " Subject " + cert.getSubjectDN() );
            LOG.debug( "   Issuer  " + cert.getIssuerDN() );
            sha1.update( cert.getEncoded() );
            LOG.debug( "   sha1    " + toHexString( sha1.digest() ) );
            md5.update( cert.getEncoded() );
            LOG.debug( "   md5     " + toHexString( md5.digest() ) );
        }

        int k = 0;

        X509Certificate cert = chain[k];
        // now just using the hostname instead of : String alias = host + "-" + ( k + 1 );
        ks.setCertificateEntry( host, cert );

        OutputStream out = new FileOutputStream( "jssecacerts" );
        ks.store( out, passphrase );
        out.close();

        LOG.debug( "cert = {}", cert );
        LOG.debug( "Added certificate to keystore 'jssecacerts' using alias '" + host + "'" );
    }


    private static String toHexString( byte[] bytes ) {
        StringBuilder sb = new StringBuilder( bytes.length * 3 );
        for ( int b : bytes ) {
            b &= 0xff;
            sb.append( HEX_DIGITS[b >> 4] );
            sb.append( HEX_DIGITS[b & 15] );
            sb.append( ' ' );
        }
        return sb.toString();
    }


    private static class SavingTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;


        SavingTrustManager( X509TrustManager tm ) {
            this.tm = tm;
        }


        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }


        public void checkClientTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
            throw new UnsupportedOperationException();
        }


        public void checkServerTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted( chain, authType );
        }
    }
}
