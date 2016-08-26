/*
 * *******************************************************
 * Copyright VMware, Inc. 2013, 2016.  All Rights Reserved.
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

import vmware.samples.common.authentication.VapiAuthenticationHelper;
import vmware.samples.common.authentication.VimAuthenticationHelper;

/**
 * Abstract base class for all samples
 *
 */
public abstract class SamplesAbstractBase {
    protected String server;
    protected String username;
    protected String password;
    protected String clearData = "true";
    protected String configFile;
    protected VimAuthenticationHelper vimAuthHelper;
    protected VapiAuthenticationHelper vapiAuthHelper;
    protected StubConfiguration sessionStubConfig;
    protected Map<String, Object> parsedOptions = null;

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
            Option serverOption = Option.builder()
                                        .required(true)
                                        .hasArg()
                                        .argName("SERVER")
                                        .longOpt("server")
                                        .desc("hostname of management node")
                                        .build();

            Option usernameOption = Option.builder()
                                          .required(true)
                                          .hasArg()
                                          .argName("USERNAME")
                                          .longOpt("username")
                                          .desc("username to login to the "
                                                  + "management node")
                                          .build();

            Option passwordOption = Option.builder()
                                          .required(true)
                                          .hasArg()
                                          .argName("PASSWORD")
                                          .longOpt("password")
                                          .desc("password to login to the "
                                                  + "management node")
                                          .build();

            Option cleardataOption = Option.builder()
                                           .required(true)
                                           .hasArg()
                                           .argName("true | false")
                                           .longOpt("cleardata")
                                           .desc("Set to true to clear up all "
                                                   + "the sample data "
                                                   + "after the run.")
                                           .build();

            List<Option> optionList = new ArrayList<>(Arrays.asList(
                serverOption, usernameOption, passwordOption, cleardataOption));
            optionList.addAll(sampleOptions);
            paramsHelper = new ParametersHelper(optionList);
            this.parsedOptions = paramsHelper.parse(args);
            this.configFile = (String)parsedOptions.get("config-file");
            this.server = (String) parsedOptions.get("server");
            this.username = (String) parsedOptions.get("username");
            this.password = (String) parsedOptions.get("password");
            this.clearData = (String) parsedOptions.get("cleardata");
        } catch (ParseException pex) {
            System.out.println(pex.getMessage());
            paramsHelper.printUsage();
            System.exit(0);
        } catch (ConfigurationException cex) {
            System.out.println(cex.getMessage());
            paramsHelper.printUsage();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public String getServer() {
        return this.server;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getClearData() {
        return this.clearData;
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
     * Creates a session with the server using username/password.
     * @throws Exception
     */
    protected void login() throws Exception {
        this.vapiAuthHelper = new VapiAuthenticationHelper();
        this.vimAuthHelper = new VimAuthenticationHelper();
        this.sessionStubConfig =
                vapiAuthHelper.loginByUsernameAndPassword(
                    this.server, this.username, this.password);
        this.vimAuthHelper.loginByUsernameAndPassword(
                    this.server, this.username, this.password);
    }

    /**
     * Logs out of the server
     * @throws Exception
     */
    protected void logout() throws Exception {
        this.vapiAuthHelper.logout();
        this.vimAuthHelper.logout();
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
        try {
            // Parse the command line arguments or the configuration file
            parseArgs(args);

            // Login to the server
            login();

            // Setup any resources required by the sample
            setup();

            // Execute the sample
            run();
            
            if (clearData.equalsIgnoreCase("true")) {
                // Clean up the sample data
               cleanup(); 
            }

        } finally {
            // Logout of the server
            logout();
        }
    }
}
