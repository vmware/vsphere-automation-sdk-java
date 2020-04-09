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

import java.util.List;

import com.vmware.authorization.IdentityProviderType;
import com.vmware.authorization.oidc.OIDCType;
import com.vmware.authorization.oauth2.Oauth2Type;
import com.vmware.idp.IDPType;
import com.vmware.vcenter.identity.Providers;
import com.vmware.vcenter.identity.ProvidersTypes.Summary;

import vmware.samples.common.authentication.VapiAuthenticationHelper;
/**
 * Description: Containing generic method implementation required to get identity providers list.
 * Author: VMware, Inc.
 * Sample Prerequisites: vCenter 6.8.X
 */
public class VcenterIdentityProvider {
	
	private VapiAuthenticationHelper vAuthHelper = new VapiAuthenticationHelper();
	 
 	private Providers providersService;
		
	/**
	 * Gets the identity providers.
	 *
	 * @param server the server
	 * @return the identity providers
	 * @throws Exception the exception
	 */
	public List<Summary> getIdentityProviders(String vCenterServer,
			boolean skipServerVerification) throws Exception {
		this.vAuthHelper.createStubFactory(vCenterServer, skipServerVerification);
		this.providersService = this.vAuthHelper.getStubFactory().createStub(Providers.class);
		List<Summary> identityProviders = providersService.list();
		return identityProviders;
	}

	/**
	 * Returns the default Identity Provider for the given vCenter Server
	 * 
	 * @param server
	 * @return
	 * @throws Exception 
	 */
	public Summary getDefaultIdentityProviderSummary(String vCenterServer,
			boolean skipServerVerification) throws Exception
	{
		Summary providerSummary = null;
		List<Summary> identityProviders = getIdentityProviders(vCenterServer, skipServerVerification);
		if(identityProviders.isEmpty())
		{
			System.out.println("Provider Empty");
			return providerSummary;
		}
		for (Summary summary : identityProviders) {
			if (summary.getIsDefault())
			{
				providerSummary = summary;
				break;
			}
		}
		return providerSummary;
	}
	
	/**
	 * Returns the default Identity Provider for the given vCenter Server
	 * 
	 * @param server
	 * @return
	 * @throws Exception 
	 */
	public IdentityProviderType getDefaultIdentityProviderWithType(String vCenterServer,
			boolean skipServerVerification) throws Exception
	{

		Summary summary = getDefaultIdentityProviderSummary(vCenterServer, skipServerVerification);
		IdentityProviderType providerType = getIdentityWithTypeFromSummary(summary);
		return providerType;
	}
	
	private IdentityProviderType getIdentityWithTypeFromSummary(Summary summary) {
		if (null == summary)
			return null;
		IdentityProviderType identityType = null;
		IDPType idpType = IDPType.fromName(summary.getConfigTag().name());
		if(IDPType.OAUTH2.equals(idpType)) {
			identityType = new Oauth2Type(summary.getOauth2().getAuthEndpoint(),
					summary.getOauth2().getTokenEndpoint(), 
					summary.getOauth2().getAuthQueryParams());
		}else
			identityType  = new OIDCType(summary.getOidc().getAuthEndpoint(),
					summary.getOidc().getTokenEndpoint(), 
					summary.getOidc().getAuthQueryParams()); 
		return identityType;
	}

}

