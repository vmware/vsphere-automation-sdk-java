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

import java.util.Map;

import com.vmware.authorization.AuthorizationRequestExecutor;
import com.vmware.authorization.Constants;
import com.vmware.authorization.IDPFactory;
import com.vmware.authorization.IdentityProviderType;
import com.vmware.authorization.grant.AuthorizationGrant;
import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vapi.saml.SamlToken;

import vmware.samples.common.authentication.VapiAuthenticationHelper;
import vmware.samples.vcenter.identity.helpers.VcenterIdentityProvider;
import vmware.samples.vcenter.identity.helpers.VcenterTokenExchange;

public class AuthorizationUtil {
	
	private AuthorizationUtil() {}

	public static IdentityProviderType getDefaultIdentityProviderWithType(String vcenterServer,
			boolean skipServerVerification) throws Exception {
		VcenterIdentityProvider vip = new VcenterIdentityProvider();
        IdentityProviderType identityType = vip.getDefaultIdentityProviderWithType(vcenterServer,
        		skipServerVerification);
		return identityType;
		
	}
	public static <T extends Service> T getStub(Class<T> serviceClass, IdentityProviderType identityType,
			AuthorizationGrant grantType, String vcenterServer) throws Exception {
		
		AuthorizationRequestExecutor grantExecutor = IDPFactory.getAuthRequestExecutor(identityType.getType(),
                grantType);
		 Map<String, String> map = grantExecutor.getAccessAndIdToken();
		 VcenterTokenExchange tokenSer = new VcenterTokenExchange();
	     SamlToken samlToken = tokenSer.getSAMLToken(map.get(Constants.ACCESS_TOKEN),
	                map.get(Constants.ID_TOKEN), vcenterServer);
	     //System.out.println("SAML Bearer Token :" + samlToken);   
	     VapiAuthenticationHelper vapiHelper = new VapiAuthenticationHelper();
	     HttpConfiguration httpConfig = vapiHelper.buildHttpConfiguration(true);
	     StubConfiguration sessionStubConfig = vapiHelper
	                .loginBySamlBearerToken(vcenterServer, samlToken, httpConfig);
	     
	     return vapiHelper.getStubFactory().createStub(serviceClass,
	                sessionStubConfig);
		
	}
}

