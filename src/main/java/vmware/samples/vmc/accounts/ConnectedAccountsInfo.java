package vmware.samples.vmc.accounts;


import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.vapi.client.ApiClient;
import com.vmware.vmc.model.AwsCustomerConnectedAccount;
import com.vmware.vmc.orgs.account_link.ConnectedAccounts;
import vmware.samples.common.VmcSamplesAbstractBase;
import vmware.samples.common.authentication.VmcAuthenticationHelper;

public class ConnectedAccountsInfo extends VmcSamplesAbstractBase {
    private ConnectedAccounts connectedAccounts;    
    private ApiClient apiClient;
    private String orgId;

    @Override
    protected void parseArgs(String[] args) {
        Option orgOption = Option.builder()
                .longOpt("org_id")
                .desc("Specify the organization id")
                .argName("ORGANIZATION ID")
                .required(true)
                .hasArg()
                .build();
    
        List<Option> optionList = Arrays.asList(orgOption);
        super.parseArgs(optionList, args);
        this.orgId = (String) parsedOptions.get("org_id");
    }

    @Override
    protected void setup() throws Exception {
        this.vmcAuthHelper = new VmcAuthenticationHelper();
        this.apiClient = this.vmcAuthHelper.newVmcClient(this.vmcServer, this.cspServer, this.refreshToken);
        this.connectedAccounts = apiClient.createStub(ConnectedAccounts.class);
    }

    @Override
    protected void run() throws Exception {
        List<AwsCustomerConnectedAccount> awsCuAccounts = connectedAccounts.get(orgId, "AWS");
        for (AwsCustomerConnectedAccount awsCuAccount : awsCuAccounts) {
            System.out.println("Account Details\n");
            System.out.println("Account Number : "+awsCuAccount.getAccountNumber());
            System.out.println("Username : "+awsCuAccount.getUserName());
            System.out.println("UserId : "+awsCuAccount.getUserId());
            System.out.println("Last Updated Username : "+awsCuAccount.getUpdatedByUserName());
            System.out.println("Cloud Formation Stack Name : "+awsCuAccount.getCfStackName());
            System.out.println("----------------------------\n");
        }
    }

    @Override
    protected void cleanup() throws Exception {
        // No cleanup required

    }

    public static void main(String[] args) throws Exception {
                new ConnectedAccountsInfo().execute(args);
    }

}
