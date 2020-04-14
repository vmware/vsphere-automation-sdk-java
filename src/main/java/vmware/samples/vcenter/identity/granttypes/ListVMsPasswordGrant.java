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
package vmware.samples.vcenter.identity.granttypes;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes.FilterSpec.Builder;
import com.vmware.vcenter.VMTypes.Summary;
import vmware.samples.common.ParametersHelper;
import vmware.samples.common.VcenterAuthorizationStubUtil;
/**
 * Description: Demonstrated Listing of VMs present in vCenter using Password grant type for oauth2.
 * Password grant type: The Password grant type is used by first-party clients to exchange a user's credentials
 * for an access token.Since this involves the client asking the user for their password, it should not be used by.
 * third party clients. In this flow, the user's username and password are exchanged directly for an access token.
 * Author: VMware, Inc.
 * Sample Prerequisites: vCenter 7.0
 * Arguments:
 * VcenterServer: vcenter server IP or Host name.
 * clientId: client Id of Oauth registered app.
 * clientSecret: Client Secret key of Oauth registered app.
 * userName: user name provided while creating the app in OIDC/Oauth App.
 * passWord: passWord provided while creating the app in OIDC/Oauth App.
 */
public class ListVMsPasswordGrant {
    protected String vmFolderName;
    protected String datacenterName;
    protected String clusterName;
    protected String server;
    protected String clientId;
    protected String clientSecret;
    protected String userName;
    protected String passWord;
    protected Map<String, Object> parsedOptions;
    protected VcenterAuthorizationStubUtil authorizationStubUtil;
    protected StubConfiguration sessionStubConfig;
    protected Boolean skipServerVerification = true;
    private VM vmService;
    protected void parseArgs(String[] args) throws ConfigurationException, ParseException {
    ParametersHelper paramsHelper = null;
    Option datacenterOption = Option.builder()
            .longOpt("datacenter")
            .desc("OPTIONAL: Specify the name of the Datacenter"
                    + " to list the Vms in it.")
            .argName("DATACENTER")
            .required(false)
            .hasArg()
            .build();
    Option vmFolderOption = Option.builder()
            .longOpt("vmfolder")
            .desc("OPTIONAL: Specify the name of the VM Folder to list the"
                    + " Vms in it.")
            .argName("VM FOLDER")
            .required(false)
            .hasArg()
            .build();
    Option clusterOption = Option.builder()
            .longOpt("cluster")
            .desc("OPTIONAL: Specify the name of the Cluster to list the"
                    + " Vms in it.")
            .argName("CLUSTER")
            .required(false)
            .hasArg()
            .build();
    Option serverOption = Option.builder()
            .required(true)
            .hasArg()
            .argName("SERVER")
            .longOpt("server")
            .desc("hostname of vCenter Server")
            .build();
    Option clientIdOption = Option.builder()
              .hasArg()
              .argName("CLIENT_ID")
              .longOpt("clientId")
              .desc("client id of the CSP registered app")
              .build();
    Option clientSecretOption = Option.builder()
              .hasArg()
              .argName("CLIENT_SECRET")
              .longOpt("clientSecret")
              .desc("client secret of the CSP registered app")
              .build();
    Option userNameOption = Option.builder()
            .required(true)
            .hasArg()
            .argName("USER_NAME")
            .longOpt("userName")
            .desc("userName of the CSP registerd app")
            .build();
    Option passWordOption = Option.builder()
            .required(true)
            .hasArg()
            .argName("PAASWORD")
            .longOpt("passWord")
            .desc("passWord of the CSP registerd app")
            .build();
    Option skipServerVerificationOption =
            Option.builder()
                  .required(false)
                  .longOpt("skip-server-verification")
                  .type(Boolean.class)
                  .desc("OPTIONAL: Specify this option if you do not "
                          + "want to perform SSL certificate "
                          + "verification.\nNOTE: Circumventing SSL "
                          + "trust in this manner is unsafe and should "
                          + "not be used with production code. "
                          + "This is ONLY FOR THE PURPOSE OF "
                          + "DEVELOPMENT ENVIRONMENT.")
                  .build();
    Option configFileJson =  Option.builder()
            .required(false)
            .longOpt("oauth_app_json")
            .hasArg()
            .desc("OPTIONAL: Specify this option when identity provider meta data avaiable in config file")
            .build();
    List<Option> optionList = Arrays.asList(vmFolderOption,
            datacenterOption, clusterOption, serverOption, clientIdOption,
            clientSecretOption, userNameOption, passWordOption, skipServerVerificationOption, configFileJson);
    paramsHelper = new ParametersHelper(optionList);
    this.parsedOptions = paramsHelper.parse(args,
            this.getClass().getName());
    this.parsedOptions=paramsHelper.parseJsonConfig(args);
    this.vmFolderName = (String) parsedOptions.get("vmfolder");
    this.datacenterName = (String) parsedOptions.get("datacenter");
    this.clusterName = (String) parsedOptions.get("cluster");
    this.clientId = (String) parsedOptions.get("clientId");
    this.clientSecret = (String) parsedOptions.get("clientSecret");
    this.server = (String) parsedOptions.get("server");
    this.userName = (String) parsedOptions.get("userName");
    this.passWord = (String) parsedOptions.get("passWord");
    Object skipServerVerificationObj =
            parsedOptions.get("skip-server-verification");
    if(skipServerVerificationObj != null) {
        this.skipServerVerification =
                (Boolean) skipServerVerificationObj;
    } else {
        this.skipServerVerification = false;
    }}
    protected void login() throws Exception {
        authorizationStubUtil = new VcenterAuthorizationStubUtil();
        sessionStubConfig = authorizationStubUtil.
                loginUsingPasswordGrantType(server, clientId, clientSecret, userName, passWord, skipServerVerification);
    }
    protected void setup() throws Exception {
        vmService = authorizationStubUtil.getStub(VM.class, sessionStubConfig);
    }
    /**
     * The main method.
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {
        try {
            new ListVMsPasswordGrant().execute(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }
    private void execute(String[] args) throws Exception {
        parseArgs(args);
        login();
        setup();
        List<Summary> vmList = vmService.list(new Builder().build());
        System.out.println("----------------------------------------");
        System.out.println("List of VMs " + vmList.size());
        for (Summary vmSummary : vmList) {
            System.out.println(vmSummary);
        }
        System.out.println("----------------------------------------");
        authorizationStubUtil.logout();
    }

}
