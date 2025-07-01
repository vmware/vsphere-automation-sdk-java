/*
 * *******************************************************
 * Copyright VMware, Inc. 2019.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.common;
import java.net.URI;
import java.util.Map;
import com.vmware.authorization.AuthorizationRequestExecutor;
import com.vmware.authorization.Constants;
import com.vmware.authorization.IDPFactory;
import com.vmware.authorization.IdentityProviderType;
import com.vmware.authorization.grant.AuthorizationGrant;
import com.vmware.authorization.grant.oauth2.AuthorizationCodeGrant;
import com.vmware.authorization.grant.oauth2.ClientCredentialsGrant;
import com.vmware.authorization.grant.oauth2.RefreshTokenGrant;
import com.vmware.authorization.grant.oidc.PasswordCredentialsGrant;
import com.vmware.authorization.id.AuthorizationCodeAndState;
import com.vmware.authorization.id.Identity;
import com.vmware.authorization.id.RefreshToken;
import com.vmware.idp.IDPType;
import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vapi.saml.SamlToken;
import vmware.samples.common.authentication.AuthorizationCodeProvider;
import vmware.samples.common.authentication.VapiAuthenticationHelper;
import vmware.samples.vcenter.identity.helpers.VcenterIdentityProvider;
import vmware.samples.vcenter.identity.helpers.VcenterTokenExchange;
/**
 * Description: Utility class to support login using different grant types.
 * e.g authorization code, client credentials, refresh token and password types.
  When logged in successfully, It provides the Service Stub stub.
  Author: VMware, Inc. 
  Sample Prerequisites: vCenter 7.0 with backed by supported IDPs
 */
public class VcenterAuthorizationStubUtil {
    VapiAuthenticationHelper vapiHelper;
    public IdentityProviderType getDefaultIdentityProviderWithType(
            String vcenterServer, boolean skipServerVerification)
            throws Exception {
        VcenterIdentityProvider vip = new VcenterIdentityProvider();
        IdentityProviderType identityType = vip
                .getDefaultIdentityProviderWithType(vcenterServer,
                        skipServerVerification);
        return identityType;
    }
    
