/*
 * *******************************************************
 * Copyright (c) 2024 Broadcom. All Rights Reserved.
 * Broadcom Confidential. The term "Broadcom" refers to Broadcom Inc.
 * and/or its subsidiaries.
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

 import com.vmware.vapi.bindings.StubConfiguration;
 import com.vmware.vapi.cis.authn.SecurityContextFactory;
 import com.vmware.vapi.core.ExecutionContext;
 import com.vmware.vapi.protocol.HttpConfiguration;
 import com.vmware.vapi.saml.SamlToken;
 import org.apache.commons.cli.Option;
 import vmware.samples.common.authentication.VapiAuthenticationHelper;
 import vmware.samples.sso.SsoHelper;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 
 /**
  * Abstract base class for snapservice samples
  */
 public abstract class SnapserviceSamplesAbstractBase extends SamplesAbstractBase {
 
     protected String ssServer;
     protected VapiAuthenticationHelper ssAuthHelper, vcAuthHelper;
     protected StubConfiguration ssStubConfig, vcStubConfig;
 
     /**
      * Parses the command line arguments / config file and creates a map of
      * key-value pairs having all the required options for the sample
      *
      * @param sampleOptions List of options required by the sample
      * @param args          String array of arguments
      */
     protected void parseArgs(List<Option> sampleOptions, String[] args) {
 
         // Default options for all snapservice samples
         Option ssServerOption = Option.builder()
                 .required(true)
                 .hasArg()
                 .argName("SNAPSERVICE")
                 .longOpt("snapservice")
                 .desc("hostname of snapservice server")
                 .build();
 
         List<Option> optionList = new ArrayList<>(Arrays.asList(ssServerOption));
         optionList.addAll(sampleOptions);
 
         super.parseArgs(optionList, args);
         this.ssServer = (String) parsedOptions.get("snapservice");
     }
 
     public String getSsServer() {
         return this.ssServer;
     }
 
     /**
      * Creates a session with the vCenter using username/password.
      * Acquire SAML token from psc using username/password.
      * Creates a session with the snapservice using the acquired token.
      *
      * <p><b>
      * Note: If the "skip-server-verification" option is specified, then this
      * method trusts the SSL certificate from the server and doesn't verify
      * it. Circumventing SSL trust in this manner is unsafe and should not be
      * used with production code. This is ONLY FOR THE PURPOSE OF DEVELOPMENT
      * ENVIRONMENT
      * <b></p>
      *
      * @throws Exception
      */
     protected void login() throws Exception {
         // Session to vCenter
         this.vcAuthHelper = new VapiAuthenticationHelper();
         HttpConfiguration httpConfig = buildHttpConfiguration();
         this.vcStubConfig =
                 vcAuthHelper.loginByUsernameAndPassword(
                         this.server, this.username, this.password, httpConfig);
 
         // Session to snapservice
         this.ssAuthHelper = new VapiAuthenticationHelper();
         this.ssAuthHelper.createStubFactory(ssServer, httpConfig);
 
         System.out.println("\n\nAcquire SAML token from PSC.\n");
 
         String ssoUrl = "https://" + server + "/sts/STSService";
 
         // The token lifetime is 30 minutes.
         SamlToken samlBearerToken = SsoHelper.getSamlBearerToken(ssoUrl,
                 username,
                 password);
 
         // Create a SAML security context using SAML bearer token
         ExecutionContext.SecurityContext samlSecurityContext =
                 SecurityContextFactory.createSamlSecurityContext(
                         samlBearerToken, null);
 
         // Create a stub configuration with SAML security context
         this.ssStubConfig =
                 new StubConfiguration(samlSecurityContext);
     }
 
     /**
      * Logs out of the server
      *
      * @throws Exception
      */
     protected void logout() throws Exception {
         this.vcAuthHelper.logout();
         // No need to logout ssAuthHelper.
     }
 }
 