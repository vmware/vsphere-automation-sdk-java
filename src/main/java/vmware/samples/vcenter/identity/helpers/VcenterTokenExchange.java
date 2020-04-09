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
package vmware.samples.vcenter.identity.helpers;

import javax.xml.bind.DatatypeConverter;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.cis.authn.SecurityContextFactory;
import com.vmware.vapi.core.ExecutionContext.SecurityContext;
import com.vmware.vapi.saml.DefaultTokenFactory;
import com.vmware.vapi.saml.SamlToken;
import com.vmware.vcenter.tokenservice.TokenExchange;
import com.vmware.vcenter.tokenservice.TokenExchangeTypes;

import vmware.samples.common.authentication.VapiAuthenticationHelper;


public class VcenterTokenExchange {
	private VapiAuthenticationHelper vapiHelper = new VapiAuthenticationHelper(); 
	
	public VcenterTokenExchange() {}
	
    /**
     * Hits the token exchange service to get the SAML token to login to the
     * corresponding vcenter.
     * @param authenticationHelper the authentication helper
     * @param access_token         the access token
     * @param id_token             the id token
     * @param vcenterHostname      the Vcenter host name
     * @param httpConfiguration    the http configuration
     * @return the saml token of type SamlToken
     */
    public SamlToken getSAMLToken(String access_token, String id_token, String vcenterHostname) {
        SamlToken samlBearerToken = null;
        try {
            SecurityContext context = SecurityContextFactory.createOAuthSecurityContext(access_token.toCharArray());
            StubConfiguration sessionStubConfig = new StubConfiguration(context);
            TokenExchangeTypes.ExchangeSpec spec = new TokenExchangeTypes.ExchangeSpec();
            //ID Token have valid values only for authorization code grant type
            if(!"null".equalsIgnoreCase(id_token)) {
                spec.setActorToken(id_token);
                spec.setActorTokenType("urn:ietf:params:oauth:token-type:id_token");
            }
            spec.setGrantType("urn:ietf:params:oauth:grant-type:token-exchange");
            spec.setRequestedTokenType("urn:ietf:params:oauth:token-type:saml2");
            spec.setSubjectTokenType("urn:ietf:params:oauth:token-type:access_token");
            spec.setSubjectToken(access_token);
            vapiHelper.createStubFactory(vcenterHostname, true);
            TokenExchangeTypes.Info info = new TokenExchangeTypes.Info();
            TokenExchange tokenExchange = vapiHelper.getStubFactory().createStub(TokenExchange.class,
                    sessionStubConfig);
            info = tokenExchange.exchange(spec);
            byte[] decodedBytes = DatatypeConverter.parseBase64Binary(info.getAccessToken());
            String decodedSamlToken = new String(decodedBytes, "UTF-8");
            samlBearerToken = DefaultTokenFactory.createToken(decodedSamlToken);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        return samlBearerToken;

    }
}
