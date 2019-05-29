package vmware.samples.fedramp;

import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;

import com.vmware.cis.tagging.Tag;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration.KeyStoreConfig;
import com.vmware.vapi.protocol.HttpConfiguration.SslConfiguration;
import com.vmware.vapi.saml.SamlToken;

import vmware.samples.common.ParametersHelper;
import vmware.samples.common.SslUtil;
import vmware.samples.common.authentication.VapiAuthenticationHelper;

public class FedrampAuthenticationFlow {
    private Tag taggingService;
    private String vCenterServer;
    private String refreshToken;
    private String cspServer;
    protected String vcenterTruststorePath;
    protected String vcenterTruststorePassword;    
    private VapiAuthenticationHelper vapiAuthHelper;
    private Map<String, Object> parsedOptions;
	private Boolean skipServerVerification;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    public void parseArgs(String[] args) {
    	try {
	    	ParametersHelper paramsHelper = null;
            Option serverOption = Option.builder()
                    .required(true)
                    .hasArg()
                    .argName("vCENTER SERVER")
                    .longOpt("vCenterServer")
                    .desc("hostname or IP address of vCenter Server")
                    .build();

	        Option refreshToken = Option.builder()
	                .longOpt("refreshToken")
	                .desc("VMC refresh token")
	                .argName("VMC refresh token")
	                .required(true)
	                .hasArg()
	                .build();
	        
	        Option cspServer = Option.builder()
	                .longOpt("cspServer")
	                .desc("hostname or IP address of CSP Server")
	                .argName("CSP SERVER")
	                .required(true)
	                .hasArg()
	                .build();
            
	        Option skipServerVerificationOption = Option.builder()
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
            Option truststorePathOption = Option.builder()
                    .required(false)
                    .hasArg()
                    .argName("ABSOLUTE PATH OF JAVA TRUSTSTORE FILE")
                    .longOpt("vcenterTruststorePath")
                    .desc("Specify the absolute path to the file "
                         + "containing the trusted server certificates. "
                         + "This option can be skipped if the parameter "
                         + "skip-server-verification is specified.")
                    .build();

            Option truststorePasswordOption = Option.builder()
                    .required(false)
                    .hasArg()
                    .argName("JAVA TRUSTSTORE PASSWORD")
                    .longOpt("vCenterTruststorePassword")
                    .desc("Specify the password for the java "
                         + "truststore. This option can be skipped if "
                         + "the parameter skip-server-verification is "
                         + "specified.")
                    .build();

	        List<Option> optionList = Arrays.asList(serverOption, refreshToken,
	        		cspServer, truststorePathOption, truststorePasswordOption,
	        		skipServerVerificationOption);
	        paramsHelper = new ParametersHelper(optionList);

	        this.parsedOptions = paramsHelper.parse(args,
	                this.getClass().getName());        
	        this.vCenterServer = (String) parsedOptions.get("vCenterServer");
	        this.cspServer = (String) parsedOptions.get("cspServer");
	        this.refreshToken = (String) parsedOptions.get("refreshToken");
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
                this.vcenterTruststorePath =
                        (String) parsedOptions.get("truststorepath");
            }

            Object truststorePasswordObj =
                    parsedOptions.get("truststorepassword");
            if(truststorePasswordObj != null) {
                this.vcenterTruststorePassword =
                        (String) parsedOptions.get("truststorepassword");
            }

            // Check if truststorePath and truststorePassword are specified
            if(!this.skipServerVerification && (
                    this.vcenterTruststorePath == null ||
                    this.vcenterTruststorePassword == null)) {
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

    public void run() throws Exception {

        System.out.println("\n\n#### Example: Login to vCenter server with "
                           + "embedded Platform Services Controller");

        this.vapiAuthHelper = new VapiAuthenticationHelper();

        HttpConfiguration httpConfig = buildHttpConfiguration();

        System.out.println("\nStep 1: Retrieve the SAML bearer token.");
        SamlToken samlBearerToken = SamlTokenUtil.getSamlBearerToken(this.vCenterServer,
        		this.cspServer, this.refreshToken);        

        System.out.println("\nStep 2. Login to vAPI services using the "
                           + "SAML bearer token.");
        StubConfiguration sessionStubConfig =
                this.vapiAuthHelper.loginBySamlBearerToken(this.vCenterServer, samlBearerToken, httpConfig);

        System.out.println("\nStep 3: Perform certain tasks using the vAPI "
                           + "services.");
        this.taggingService = this.vapiAuthHelper.getStubFactory()
            .createStub(Tag.class, sessionStubConfig);
        System.out.println("\nListing all tags on the vcenter server..");
        List<String> tagList = this.taggingService.list();
        if(tagList.isEmpty()) {
            System.out.println("\nNo tags found !");
        } else {
            System.out.println("\nTag Name\tTag Description");
            for(String tagId : tagList) {
                System.out.println(this.taggingService.get(tagId).getName()
                        + "\t" + this.taggingService.get(tagId).getDescription());
            }
        }
        vapiAuthHelper.logout();
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
     * If "skip-server-verification" is specified, then the server certificate
     * verification is skipped. The method retrieves the certificate
     * from specified server and adds it to an in-memory trustStore which is
     * returned.
     * If "skip-server-verification" is not specified, then it uses the
     * truststorepath and truststorepassword to load the truststore and return
     * it.
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
            System.setProperty("javax.net.ssl.trustStore", this.vcenterTruststorePath);
            KeyStore trustStore =
                SslUtil.loadTrustStore(this.vcenterTruststorePath,
                		this.vcenterTruststorePassword);
            KeyStoreConfig keyStoreConfig =
            		new KeyStoreConfig("", this.vcenterTruststorePassword);
            sslConfig =
            		new SslConfiguration.Builder()
            		.setKeyStore(trustStore)
            		.setKeyStoreConfig(keyStoreConfig)
            		.getConfig();
        }

        return sslConfig;
    }

    public static void main(String[] args) throws Exception {
    	FedrampAuthenticationFlow fedrampAuth = null;
    	fedrampAuth = new FedrampAuthenticationFlow();

        // Parse the command line arguments or the configuration file
    	fedrampAuth.parseArgs(args);

        // Execute the sample
    	fedrampAuth.run();
    }

}
