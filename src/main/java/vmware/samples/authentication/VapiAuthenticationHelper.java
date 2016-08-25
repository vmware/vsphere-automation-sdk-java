/*
 * *******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.authentication;

import java.security.KeyStore;

import com.vmware.cis.Session;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vapi.cis.authn.ProtocolFactory;
import com.vmware.vapi.cis.authn.SecurityContextFactory;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.core.ExecutionContext.SecurityContext;
import com.vmware.vapi.protocol.ProtocolConnection;
import com.vmware.vapi.saml.SamlToken;
import com.vmware.vapi.security.SessionSecurityContext;

import vmware.samples.common.SslUtil;

/**
 * Helper class which provides methods for
 * 1. login/logout using username, password authentication.
 * 2. getting authenticated stubs for client-side interfaces.
 */
public class VapiAuthenticationHelper {
    private Session sessionSvc;
    private StubFactory stubFactory;
    public static final String VAPI_PATH = "/api";

    /**
     * Creates a session with the server using username and password
     * <p>
     * Note: This method uses https for communication but doesn't verify
     * the certificate from the server. Circumventing SSL is unsafe and should
     * not be used with production code. This is ONLY FOR THE PURPOSE OF
     * DEVELOPMENT ENVIRONMENT.
     * </p>
     *
     * @param server hostname or ip address of the server to log in to
     * @param username username for login
     * @param password password for login
     *
     * @return the stub configuration configured with an authenticated session
     * @throws Exception if there is an existing session
     */
    public StubConfiguration loginByUsernameAndPassword(
        String server, String username, String password) throws Exception {
        if(this.sessionSvc != null) {
            throw new Exception("Session already created");
        }

        this.stubFactory = createApiStubFactory(server);

        // Create a security context for username/password authentication
        SecurityContext securityContext =
                SecurityContextFactory.createUserPassSecurityContext(
                    username, password.toCharArray());

        // Create a stub configuration with username/password security context
        StubConfiguration stubConfig = new StubConfiguration(securityContext);

        // Create a session stub using the stub configuration.
        Session session =
                this.stubFactory.createStub(Session.class, stubConfig);

        // Login and create a session
        char[] sessionId = session.create();

        // Initialize a session security context from the generated session id
        SessionSecurityContext sessionSecurityContext =
                new SessionSecurityContext(sessionId);

        // Update the stub configuration to use the session id
        stubConfig.setSecurityContext(sessionSecurityContext);

        /*
         * Create a stub for the session service using the authenticated
         * session
         */
        this.sessionSvc =
                this.stubFactory.createStub(Session.class, stubConfig);

        return stubConfig;
    }

    /**
     * Creates a session with the server using SAML Bearer Token
     * <p>
     * Note: This method uses https for communication but doesn't verify
     * the certificate from the server. Circumventing SSL is unsafe and should
     * not be used with production code. This is ONLY FOR THE PURPOSE OF
     * DEVELOPMENT ENVIRONMENT.
     * </p>
     *
     * @param server hostname or ip address of the server to log in to
     * @param samlBearerToken a SAML bearer token
     *
     * @return the stub configuration configured with an authenticated session
     * @throws Exception
     */
    public StubConfiguration loginBySamlBearerToken(
        String server, SamlToken samlBearerToken) throws Exception {
        if(this.sessionSvc != null) {
            throw new Exception("Session already created");
        }

        this.stubFactory = createApiStubFactory(server);

        // Create a SAML security context using SAML bearer token
        SecurityContext samlSecurityContext =
                SecurityContextFactory.createSamlSecurityContext(
                    samlBearerToken, null);

        // Create a stub configuration with SAML security context
        StubConfiguration stubConfig =
                new StubConfiguration(samlSecurityContext);

        // Create a session stub using the stub configuration.
        Session session =
                this.stubFactory.createStub(Session.class, stubConfig);

        // Login and create a session
        char[] sessionId = session.create();

        // Initialize a session security context from the generated session id
        SessionSecurityContext sessionSecurityContext =
                new SessionSecurityContext(sessionId);

        // Update the stub configuration to use the session id
        stubConfig.setSecurityContext(sessionSecurityContext);

        /*
         * Create a stub for the session service using the authenticated
         * session
         */
        this.sessionSvc =
                this.stubFactory.createStub(Session.class, stubConfig);

        return stubConfig;
    }


    /**
     * Logs out of the current session.
     */
    public void logout() {
        if (this.sessionSvc != null) {
            this.sessionSvc.delete();
        }
    }

    /*
     * Connects to the server using https protocol and returns the factory
     * instance that can be used for creating the client side stubs.
     *
     * Note: This method trusts the https certificate and doesn't
     * verify it. Circumventing SSL is unsafe and should not be used with
     * production code. This is ONLY FOR THE PURPOSE OF DEVELOPMENT ENVIRONMENT
     *
     * @param server hostname or ip address of the server
     * @return factory for the client side stubs
     */
    private StubFactory createApiStubFactory(String server) {
        // Create a https connection with the vapi url
        ProtocolFactory pf = new ProtocolFactory();
        String apiUrl = "https://" + server + VAPI_PATH;

        /*
         * Retrieve the SSL certificate chain of the server and store the
         * root certificate into an in-memory trust store.
         * Note: Below code is to be used ONLY IN DEVELOPMENT ENVIRONMENTS.
         * Circumventing SSL trust is unsafe and should not be used in
         * production software.
         */
        KeyStore trustStore = SslUtil.createTrustStoreForServer(apiUrl);

        // Get a connection to the vapi url
        ProtocolConnection connection = pf.getConnection("http",
                                                         apiUrl,
                                                         trustStore);

        // Initialize the stub factory with the api provider
        ApiProvider provider = connection.getApiProvider();
        StubFactory stubFactory = new StubFactory(provider);
        return stubFactory;
    }

    /**
     * Returns the stub factory for the api endpoint
     *
     * @return
     */
    public StubFactory getStubFactory() {
        return this.stubFactory;
    }
}
