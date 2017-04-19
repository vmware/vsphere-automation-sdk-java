/*
 * *******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.sso;

import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import org.oasis_open.docs.ws_sx.ws_trust._200512.LifetimeType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RenewingType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.AttributedDateTime;
import org.w3c.dom.Element;

import com.rsa.names._2009._12.product.riat.wsdl.STSService;
import com.rsa.names._2009._12.product.riat.wsdl.STSServicePortType;
import com.vmware.vapi.saml.DefaultTokenFactory;
import com.vmware.vapi.saml.SamlToken;
import com.vmware.vapi.saml.exception.InvalidTokenException;

import vmware.samples.common.SslUtil;
import vmware.samples.sso.soaphandlers.HeaderHandlerResolver;
import vmware.samples.sso.soaphandlers.SamlTokenExtractionHandler;
import vmware.samples.sso.soaphandlers.TimeStampHandler;
import vmware.samples.sso.soaphandlers.UserCredentialHandler;

/**
 * Utility class for acquiring a SAML token from SSO service
 */
public class SsoHelper {

    /**
     * Returns a SAML bearer token by authenticating with the sso server.
     *
     * @param ssoServer sso server hostname or ip address
     * @param username username to login
     * @param password password to login
     *
     * @return a saml bearer token
     * @throws DatatypeConfigurationException
     * @throws InvalidTokenException
     */
    public static SamlToken getSamlBearerToken(String ssoUrl, String username,
        String password)
        throws DatatypeConfigurationException, InvalidTokenException {

        HostnameVerifier hv = new HostnameVerifier() {
            @Override
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };

        /*
         * Note: This method uses https for communication but doesn't verify
         * the certificate from the server. Circumventing SSL is unsafe and
         * should not be used with production code. This is ONLY FOR THE PURPOSE
         * OF DEVELOPMENT ENVIRONMENT.
         */
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        SslUtil.trustAllHttpsCertificates();

        /* Instantiating the STSService */
        STSService stsService = new STSService();

        /*
         * Instantiating the HeaderHandlerResolver. This is required to provide
         * the capability of modifying the SOAP headers and the SOAP message in
         * general for various requests via the different handlers. For
         * different kinds of requests to SSO server one needs to follow the
         * WS-Trust guidelines to provide the required SOAP message structure.
         */
        HeaderHandlerResolver headerResolver = new HeaderHandlerResolver();

        /*
         * For this specific case we need the following header elements wrapped
         * in the security tag.
         *
         * 1. Timestamp containing the request's creation and expiry time
         *
         * 2. UsernameToken containing the username/password
         */

        /* Adding the Timestamp via TimeStampHandler */
        headerResolver.addHandler(new TimeStampHandler());

        /* Adding the UsernameToken via UserCredentialHandler */
        UserCredentialHandler ucHandler = new UserCredentialHandler(username,
            password);
        SamlTokenExtractionHandler sbHandler = new SamlTokenExtractionHandler();
        headerResolver.addHandler(ucHandler);
        headerResolver.addHandler(sbHandler);

        /*
         * Set the handlerResolver for the STSService to the
         * HeaderHandlerResolver created above
         */
        stsService.setHandlerResolver(headerResolver);

        /*
         * Retrieve the STSServicePort from the STSServicePortType object Note:
         * All the required handlerResolvers need to be set in the
         * STSServicePortType object before you retrieve the STSService instance
         */
        STSServicePortType stsPort = stsService.getSTSServicePort();

        /*
         * Construct the SOAP body for the request. RequestSecurityTokenType is
         * the parameter type that is passed to the "acquire" method. However,
         * based on what kind of token (bearer or holder-of-key type) and by
         * what means (aka username/password, certificate, or existing token) we
         * want to acquire the token, different elements need to be populated
         */
        RequestSecurityTokenType tokenType = new RequestSecurityTokenType();

        /*
         * For this request we need at least the following element in the
         * RequestSecurityTokenType set
         *
         * 1. Lifetime - represented by LifetimeType which specifies the
         * lifetime for the token to be issued
         *
         * 2. Tokentype - "urn:oasis:names:tc:SAML:2.0:assertion", which is the
         * class that models the requested token
         *
         * 3. RequestType -
         * "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue", as we want
         * to get a token issued
         *
         * 4. KeyType -
         * "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer",
         * representing the kind of key the token will have. There are two
         * options namely bearer and holder-of-key
         *
         * 5. SignatureAlgorithm -
         * "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", representing the
         * algorithm used for generating signature
         *
         * 6. Renewing - represented by the RenewingType which specifies whether
         * the token is renewable or not
         */
        LifetimeType lifetime = new LifetimeType();

        DatatypeFactory dtFactory = DatatypeFactory.newInstance();
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone(
            "GMT"));
        XMLGregorianCalendar xmlCalendar = dtFactory.newXMLGregorianCalendar(
            cal);
        AttributedDateTime created = new AttributedDateTime();
        created.setValue(xmlCalendar.toXMLFormat());

        AttributedDateTime expires = new AttributedDateTime();
        xmlCalendar.add(dtFactory.newDuration(30 * 60 * 1000));
        expires.setValue(xmlCalendar.toXMLFormat());

        lifetime.setCreated(created);
        lifetime.setExpires(expires);

        tokenType.setTokenType("urn:oasis:names:tc:SAML:2.0:assertion");
        tokenType.setRequestType(
            "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue");
        tokenType.setLifetime(lifetime);
        tokenType.setKeyType(
            "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer");
        tokenType.setSignatureAlgorithm(
            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        tokenType.setDelegatable(true);

        RenewingType renewing = new RenewingType();
        renewing.setAllow(Boolean.FALSE);
        renewing.setOK(Boolean.FALSE); // WS-Trust Profile: MUST be set to false
        tokenType.setRenewing(renewing);

        /* Set the endpoint address for the request */
        Map<String, Object> reqContext = ((BindingProvider) stsPort)
            .getRequestContext();
        reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ssoUrl);

        /*
         * Invoke the "issue" method on the STSService object to acquire the
         * token from SSO Server
         */
        stsPort.issue(tokenType);

        // SamlTokenExtractionHandler will now contain the raw SAML token for
        // further consumption
        Element bearerTokenElement = sbHandler.getToken();
        return DefaultTokenFactory.createTokenFromDom(bearerTokenElement);
    }
}
