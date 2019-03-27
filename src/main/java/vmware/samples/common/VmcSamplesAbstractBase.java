/*
 * *******************************************************
 * Copyright VMware, Inc. 2017.  All Rights Reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration;

import vmware.samples.common.authentication.VmcAuthenticationHelper;

/**
 * Abstract base class for VMC samples.
 */
public abstract class VmcSamplesAbstractBase {
    protected String vmcServer;
    protected String cspServer;
    protected String refreshToken;
    protected boolean clearData;
    protected String configFile;
    protected VmcAuthenticationHelper vmcAuthHelper;
    protected StubConfiguration sessionStubConfig;
    protected Map<String, Object> parsedOptions;
    
    /**
     * Parses the command line arguments / config file and creates a map of
     * key-value pairs having all the required options for the sample
     *
     * @param sampleOptions List of options required by the sample
     * @param args String array of arguments
     */
    protected void parseArgs(List<Option> sampleOptions, String[] args) {
        ParametersHelper paramsHelper = null;
        try {
            // Default options for all samples
            Option vmcServerOption = Option.builder()
                                        .required(false)
                                        .hasArg()
                                        .argName("VMC SERVER")
                                        .longOpt("vmcserver")
                                        .desc("OPTIONAL: hostname of VMC Server")
                                        .build();

            Option cspServerOption = Option.builder()
                                        .required(false)
                                        .hasArg()
                                        .argName("CSP SERVER")
                                        .longOpt("cspserver")
                                        .desc("OPTIONAL: hostname of CSP Server")
                                        .build();
            
            Option refreshTokenOption = Option.builder()
                                        .required(true)
                                        .hasArg()
                                        .argName("REFRESH TOKEN")
                                        .longOpt("refreshtoken")
                                        .desc("refresh token for getting an access token")
                                        .build();

            Option cleardataOption = Option.builder()
                                           .required(false)
                                           .longOpt("cleardata")
                                           .type(Boolean.class)
                                           .desc("OPTIONAL: Specify this option"
                                                   + " to undo all persistent "
                                                   + "results of running the "
                                                   + "sample.")
                                           .build();

            Option configFileOption = Option.builder()
                                            .required(false)
                                            .hasArg()
                                            .argName("CONFIGURATION FILE")
                                            .longOpt("config-file")
                                            .desc("OPTIONAL: Absolute path to  "
                                                    + "the configuration file "
                                                    + "containing the sample "
                                                    + "options.\nNOTE: "
                                                    + "Parameters can be "
                                                    + "specified either "
                                                    + "in the configuration "
                                                    + "file or on the command "
                                                    + "line. Command "
                                                    + "line parameters will "
                                                    + "override values "
                                                    + "specified in the "
                                                    + "configuration file.")
                                            .build();

            List<Option> optionList = new ArrayList<>(Arrays.asList(
                configFileOption, refreshTokenOption, vmcServerOption, cspServerOption));
            optionList.addAll(sampleOptions);
            optionList.addAll(Arrays.asList(cleardataOption));

            paramsHelper = new ParametersHelper(optionList);
            this.parsedOptions = paramsHelper.parse(args,
                    this.getClass().getName());

            this.refreshToken = (String) parsedOptions.get("refreshtoken");

            Object clearDataObj = parsedOptions.get("cleardata");
            if (clearDataObj != null) {
                this.clearData = (Boolean) clearDataObj;
            } else {
                this.clearData = false;
            }

            this.configFile =
                    parsedOptions.get("config-file") != null ?
                            (String) parsedOptions.get(
                                "config-file") : null;
                                
             Object vmcServerObj = parsedOptions.get("vmcserver");
             if (vmcServerObj != null) {
                 this.vmcServer = (String) vmcServerObj;
             } else {
                 this.vmcServer = "vmc.vmware.com";
             }

             Object cspServerObj = parsedOptions.get("cspserver");
             if (cspServerObj != null) {
                 this.cspServer = (String) cspServerObj;
             } else {
                 this.cspServer = "console.cloud.vmware.com";
             }

        } catch (ParseException pex) {
            System.out.println(pex.getMessage());
            System.exit(0);
        } catch (ConfigurationException cex) {
            System.out.println(cex.getMessage());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public String getVmcServer() {
        return this.vmcServer;
    }

    public String getCspServer() {
        return this.cspServer;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public boolean isClearData() {
        return clearData;
    }

    public String getConfigFile() {
        return configFile;
    }

    public Map<String, Object> getParsedOptions() {
        return this.parsedOptions;
    }

    /**
     * Define the options for the sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    protected abstract void parseArgs(String[] args);

    /**
     * Setup any resources required for the sample run.
     * @throws Exception
     */
    protected abstract void setup() throws Exception;

    /**
     * Run the sample
     * @throws Exception
     */
    protected abstract void run() throws Exception;

    /**
     * Clean up any sample data created as part of the sample run.
     * @throws Exception
     */
    protected abstract void cleanup() throws Exception;

    /**
     * Builds the Http settings to be applied for the connection to the server.
     * @return http configuration
     * @throws Exception 
     */
    protected HttpConfiguration buildHttpConfiguration() throws Exception {
        HttpConfiguration httpConfig =
            new HttpConfiguration.Builder().getConfig();
        
        return httpConfig;
    }

    /**
     * Logs out of the server
     * @throws Exception
     */
    protected void logout() throws Exception {
        // No logout required
    }
    
    /**
     * Executes the sample using the command line arguments or parameters from
     * the configuration file. Execution involves the following steps:
     * 1. Parse the arguments required by the sample
     * 2. Login to the server
     * 3. Setup any resources required by the sample run
     * 4. Run the sample
     * 5. Cleanup any data created by the sample run, if cleanup=true
     * 6. Logout of the server
     *
     * @param args command line arguments passed to the sample
     * @throws Exception
     */
    protected void execute(String[] args) throws Exception {
        // Parse the command line arguments or the configuration file
        parseArgs(args);

        // Setup any resources required by the sample
        setup();

        // Execute the sample
        run();

        if (clearData) {
            // Clean up the sample data
            cleanup();
        }
    }
}
