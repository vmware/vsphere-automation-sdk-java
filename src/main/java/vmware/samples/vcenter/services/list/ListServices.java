package vmware.samples.vcenter.services.list;

import com.vmware.vcenter.services.Service;
import com.vmware.vcenter.services.ServiceTypes;
import org.apache.commons.cli.Option;
import vmware.samples.common.SamplesAbstractBase;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Description: Demonstrates getting list of Services present in vCenter
 * Author: VMware, Inc.
 * Sample Prerequisites: vCenter 6.5+
 * Sample Requirements:
 *  1 vCenter Server
 *  2 ESX hosts
 *  1 datastore
 */
public class ListServices extends SamplesAbstractBase {
    private String service;
    private Service serviceApiStub;
    private Map<String, ServiceTypes.Info> servicesList;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    @Override
    protected void parseArgs(String[] args) {
        Option serviceOption = Option.builder()
                .longOpt("service")
                .desc("OPTIONAL: Specify the name of the service"
                        + " whose state is being queried.")
                .argName("SERVICE")
                .required(false)
                .hasArg()
                .build();
        List<Option> optionList = Arrays.asList(serviceOption);
        super.parseArgs(optionList, args);
        this.service = (String) parsedOptions.get("service");
    }

    @Override
    protected void setup() throws Exception {
        this.serviceApiStub =
                vapiAuthHelper.getStubFactory()
                        .createStub(Service.class, sessionStubConfig);
    }

    @Override
    protected void run() throws Exception {

        // List all vCenter services' information if service name is not specified
        if(service == null || service.isEmpty() ){
            //Get the list of all services on the server
            servicesList = serviceApiStub.listDetails();
            for (Map.Entry<String, ServiceTypes.Info> svc : servicesList.entrySet()) {
                formattedOutputDisplay(svc.getValue(), svc.getKey());
            }
        }
        // List information of service provided as arg
        else
        {
            ServiceTypes.Info serviceInfo = serviceApiStub.get(this.service);
            formattedOutputDisplay(serviceInfo, this.service);
        }
    }

    protected void formattedOutputDisplay(ServiceTypes.Info info, String serviceName) {
        System.out.println("-----------------------------");
        System.out.println("Service Name : " + serviceName);
        System.out.println("Service Name Key : " + info.getNameKey());
        System.out.println("Service Health : " + info.getHealth());
        System.out.println("Service Status : " + info.getState());
        System.out.println("Service Startup Type : " + info.getStartupType());
    }

    @Override
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
        new ListServices().execute(args);
    }
}
