package vmware.samples.vmc.custom;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vapi.client.ApiClient;
//import com.vmware.vmc.orgs.AccountLink;
import com.vmware.vmc.orgs.account_link.ConnectedAccounts;
//import com.vmware.vmc.orgs.account_link.SddcConnections;

import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcAuthenticationHelper;

public class VMCCustomerOps extends VmcSamplesAbstractBase {
    private ConnectedAccounts connectedAccounts;    
    private ApiClient apiClient;
   // private SddcConnections sddcConnections;
    private String orgId, sddcName/*, sddcId*/;


	@Override
	protected void parseArgs(String[] args) {
        Option orgOption = Option.builder()
                .longOpt("org_id")
                .desc("Specify the organization id")
                .argName("ORGANIZATION ID")
                .required(true)
                .hasArg()
                .build();
        Option sddcOption = Option.builder()
                .longOpt("sddc_name")
                .desc("Specify the name of the sddc to be created")
                .argName("SDDC NAME")
                .required(true)
                .hasArg()
                .build();    
        List<Option> optionList = Arrays.asList(orgOption, sddcOption);

        super.parseArgs(optionList, args);
        this.orgId = (String) parsedOptions.get("org_id");
        this.sddcName = (String) parsedOptions.get("sddc_name");
	}

	@Override
	protected void setup() throws Exception {
        this.vmcAuthHelper = new VmcAuthenticationHelper();
        this.apiClient = this.vmcAuthHelper.newVmcClient(this.vmcServer, this.cspServer, this.refreshToken);
        this.connectedAccounts = apiClient.createStub(ConnectedAccounts.class);
        //this.sddcConnections = apiClient.createStub(SddcConnections.class);
	}

	@Override
	protected void run() throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("Hello");
		//AccountLink accountLink = apiClient.createStub(AccountLink.class);
	    //accountLink.get(orgId);

		//connectedAccounts.get(orgId, "AWS");
		//sddcConnections.get(orgId, sddcId);
		System.out.println(connectedAccounts.get(orgId, null));
		//System.out.println(sddcConnections.get(orgId, sddcId));
		System.out.println(sddcName);

	}

	@Override
	protected void cleanup() throws Exception {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
        new VMCCustomerOps().execute(args);


	}

}