    /**
     * A method to get the session stub configuration using vapi authentication
     * helper.
     * @param identityType : includes whether it is OAUTH2, OIDC.
     * @param grantType : indicates various grant types like auth code, client
     *                     credentials, refresh token and password types.
     * @param vcenterServer : server host name or IP address.
     * @return StubConfiguration object.
     * @throws Exception
     */
    public StubConfiguration getStub(IdentityProviderType identityType,
            AuthorizationGrant grantType, String vcenterServer)
            throws Exception {
        AuthorizationRequestExecutor grantExecutor = IDPFactory
                .getAuthRequestExecutor(identityType.getType(), grantType);
        Map<String, String> map = grantExecutor.getAccessAndIdToken();
        VcenterTokenExchange tokenSer = new VcenterTokenExchange();
        //TODO: need to convert samlToken to string type as from the following review url.
        //https://reviewboard.eng.vmware.com/r/1573549/diff/5/
        SamlToken samlToken = tokenSer.getSAMLToken(
                map.get(Constants.ACCESS_TOKEN), map.get(Constants.ID_TOKEN),
                vcenterServer);
        vapiHelper = new VapiAuthenticationHelper();
        HttpConfiguration httpConfig = vapiHelper.buildHttpConfiguration(true);
        StubConfiguration sessionStubConfig = vapiHelper
                .loginBySamlBearerToken(vcenterServer, samlToken, httpConfig);
        return sessionStubConfig;
    }
    /**
     * method to login using auth-code grant type, this method accepts only
     * required parameters for this grant type.
     * @param server : vcenter server ip address or host name.
     * @param clientId : client id of the registered app under oauth2 or oidc.
     * @param clientSecret : indicates client secret of the registered app under
     *        oauth2 or oidc.
     * @param redirectUri : redirect uri where actually code and state variables
     *        are been sent.
     * @param webServerType : whether local or remote wenb server we need to use.
     * @param skipServerVerification : flag to skip server authenication or not.
     * @return session stub config object to mentain login session with Vcenter.
     * @throws Exception
     */
    public StubConfiguration loginUsingAuthCodeGrantType(String server,
            String clientId, String clientSecret, String redirectUri,
            String webServerType, boolean skipServerVerification)
            throws Exception {
        return loginUsingAuthCodeGrantType(server, clientId, clientSecret,
                redirectUri, webServerType, null, null, null, null,
                skipServerVerification);
    }
    /**
     * method to login using auth-code grant type based identity type OIDC or OAUTH2, this method only accepts
     * required parameters and as well as all optional parameters for this grant
     * type.
     * @param server : vcenter server ip address or host name.
     * @param clientId : client id of the registered app under oauth2 or oidc.
     * @param clientSecret : indicates client secret of the registered app under
     *        oauth2 or oidc.
     * @param redirectUri : redirect uri where actually code and state variables
     *        are been sent.
     * @param webServerType : whether local or remote wenb server we need to use.
     * @param org_id : org id tell under which organization the app is being
     *        registered.
     * @param username : users user name associated with identity provider.
     * @param password : users password associated with identity provider.
     * @param ref_token : refresh token to refresh the access token and id token.
     * @param skipServerVerification- tell about to skip server verification or
     *        not.
     * @return session stub config object to maintain login session with Vcenter.
     * @throws Exception
     */
    public StubConfiguration loginUsingAuthCodeGrantType(String server,
            String clientId, String clientSecret, String redirectUri,
            String webServerType, String org_id, String username,
            String password, String ref_token, boolean skipServerVerification)
            throws Exception {
        IdentityProviderType identityType = getDefaultIdentityProviderWithType(
                server, skipServerVerification);
        URI redirectURI = new URI(redirectUri);
        AuthorizationCodeProvider authProvider = new AuthorizationCodeProvider(
                identityType, redirectURI.toString(), webServerType);
        authProvider.openbrowser(server, clientId);
        AuthorizationCodeAndState codeAndState = authProvider
                .getAuthorizationCodeAndState();
        AuthorizationGrant grantType;
        Identity orgIdentity = getValueForDefaultIdentity(org_id);
        Identity userIdentity = getValueForDefaultIdentity(username);
        Identity passwordIdentity = getValueForDefaultIdentity(password);
        Identity refreshTokenIdentity = getValueForDefaultIdentity(ref_token);
        if (IDPType.OAUTH2.equals(identityType.getType())) {
            grantType = new AuthorizationCodeGrant(codeAndState,
                    identityType.getTokenEndPoint(), redirectURI,
                    new Identity(clientId), new Identity(clientSecret),
                    orgIdentity, userIdentity, passwordIdentity,
                    refreshTokenIdentity);
        } else {
            grantType = new com.vmware.authorization.grant.oidc.AuthorizationCodeGrant(
                    codeAndState, identityType.getTokenEndPoint(), redirectURI,
                    new Identity(clientId), new Identity(clientSecret));
        }
        return getStub(identityType, grantType, server);
    }
    /**
     * This method returns an instance of Identity for a non empty String.
     * @param paramValue : value passed to method to check is it empty or not.
     * @return Identity object.
     */
    private Identity getValueForDefaultIdentity(String paramValue) {
        if (isEmpty(paramValue))
            return null;
        return new Identity(paramValue);
    }
    /**
     * method to check the parameter value is empty or not.
     * @param paramValue : value of the parameter.
     * @return boolean
     */
    private boolean isEmpty(String paramValue) {
        if (null == paramValue || paramValue.isEmpty()) {
            return true;
        }
        return false;
    }
    /**
     * method to get the stub for the particular service class.
     * @param serviceClass : service class template.
     * @param sessionStubConfig : session stub object after authentication.
     * @return service class.
     */
    public <T extends Service> T getStub(Class<T> serviceClass,
            StubConfiguration sessionStubConfig) {
        return vapiHelper.getStubFactory().createStub(serviceClass,
                sessionStubConfig);
    }
    /**
     * method to log out from the particular session,
     */
    public void logout() {
        vapiHelper.logout();
    }
   /**
    * method to login using client-credentials grant type, this method only accepts
    * required parameters for this grant type.
    * @param server : vcenter server ip address or host name.
    * @param clientId : client id of the registered app under oauth2 or oidc.
    * @param clientSecret : indicates client secret of the registered app under
    * @param orgId : indicates under which org vcenter is been deployed.
    * @param skipServerVerification : Value to skip server verification or
    * not.
    * @return :  session stub config object to maintain login session with Vcenter.
    * @throws Exception
    */
    public StubConfiguration loginUsingClientCredentialsGrantType(String server, String clientId, String clientSecret,
            String orgId,  boolean skipServerVerification) throws Exception {
        return loginUsingClientCredentialsGrantType(server, clientId, clientSecret, orgId,
                null, null, null, null, null, skipServerVerification);
    }
    /**
     * method to login using client-credentials grant type, based identity type OIDC or OAUTH2,this method accepts
     * required parameters as well as all optional parameters for this grant type.
     * currently OIDC enabled ADFS does not support client credentials grant type.
     * @param server : vcenter server ip address or host name.
     * @param clientId : client id of the registered app under oauth2 or oidc.
     * @param clientSecret : indicates client secret of the registered app under oauth2 or oidc.
     * @param orgId : indicates under which org vcenter is been deployed.
     * @param redirectUri : redirect uri where actually code and state variables
     * are been sent.
     * @param username : users user name associated with identity provider.
     * @param password : users password associated with identity provider.
     * @param refresh_token : refresh token to refresh the access token and id token.
     * @param webServerType : whether local or remote wenb server we need to use.
     * @param skipServerVerification : tell about to skip server verification or
     * not.
     * @return : session stub config object to maintain login session with Vcenter.
     * @throws Exception
     */
    public StubConfiguration loginUsingClientCredentialsGrantType(String server,
            String clientId, String clientSecret, String orgId, String redirectUri,
            String userName, String passWord, String refresh_token, String webServerType,
            boolean skipServerVerification) throws Exception {
        IdentityProviderType identityType = getDefaultIdentityProviderWithType(
                server, true);
        AuthorizationGrant clientCredentialsGranttype = null;
        if (IDPType.OAUTH2.equals(identityType.getType())) {
            clientCredentialsGranttype = new ClientCredentialsGrant(
                    identityType.getTokenEndPoint(), new Identity(orgId),
                    new Identity(clientId), new Identity(clientSecret));
        }
        return getStub(identityType, clientCredentialsGranttype, server);
    }
    /**
     * method to login using refresh-token grant type, based on identity type OIDC or OAUTH2,this method accepts
     * required parameters as well as all optional parameters for this grant type.
     * @param server : vcenter server ip address or host name.
     * @param clientId : client id of the registered app under oauth2 or oidc.
     * @param clientSecret : indicates client secret of the registered app under oauth2 or oidc.
     * @param refreshToken : refresh token to refresh the access token and id token.
     * @param skipServerVerification : tell about to skip server verification or not.
     * @param username : users user name associated with identity provider.
     * @param password : users password associated with identity provider.
     * @param orgId : indicates under which org vcenter is been deployed.
     * @param redirectUri : redirect uri where actually code and state variables
     * are been sent.
     * @param serveType : whether local or remote wenb server we need to use.
     * @return  : session stub config object to maintain login session with Vcenter.
     * @throws Exception
     */
    public StubConfiguration loginUsingRefreshTokenGrantType(String server,
            String clientId, String clientSecret, String refreshToken,
            String serveType, String userName, String passWord,
            String orgId, String redirectUri, boolean skipServerVerification) throws Exception {
        IdentityProviderType identityType = getDefaultIdentityProviderWithType(
                server, true);
        AuthorizationGrant refreshGrantType = null;
        /**
         * Get RequestExecutor by passing the RefreshTokenGrant
         */
        if (IDPType.OAUTH2.equals(identityType.getType())) {
            refreshGrantType = new RefreshTokenGrant(
                    identityType.getTokenEndPoint(),
                    new RefreshToken(refreshToken), new Identity(clientId),
                    new Identity(clientSecret));
        } else {
            refreshGrantType = new com.vmware.authorization.grant.oidc.RefreshTokenGrant(
                    identityType.getTokenEndPoint(), new Identity(clientId),
                    new Identity(clientSecret), new Identity(refreshToken));
        }
        return getStub(identityType, refreshGrantType, server);
    }
    /**
     * method to login using refresh-token grant type, this method only accepts
     * required parameters for this grant type.
     * @param server : vcenter server ip address or host name.
     * @param clientId : client id of the registered app under oauth2 or oidc.
     * @param clientSecret : indicates client secret of the registered app under oauth2 or oidc.
     * @param refreshToken : refresh token to refresh the access token and id token.
     * @param skipServerVerification : tells about to skip server verification or not.
     * @return : session stub config object to maintain login session with Vcenter.
     * @throws Exception
     */
    public StubConfiguration loginUsingRefreshTokenGrantType(String server,
            String clientId, String clientSecret, String refreshToken,
            boolean skipServerVerification) throws Exception {
    return loginUsingRefreshTokenGrantType(server, clientId, clientSecret, refreshToken,
            null, null, null, null, null, skipServerVerification);
      }
    /**
     * method to login using password grant type, this method accepts all
     * required parameters as well as optional parameters.
     * @param server : vcenter server ip address or host name.
     * @param clientId : client id of the registered app under oauth2 or oidc.
     * @param clientSecret : indicates client secret of the registered app under oauth2 or oidc.
     * @param username : users user name associated with identity provider.
     * @param password : users password associated with identity provider.
     * @param skipServerVerification : tells about to skip server verification or not.
     * @param orgId : indicates under which org vcneter is been deployed.
     * @param redirectUri : redirect uri where actually code and state variables
     * are been sent.
     * @param serverType : local or remote web server. For testing purpose, the sample
     * code shipped along with this SDK will spawn a local webserver.
     * @param refresh_token : refresh token to refresh the access token and id token.
     * @return : session stub config object to maintain login session with Vcenter.
     * @throws Exception
     */
    public StubConfiguration loginUsingPasswordGrantType(String server,
            String clientId, String clientSecret, String userName,
            String password, String refresh_token, String orgId, String redirectUri,
            String serverType, boolean skipServerVerification) throws Exception {
        IdentityProviderType identityType = getDefaultIdentityProviderWithType(
                server, true);
        AuthorizationGrant passwordGrantType = null;
        if (IDPType.OIDC.equals(identityType.getType())) {
            passwordGrantType = new PasswordCredentialsGrant(
                    identityType.getTokenEndPoint(), new Identity(clientId),
                    new Identity(clientSecret), new Identity(userName),
                    new Identity(password));
        } else {
        }
        return getStub(identityType, passwordGrantType, server);
    }
    /**
     * method to login using password grant type, this method only accepts required parameters.
     * @param server : vcenter server ip address or host name.
     * @param clientId : client id of the registered app under oauth2 or oidc.
     * @param clientSecret : indicates client secret of the registered app under oauth2 or oidc.
     * @param username : users user name associated with identity provider.
     * @param password : users password associated with identity provider.
     * @param skipServerVerification : Value to skip server verification or not.
     * @return : session stub config object to maintain login session with Vcenter.
     * @throws Exception
     */
    public StubConfiguration loginUsingPasswordGrantType(String server,
            String clientId, String clientSecret, String userName,
            String password, boolean skipServerVerification) throws Exception {
        return loginUsingPasswordGrantType( server,
                 clientId,  clientSecret,  userName,
                 password,  null, null, null, null, skipServerVerification);
    }
}
