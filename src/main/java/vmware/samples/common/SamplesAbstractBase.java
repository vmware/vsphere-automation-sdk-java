/*
 * *******************************************************
 * Copyright VMware, Inc. 2013, 2016.  All Rights Reserved.
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

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration.KeyStoreConfig;
import com.vmware.vapi.protocol.HttpConfiguration.SslConfiguration;

import vmware.samples.common.authentication.VapiAuthenticationHelper;
import vmware.samples.common.authentication.VimAuthenticationHelper;

/**
 * Abstract base class for vSphere samples
 *
 */
public abstract class SamplesAbstractBase {
    protected String server;
    protected String username;
    protected String password;
    protected boolean clearData;
    protected boolean skipServerVerification;
    protected String truststorePath;
    protected String truststorePassword;
    protected String configFile;
    protected VimAuthenticationHelper vimAuthHelper;
    protected VapiAuthenticationHelper vapiAuthHelper;
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
            Option serverOption = Option.builder()
                                        .required(true)
                                        .hasArg()
                                        .argName("SERVER")
                                        .longOpt("server")
                                        .desc("hostname of vCenter Server")
                                        .build();

            Option usernameOption = Option.builder()
                                          .required(true)
                                          .hasArg()
                                          .argName("USERNAME")
                                          .longOpt("username")
                                          .desc("username to login to the "
                                                  + "vCenter Server")
                                          .build();

            Option passwordOption = Option.builder()
                                          .required(true)
                                          .hasArg()
                                          .argName("PASSWORD")
                                          .longOpt("password")
                                          .desc("password to login to the "
                                                  + "vCenter Server")
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

            Option truststorePathOption =
                    Option.builder()
                          .required(false)
                          .hasArg()
                          .argName("ABSOLUTE PATH OF JAVA TRUSTSTORE FILE")
                          .longOpt("truststorepath")
                          .desc("Specify the absolute path to the file "
                               + "containing the trusted server certificates. "
                               + "This option can be skipped if the parameter "
                               + "skip-server-verification is specified.")
                          .build();

