package org.safehaus.embedded.jetty.utils;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.jar.Attributes;


/**
 * A jar runner.
 */
public class JarRunner {

    private static final String BUNDLE_MAIN_CLASS = "Bundle-MainClass";


    public static void main( String[] args ) throws Throwable {
        invokeClass( getMainClassName(), args );
    }


    public static String getMainClassName() throws IOException {
        ProtectionDomain protectionDomain = JettyRunner.class.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URL rootUrl = codeSource.getLocation();
        if ( rootUrl.getFile().toLowerCase().endsWith( ".jar" ) ) {
            URL rootJarUrl = new URL( "jar", "", rootUrl + "!/" );
            JarURLConnection uc = ( JarURLConnection ) rootJarUrl.openConnection();
            Attributes attr = uc.getMainAttributes();
            return attr != null ? attr.getValue( BUNDLE_MAIN_CLASS ) : null;
        }
        throw new IllegalStateException( "JettyRunner is not inside a jar file" );
    }


    public static void invokeClass( String name, String[] args )
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException
    {
        final String mainMethodName = "main";
        JarClassLoader loader = new JarClassLoader( ( ( URLClassLoader ) ClassLoader.getSystemClassLoader() ).getURLs(),
                Thread.currentThread().getContextClassLoader() );
        Class clazz = loader.loadClass( name );
        Method method = clazz.getMethod( mainMethodName, new Class[] { args.getClass() } );
        method.setAccessible( true );
        int mods = method.getModifiers();
        if ( method.getReturnType() != void.class || !Modifier.isStatic( mods ) || !Modifier.isPublic( mods ) ) {
            throw new NoSuchMethodException( mainMethodName );
        }
        try {
            method.invoke( null, new Object[] { args } );
        }
        catch ( IllegalAccessException e ) {
            // This should not happen, as we have
            // disabled access checks
        }
    }


}
