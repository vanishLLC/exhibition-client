/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */


package exhibition.util.security;
/*
 * @(#)MyJCE.java	1.4 02/01/17
 *
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * -Redistributions of source code must retain the above copyright
 * notice, this  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of Oracle or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF  OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class MyJCE/* extends Provider*/ {
//
//    // Flag for avoiding unnecessary self-integrity checking.
//    private static boolean verifiedSelfIntegrity = false;
//
//    // Provider's signing cert which is used to sign the jar.
//    private static X509Certificate providerCert = null;
//
//    public MyJCE() {
//        // First, register provider name, version and description.
//        super("MyJCE", 1.0, "sample provider which supports nothing");
//        // Set up the provider properties here
//        // For examples, reference the Appendix A and B of
//        // JCE "How to Implement a Provider" Guide.
//        //
//        //    ...
//        //
//    }
//
//    /**
//     * Perform self-integrity checking. Call this method in all
//     * the constructors of your SPI implementation classes.
//     * NOTE: The following implementation assumes that all
//     * your provider implementation is packaged inside ONE jar.
//     */
//    public static synchronized boolean selfIntegrityChecking() {
//        if (verifiedSelfIntegrity) {
//            return true;
//        }
//
//        URL providerURL = AccessController.doPrivileged(
//                new PrivilegedAction<URL>() {
//                    public URL run() {
//                        CodeSource cs = MyJCE.class.getProtectionDomain().
//                                getCodeSource();
//                        return cs.getLocation();
//                    }
//                });
//
//        if (providerURL == null) {
//            return false;
//        }
//
//        // Open a connnection to the provider JAR file
//        JarVerifier jv = new JarVerifier(providerURL);
//
//        // Make sure that the provider JAR file is signed with
//        // provider's own signing certificate.
//        try {
//            if (providerCert == null) {
//                providerCert = setupProviderCert();
//            }
//            jv.verify(providerCert);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        verifiedSelfIntegrity = true;
//        return true;
//    }
//
//    /*
//     * Set up 'providerCert' with the certificate bytes.
//     */
//    private static X509Certificate setupProviderCert()
//            throws IOException, CertificateException {
//        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//        ByteArrayInputStream inStream = new ByteArrayInputStream(
//                bytesOfProviderCert);
//        X509Certificate cert = (X509Certificate)
//                cf.generateCertificate(inStream);
//        inStream.close();
//        return cert;
//    }
//
//    static class JarVerifier {
//
//        private URL jarURL = null;
//        private JarFile jarFile = null;
//
//        JarVerifier(URL jarURL) {
//            this.jarURL = jarURL;
//        }
//
//        /**
//         * Retrive the jar file from the specified url.
//         */
//        private JarFile retrieveJarFileFromURL(URL url)
//                throws PrivilegedActionException, MalformedURLException {
//            JarFile jf = null;
//
//            // Prep the url with the appropriate protocol.
//            jarURL =
//                    url.getProtocol().equalsIgnoreCase("jar") ?
//                            url :
//                            new URL("jar:" + url.toString() + "!/");
//            // Retrieve the jar file using JarURLConnection
//            jf = AccessController.doPrivileged(
//                    new PrivilegedExceptionAction<JarFile>() {
//                        public JarFile run() throws Exception {
//                            JarURLConnection conn =
//                                    (JarURLConnection) jarURL.openConnection();
//                            // Always get a fresh copy, so we don't have to
//                            // worry about the stale file handle when the
//                            // cached jar is closed by some other application.
//                            conn.setUseCaches(false);
//                            return conn.getJarFile();
//                        }
//                    });
//            return jf;
//        }
//
//        /**
//         * First, retrieve the jar file from the URL passed in constructor.
//         * Then, compare it to the expected X509Certificate.
//         * If everything went well and the certificates are the same, no
//         * exception is thrown.
//         */
//        public void verify(X509Certificate targetCert)
//                throws IOException {
//            // Sanity checking
//            if (targetCert == null) {
//                throw new SecurityException("Provider certificate is invalid");
//            }
//
//            try {
//                if (jarFile == null) {
//                    jarFile = retrieveJarFileFromURL(jarURL);
//                }
//            } catch (Exception ex) {
//                SecurityException se = new SecurityException();
//                se.initCause(ex);
//                throw se;
//            }
//
//            Vector<JarEntry> entriesVec = new Vector<JarEntry>();
//
//            // Ensure the jar file is signed.
//            Manifest man = jarFile.getManifest();
//            if (man == null) {
//                throw new SecurityException("The provider is not signed");
//            }
//
//            // Ensure all the entries' signatures verify correctly
//            byte[] buffer = new byte[8192];
//            Enumeration entries = jarFile.entries();
//
//            while (entries.hasMoreElements()) {
//                JarEntry je = (JarEntry) entries.nextElement();
//
//                // Skip directories.
//                if (je.isDirectory()) continue;
//                entriesVec.addElement(je);
//                InputStream is = jarFile.getInputStream(je);
//
//                // Read in each jar entry. A security exception will
//                // be thrown if a signature/digest check fails.
//                int n;
//                while ((n = is.read(buffer, 0, buffer.length)) != -1) {
//                    // Don't care
//                }
//                is.close();
//            }
//
//            // Get the list of signer certificates
//            Enumeration e = entriesVec.elements();
//
//            while (e.hasMoreElements()) {
//                JarEntry je = (JarEntry) e.nextElement();
//
//                // Every file must be signed except files in META-INF.
//                Certificate[] certs = je.getCertificates();
//                if ((certs == null) || (certs.length == 0)) {
//                    if (!je.getName().startsWith("META-INF"))
//                        throw new SecurityException("The provider " +
//                                "has unsigned " +
//                                "class files.");
//                } else {
//                    // Check whether the file is signed by the expected
//                    // signer. The jar may be signed by multiple signers.
//                    // See if one of the signers is 'targetCert'.
//                    int startIndex = 0;
//                    X509Certificate[] certChain;
//                    boolean signedAsExpected = false;
//
//                    while ((certChain = getAChain(certs, startIndex)) != null) {
//                        if (certChain[0].equals(targetCert)) {
//                            // Stop since one trusted signer is found.
//                            signedAsExpected = true;
//                            break;
//                        }
//                        // Proceed to the next chain.
//                        startIndex += certChain.length;
//                    }
//
//                    if (!signedAsExpected) {
//                        throw new SecurityException("The provider " +
//                                "is not signed by a " +
//                                "trusted signer");
//                    }
//                }
//            }
//        }
//
//        /**
//         * Extracts ONE certificate chain from the specified certificate array
//         * which may contain multiple certificate chains, starting from index
//         * 'startIndex'.
//         */
//        private static X509Certificate[] getAChain(Certificate[] certs,
//                                                   int startIndex) {
//            if (startIndex > certs.length - 1)
//                return null;
//
//            int i;
//            // Keep going until the next certificate is not the
//            // issuer of this certificate.
//            for (i = startIndex; i < certs.length - 1; i++) {
//                if (!((X509Certificate)certs[i + 1]).getSubjectDN().
//                        equals(((X509Certificate)certs[i]).getIssuerDN())) {
//                    break;
//                }
//            }
//            // Construct and return the found certificate chain.
//            int certChainSize = (i-startIndex) + 1;
//            X509Certificate[] ret = new X509Certificate[certChainSize];
//            for (int j = 0; j < certChainSize; j++ ) {
//                ret[j] = (X509Certificate) certs[startIndex + j];
//            }
//            return ret;
//        }
//
//        // Close the jar file once this object is no longer needed.
//        protected void finalize() throws Throwable {
//            jarFile.close();
//        }
//    }
}
