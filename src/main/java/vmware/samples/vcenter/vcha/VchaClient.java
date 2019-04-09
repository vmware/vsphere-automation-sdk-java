/*
 * *******************************************************
 * Copyright VMware, Inc. 2018.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vcenter.vcha;

import java.util.Arrays;
import java.util.List;

import com.vmware.vcenter.vcha.CredentialsSpec;
import com.vmware.vcenter.vcha.Cluster;
import com.vmware.vcenter.vcha.cluster.Active;
import com.vmware.vcenter.vcha.cluster.Mode;
import org.apache.commons.cli.Option;
import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.vcenter.vcha.helpers.SpecHelper;
import vmware.samples.vcenter.vcha.helpers.ArgumentsHelper;

/**
 * Description: Demonstrates listing active node information, vCenter HA cluster information and vCenter HA cluster mode
 *
 * Step 1: List active node information
 * Step 2: List vCenter HA cluster information
 * Step 3: List vCenter HA cluster mode
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample requires a configured vCenter HA cluster
 *
 */
public class VchaClient extends SamplesAbstractBase {
    private String vcSpecActiveLocationHostname;
    private String vcSpecActiveLocationUsername;
    private String vcSpecActiveLocationPassword;
    private String vcSpecActiveLocationSSLThumbprint;

    private CredentialsSpec mgmtVcCredentialsSpec;
    private Cluster clusterService;
    private Active activeService;
    private Mode modeService;


    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected void parseArgs(String[] args) {
        Option mgmtHostnameOption = Option.builder()
                .longOpt(ArgumentsHelper.VC_SPEC_ACTIVE_LOCATION_HOSTNAME)
                .desc("hostname of the Management vCenter Server. Leave blank if it's a self-managed VC")
                .argName("MGMT VC HOST")
                .required(false)
                .hasArg()
                .build();
        Option mgmtUsernameOption = Option.builder()
                .longOpt(ArgumentsHelper.VC_SPEC_ACTIVE_LOCATION_USERNAME)
                .desc("username to login to the Management vCenter Server. Leave blank if it's a self-managed VC")
                .argName("MGMT VC USERNAME")
                .required(false)
                .hasArg()
                .build();
        Option mgmtPasswordOption = Option.builder()
                .longOpt(ArgumentsHelper.VC_SPEC_ACTIVE_LOCATION_PASSWORD)
                .desc("password to login to the Management vCenter Server. Leave blank if it's a self-managed VC")
                .argName("MGMT VC PASSWORD")
                .required(false)
                .hasArg()
                .build();
        Option mgmtVcSSLThumbprintOption = Option.builder()
                .longOpt(ArgumentsHelper.VC_SPEC_ACTIVE_LOCATION_SSL_THUMBPRINT)
                .desc("SSL Thumbprint of Management vCenter Server.")
                .argName("MGMT VC SSL THUMBPRINT")
                .required(true)
                .hasArg()
                .build();
        List<Option> optionList = Arrays.asList(mgmtHostnameOption,
                                                mgmtUsernameOption,
                                                mgmtPasswordOption,
                                                mgmtVcSSLThumbprintOption);
        super.parseArgs(optionList, args);
        this.vcSpecActiveLocationHostname = ArgumentsHelper.getStringArg(parsedOptions,
        		ArgumentsHelper.VC_SPEC_ACTIVE_LOCATION_HOSTNAME);
        if(this.vcSpecActiveLocationHostname == null)
            this.vcSpecActiveLocationHostname = this.getServer();
        this.vcSpecActiveLocationUsername = ArgumentsHelper.getStringArg(parsedOptions,
        		ArgumentsHelper.VC_SPEC_ACTIVE_LOCATION_USERNAME);
        if(this.vcSpecActiveLocationUsername == null)
            this.vcSpecActiveLocationUsername = this.getUsername();
        this.vcSpecActiveLocationPassword = ArgumentsHelper.getStringArg(parsedOptions,
        		ArgumentsHelper.VC_SPEC_ACTIVE_LOCATION_PASSWORD);
        if(this.vcSpecActiveLocationPassword == null)
            this.vcSpecActiveLocationPassword = this.getPassword();
        this.vcSpecActiveLocationSSLThumbprint = ArgumentsHelper.getStringArg(parsedOptions,
        		ArgumentsHelper.VC_SPEC_ACTIVE_LOCATION_SSL_THUMBPRINT);
    }

    protected void setup() throws Exception {
        this.clusterService =
                this.vapiAuthHelper.getStubFactory().createStub(Cluster.class, this.sessionStubConfig);
        this.activeService =
                this.vapiAuthHelper.getStubFactory().createStub(Active.class, this.sessionStubConfig);
        this.modeService =
                this.vapiAuthHelper.getStubFactory().createStub(Mode.class, this.sessionStubConfig);
        this.mgmtVcCredentialsSpec = SpecHelper.createCredentialsSpec(this.vcSpecActiveLocationHostname,
                                                                      this.vcSpecActiveLocationUsername,
                                                                      this.vcSpecActiveLocationPassword,
                                                                      this.vcSpecActiveLocationSSLThumbprint);
    }

    protected void run() throws Exception {
        // List active node info, vCenter HA cluster info and cluster mode

    	final String LINE_SEPARATOR = "--------------------------------------------------------------------";
        System.out.println(LINE_SEPARATOR);
        System.out.println("ACTIVE NODE INFO");
        System.out.println(LINE_SEPARATOR);
        System.out.println(this.activeService.get(this.mgmtVcCredentialsSpec, false).toString());
        System.out.println(LINE_SEPARATOR);
        System.out.println("CLUSTER INFO");
        System.out.println(LINE_SEPARATOR);
        System.out.println(this.clusterService.get(this.mgmtVcCredentialsSpec, false).toString());
        System.out.println(LINE_SEPARATOR);
        System.out.println("CLUSTER MODE");
        System.out.println(LINE_SEPARATOR);
        System.out.println(this.modeService.get().toString());
    }
    protected void cleanup() throws Exception {
        // No cleanup required
    }

    public static void main(String[] args) throws Exception {
        /*
         * Execute the sample using the command line arguments or parameters
         * from the configuration file. This executes the following steps:
         * 1. Parse the arguments required by the sample
         * 2. Login to the server
         * 3. Setup any resources required by the sample run
         * 4. Run the sample
         * 5. Cleanup any data created by the sample run, if cleanup=true
         * 6. Logout of the server
         */
        new VchaClient().execute(args);
    }
}