            Option truststorePasswordOption =
                    Option.builder()
                          .required(false)
                          .hasArg()
                          .argName("JAVA TRUSTSTORE PASSWORD")
                          .longOpt("truststorepassword")
                          .desc("Specify the password for the java "
                               + "truststore. This option can be skipped if "
                               + "the parameter skip-server-verification is "
                               + "specified.")
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
                configFileOption, serverOption, usernameOption,
                passwordOption));
            optionList.addAll(sampleOptions);
            optionList.addAll(Arrays.asList( truststorePathOption,
                truststorePasswordOption, cleardataOption,
                skipServerVerificationOption));

            paramsHelper = new ParametersHelper(optionList);
            this.parsedOptions = paramsHelper.parse(args,
                    this.getClass().getName());

            this.server = (String) parsedOptions.get("server");
            this.username = (String) parsedOptions.get("username");
            this.password = (String) parsedOptions.get("password");
            Object clearDataObj = parsedOptions.get("cleardata");
            if (clearDataObj != null) {
                this.clearData = (Boolean) clearDataObj;
            } else {
                this.clearData = false;
            }

            Object skipServerVerificationObj =
                    parsedOptions.get("skip-server-verification");
            if(skipServerVerificationObj != null) {
                this.skipServerVerification =
                        (Boolean) skipServerVerificationObj;
            } else {
                this.skipServerVerification = false;
            }

            Object truststorePathObj = parsedOptions.get("truststorepath");
            if(truststorePathObj != null) {
                this.truststorePath =
                        (String) parsedOptions.get("truststorepath");
            }

            Object truststorePasswordObj =
                    parsedOptions.get("truststorepassword");
            if(truststorePasswordObj != null) {
                this.truststorePassword =
                        (String) parsedOptions.get("truststorepassword");
            }

            this.configFile =
                    parsedOptions.get("config-file") != null ?
                            (String) parsedOptions.get(
                                "config-file") : null;

            // Check if truststorePath and truststorePassword are specified
            if(!this.skipServerVerification && (
                    this.truststorePath == null ||
                    this.truststorePassword == null)) {
                throw new ConfigurationException(
                    "The parameters truststorepath and truststorepassword "
                    + "need to be specified for server certificate "
                    + " verification. These are required"
                    + " parameters if the parameter skip-server-verification "
                    + "has not been specified.");
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

    public String getServer() {
        return this.server;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isClearData() {
        return clearData;
    }

    public boolean isSkipServerVerification() {
        return skipServerVerification;
    }

    public String getTruststorePath() {
        return truststorePath;
    }

    public String getTruststorePassword() {
        return truststorePassword;
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
     * Creates a session with the server using username/password.
     *
     *<p><b>
     * Note: If the "skip-server-verification" option is specified, then this
     * method trusts the SSL certificate from the server and doesn't verify
     * it. Circumventing SSL trust in this manner is unsafe and should not be
     * used with production code. This is ONLY FOR THE PURPOSE OF DEVELOPMENT
     * ENVIRONMENT
     * <b></p>
     * @throws Exception
     */
    protected void login() throws Exception {
        this.vapiAuthHelper = new VapiAuthenticationHelper();
        this.vimAuthHelper = new VimAuthenticationHelper();
        HttpConfiguration httpConfig = buildHttpConfiguration();
        this.sessionStubConfig =
                vapiAuthHelper.loginByUsernameAndPassword(
                    this.server, this.username, this.password, httpConfig);
        this.vimAuthHelper.loginByUsernameAndPassword(
                    this.server, this.username, this.password);
    }

    /**
     * Builds the Http settings to be applied for the connection to the server.
     * @return http configuration
     * @throws Exception 
     */
    protected HttpConfiguration buildHttpConfiguration() throws Exception {
        HttpConfiguration httpConfig =
            new HttpConfiguration.Builder()
            .setSslConfiguration(buildSslConfiguration())
            .getConfig();
        
        return httpConfig;
	}

	/**
     * Builds the SSL configuration to be applied for the connection to the
     * server
     * 
     * For vApi connections:
     * If "skip-server-verification" is specified, then the server certificate
     * verification is skipped. The method retrieves the certificate
     * from specified server and adds it to an in-memory trustStore which is
     * returned.
     * If "skip-server-verification" is not specified, then it uses the
     * truststorepath and truststorepassword to load the truststore and return
     * it.
     *
     * For VIM connections:
     * If "skip-server-verification" is specified, then it trusts all the
     * VIM API connections made to the specified server.
     * If "skip-server-verification" is not specified, then it sets the System
     * environment property "javax.net.ssl.trustStore" to the path of the file
     * containing the trusted server certificates.
     *
     *<p><b>
     * Note: Below code circumvents SSL trust if "skip-server-verification" is
     * specified. Circumventing SSL trust is unsafe and should not be used
     * in production software. It is ONLY FOR THE PURPOSE OF DEVELOPMENT
     * ENVIRONMENTS.
     *<b></p>
     * @return SSL configuration
     * @throws Exception
     */
    protected SslConfiguration buildSslConfiguration() throws Exception {
        SslConfiguration sslConfig;

        if(this.skipServerVerification) {
            /*
             * Below method enables all VIM API connections to the server
             * without validating the server certificates.
             *
             * Note: Below code is to be used ONLY IN DEVELOPMENT ENVIRONMENTS.
             * Circumventing SSL trust is unsafe and should not be used in
             * production software.
             */
            SslUtil.trustAllHttpsCertificates();

            /*
             * Below code enables all vAPI connections to the server
             * without validating the server certificates..
             *
             * Note: Below code is to be used ONLY IN DEVELOPMENT ENVIRONMENTS.
             * Circumventing SSL trust is unsafe and should not be used in
             * production software.
             */
            sslConfig = new SslConfiguration.Builder()
            		.disableCertificateValidation()
            		.disableHostnameVerification()
            		.getConfig();
        } else {
            /*
             * Set the system property "javax.net.ssl.trustStore" to
             * the truststorePath
             */
            System.setProperty("javax.net.ssl.trustStore", this.truststorePath);
            KeyStore trustStore =
                SslUtil.loadTrustStore(this.truststorePath,
                		this.truststorePassword);
            KeyStoreConfig keyStoreConfig =
            		new KeyStoreConfig("", this.truststorePassword);
            sslConfig =
            		new SslConfiguration.Builder()
            		.setKeyStore(trustStore)
            		.setKeyStoreConfig(keyStoreConfig)
            		.getConfig();
        }

        return sslConfig;
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

            if (clearData) {
                // Clean up the sample data
                cleanup();
            }

        } finally {
            // Logout of the server
            logout();
        }
    }
}
