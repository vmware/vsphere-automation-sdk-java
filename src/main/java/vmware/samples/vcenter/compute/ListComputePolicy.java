package vmware.samples.vcenter.compute;
 
import java.util.Collections;
import java.util.List;
 
import org.apache.commons.cli.Option;
 
import com.vmware.vapi.bindings.Structure;
import com.vmware.vcenter.compute.Policies;
import com.vmware.vcenter.compute.PoliciesTypes;
import vmware.samples.common.SamplesAbstractBase;
 
/**
 * Description: Demonstrates how to list Compute Policies
 *
 * @author singhk
 */
public class ListComputePolicy extends SamplesAbstractBase {
 
    private Policies policies;
 
    @Override
    protected void parseArgs(String[] args) {
        super.parseArgs(Collections.<Option>emptyList(), args);
    }
 
    @Override
    protected void setup() throws Exception {
        // Create the Policies services with authenticated session
        this.policies = vapiAuthHelper.getStubFactory().createStub(Policies.class,
                sessionStubConfig);
    }
 
    @Override
    protected void run() throws Exception {
         List<PoliciesTypes.Summary> pc = this.policies.list();
         System.out.println(pc);
         for (PoliciesTypes.Summary policySummary : pc) {
             Structure structure =  this.policies.get(policySummary.getPolicy());
             if(structure._hasTypeNameOf(
            		 com.vmware.vcenter.compute.policies.capabilities.vm_host_affinity.Info.class)){
                 com.vmware.vcenter.compute.policies.capabilities.vm_host_affinity.Info info =
                 structure._convertTo(
                		 com.vmware.vcenter.compute.policies.capabilities.vm_host_affinity.Info.class);
                 System.out.println(info);
             }else if(structure._hasTypeNameOf(
            		 com.vmware.vcenter.compute.policies.capabilities.vm_host_anti_affinity.Info.class)) {
                 com.vmware.vcenter.compute.policies.capabilities.vm_host_anti_affinity.Info info =
                 structure._convertTo(
                		 com.vmware.vcenter.compute.policies.capabilities.vm_host_anti_affinity.Info.class);
                 System.out.println(info);
             }else if(structure._hasTypeNameOf(
            		 com.vmware.vcenter.compute.policies.capabilities.vm_vm_affinity.Info.class)) {
                 com.vmware.vcenter.compute.policies.capabilities.vm_vm_affinity.Info info =
                 structure._convertTo(
                		 com.vmware.vcenter.compute.policies.capabilities.vm_vm_affinity.Info.class);
                 System.out.println(info);
             }else if(structure._hasTypeNameOf(
            		 com.vmware.vcenter.compute.policies.capabilities.vm_vm_anti_affinity.Info.class)) {
                 com.vmware.vcenter.compute.policies.capabilities.vm_vm_anti_affinity.Info info =
                 structure._convertTo(
                		 com.vmware.vcenter.compute.policies.capabilities.vm_vm_anti_affinity.Info.class);
                 System.out.println(info);
             }
        }
    }
 
    @Override
    protected void cleanup() throws Exception {
 
    }
 
    public static void main(String[] args) throws Exception {
        new ListComputePolicy().execute(args);
 
    }
}