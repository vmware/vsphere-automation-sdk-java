/*
 * *******************************************************
 * Copyright VMware, Inc. 2022.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vlcm;

import com.vmware.cis.Tasks;
import com.vmware.cis.TasksTypes;
import com.vmware.cis.task.Info;
import com.vmware.cis.task.Status;
import com.vmware.esx.settings.AddOnSpec;
import com.vmware.esx.settings.BaseImageSpec;
import com.vmware.esx.settings.clusters.Software;
import com.vmware.esx.settings.clusters.software.Drafts;
import com.vmware.esx.settings.clusters.software.drafts.software.Components;
import com.vmware.esx.settings.clusters.software.drafts.software.AddOn;
import com.vmware.esx.settings.clusters.software.DraftsTypes.FilterSpec;
import com.vmware.esx.settings.clusters.software.DraftsTypes.CommitSpec;
import com.vmware.esx.settings.clusters.SoftwareTypes;
import com.vmware.esx.settings.clusters.software.drafts.software.BaseImage;
import com.vmware.esx.settings.clusters.software.DraftsTypes.Summary;
import com.vmware.vcenter.services.Service;
import com.vmware.vcenter.services.ServiceTypes;
import org.apache.commons.cli.Option;
import vmware.samples.common.SamplesAbstractBase;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/*
    Code to run the vLCM image operations
    Pre-requisites to run the sample
    1) Vcenter version >= 7.0
    2) For running stage cluster task, esx >=8.0
    3) Cluster should be a vLCM cluster
*/

public class ImageOperations extends SamplesAbstractBase {

    private String service;
    private Service serviceApiStub;
    private Map<String, ServiceTypes.Info> servicesList;
    private Software softwareStub;
    private Tasks tasks;
    private Drafts drafts;
    public static void main(String[] args) throws Exception{
        new ImageOperations().execute(args);
    }

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
        this.softwareStub = vapiAuthHelper.getStubFactory().createStub(Software.class, sessionStubConfig);
        this.tasks = vapiAuthHelper.getStubFactory().createStub(Tasks.class, sessionStubConfig);
        this.drafts = vapiAuthHelper.getStubFactory()
                .createStub(com.vmware.esx.settings.clusters.software.Drafts.class, sessionStubConfig);

    }

    private com.vmware.cis.task.Info getTaskInfo(String taskId) {
        TasksTypes.GetSpec.Builder taskSpecBuilder = new TasksTypes.GetSpec.Builder();
        com.vmware.cis.task.Info info = tasks.get(taskId, taskSpecBuilder.build());
        return info;
    }

    private String runStageTask(String clusterId) {
        /*
            Run the stage task with the set desired image
            Returns task id of the format
            52f9f1b0-160a-25fe-fd1e-e7526df80e0d:com.vmware.esx.settings.clusters.software
         */
        SoftwareTypes.StageSpec.Builder builder = new SoftwareTypes.StageSpec.Builder();
        String stageTaskId = softwareStub.stage_Task(clusterId, builder.build());
        return stageTaskId;
    }

    private String runApplyTask(String clusterId) {
        /*
            Run the apply task with the set desired image
            Returns task id of the format
            52f9f1b0-160a-25fe-fd1e-e7526df80e0d:com.vmware.esx.settings.clusters.software
         */
        com.vmware.esx.settings.clusters.SoftwareTypes.ApplySpec.Builder builder
                = new SoftwareTypes.ApplySpec.Builder();
        builder.setAcceptEula(true);
        String taskId = softwareStub.apply_Task(clusterId, builder.build());
        return taskId;
    }

    private String runScanTask(String clusterId) {
        /*
            Run the apply task with the set desired image
            Returns task id of the format
            52f9f1b0-160a-25fe-fd1e-e7526df80e0d:com.vmware.esx.settings.clusters.software
         */
        String taskId = softwareStub.scan_Task(clusterId);
        return taskId;
    }

    private String getTaskResult(String taskId) throws InterruptedException {
        /*
            Gets the result of the task specified.
            Keeps polling till the task either fails or succeeds.
            Sample result of the Task
            com.vmware.esx.settings.cluster_compliance =>
            {incompatible_hosts=[], hosts=[map-entry => {value=com.vmware.esx.settings.host_compliance =>
                {components=[map-entry =>
                    {value=com.vmware.esx.settings.component_compliance =>
                        {current=<unset>, target_source=USER, current_source=<unset>, stage_status=NOT_STAGED,
                        notifications=com.vmware.esx.settings.notifications =>
                        {warnings=<unset>, errors=<unset>, info=<unset>},
                        status=NON_COMPLIANT,
                        target=com.vmware.esx.settings.component_info =>
                        {details=com.vmware.esx.settings.component_details =>
                        {display_version=2.0, vendor=VMware, display_name=test-component-8}, version=2.0-1}},
                         key=test-component-8}],
                        solutions=[], impact=REBOOT_REQUIRED, commit=5,
                        add_on=com.vmware.esx.settings.add_on_compliance =>
                        {current=<unset>, stage_status=<unset>, notifications=com.vmware.esx.settings.notifications =>
                        {warnings=<unset>, errors=<unset>, info=<unset>}, status=COMPLIANT, target=<unset>},
                        stage_status=NOT_STAGED,
                        hardware_support=[], base_image=com.vmware.esx.settings.base_image_compliance =>
                        {current=com.vmware.esx.settings.base_image_info =>
                        {details=com.vmware.esx.settings.base_image_details =>
                        {live_update_compatible_versions=<unset>, release_date=2022-11-09T04:52:15.825Z,
                        display_version=8.0  - 20764643, display_name=ESXi}, version=8.0.1-0.0.20764643},
                        stage_status=<unset>,
                        notifications=com.vmware.esx.settings.notifications =>
                        {warnings=<unset>, errors=<unset>, info=<unset>},
                        status=COMPLIANT, target=com.vmware.esx.settings.base_image_info =>
                        {details=com.vmware.esx.settings.base_image_details =>
                        {live_update_compatible_versions=<unset>,
                        release_date=2022-11-09T04:52:15.825Z, display_version=8.0  - 20764643, display_name=ESXi},
                        version=8.0.1-0.0.20764643}},
                        data_processing_units_compliance=<unset>, solution_impacts=<unset>,
                        scan_time=2022-12-14T03:52:16.999Z,
                        notifications=com.vmware.esx.settings.notifications => {warnings=<unset>, errors=<unset>,
                        info=[com.vmware.esx.settings.notification => {retriable=<unset>,
                        id=com.vmware.vcIntegrity.lifecycle.HostScan.QuickBoot.Supported, originator=<unset>,
                        time=2022-12-14T03:52:16.762Z, message=com.vmware.vapi.std.localizable_message =>
                        {args=[], default_message=Quick Boot is supported on the host.,
                        localized=Quick Boot is supported on the host.,
                        id=com.vmware.vcIntegrity.lifecycle.HostScan.QuickBoot.Supported, params=<unset>},
                        type=INFO, resolution=<unset>},
                        com.vmware.esx.settings.notification =>
                        {retriable=<unset>, id=com.vmware.vcIntegrity.lifecycle.HostScan.RebootImpact,
                        originator=<unset>,
                        time=2022-12-14T03:52:16.999Z, message=com.vmware.vapi.std.localizable_message =>
                        {args=[], default_message=The host will be rebooted during remediation.,
                        localized=The host will be rebooted during remediation.,
                        id=com.vmware.vcIntegrity.lifecycle.HostScan.RebootImpact, params=<unset>},
                        type=INFO, resolution=<unset>}]}, status=NON_COMPLIANT}, key=host-16}],
                        non_compliant_hosts=[host-16],
                        impact=REBOOT_REQUIRED, commit=5, compliant_hosts=[],
                        scan_time=2022-12-14T03:52:16.999Z, stage_status=NOT_STAGED,
                        unavailable_hosts=[], notifications=com.vmware.esx.settings.notifications =>
                        {warnings=<unset>, errors=<unset>, info=<unset>},
                        host_info=[map-entry =>
                        {value=com.vmware.esx.settings.host_info => {is_vsan_witness=<unset>,
                        name=10.10.10.10}, key=host-16}],
                        status=NON_COMPLIANT}
         */
        Info info;
        String res;
        do {
            info = getTaskInfo(taskId);
            res = String.valueOf(info.getResult());
            System.out.println("Task {"+taskId+"} current status is:"+String.valueOf(info.getStatus()));
            Thread.sleep(1000);
        } while(info.getStatus() != Status.FAILED && info.getStatus() != Status.SUCCEEDED);
        return res;
    }

    public String setBaseImage(String clusterId, String baseImageVersion) {
        /*
            Sets the baseImageVersion for the given cluster.
         */
        removeExistingDraftsFromCluster(clusterId);
        String draftId = drafts.create(clusterId);
        BaseImageSpec baseImageSpec = new BaseImageSpec();
        baseImageSpec.setVersion(baseImageVersion);
        BaseImage draftsBaseImage = vapiAuthHelper.getStubFactory().createStub(BaseImage.class, sessionStubConfig);
        draftsBaseImage.set(clusterId, draftId, baseImageSpec);
        commitSpec(clusterId, draftId, "Committing the base image");
        return draftId;
    }

    public void setComponent(String clusterId, String component, String compVersion) {
        /*
            Adds the specified addon to the desired image of the clusterId.
         */
        removeExistingDraftsFromCluster(clusterId);
        String draftId = drafts.create(clusterId);
        Components draftsComponents
                = vapiAuthHelper.getStubFactory().createStub(Components.class, sessionStubConfig);
        draftsComponents.set(clusterId, draftId, component, compVersion);
        commitSpec(clusterId, draftId, "Setting the component");

    }


    public void removeComponent(String clusterId, String component) {
        /*
            Removes the specified component from the desired image of the clusterId.
         */
        removeExistingDraftsFromCluster(clusterId);
        String draftId = drafts.create(clusterId);
        Components draftsComponents
                = vapiAuthHelper.getStubFactory().createStub(Components.class, sessionStubConfig);
        draftsComponents.delete(clusterId, draftId, component);
        commitSpec(clusterId, draftId, "Removing the component");
    }

    public void setAddon(String clusterId, String addonName, String addonVersion) {
        /*
            Adds the specified addon to the desired image of the clusterId.
         */
        removeExistingDraftsFromCluster(clusterId);
        String draftId = drafts.create(clusterId);
        AddOn draftsAddon = vapiAuthHelper.getStubFactory().createStub(AddOn.class, sessionStubConfig);
        AddOnSpec addOnSpec = new AddOnSpec();
        addOnSpec.setName(addonName);
        addOnSpec.setVersion(addonVersion);
        draftsAddon.set(clusterId, draftId, addOnSpec);
        commitSpec(clusterId, draftId, "Committing the addon");
    }

    public void removeAddon(String clusterId, String addonName, String addonVersion) {
        /*
            Removes the specified addon from the desired image of the clusterId.
         */
        removeExistingDraftsFromCluster(clusterId);
        String draftId = drafts.create(clusterId);
        AddOn draftsAddon = vapiAuthHelper.getStubFactory().createStub(AddOn.class, sessionStubConfig);
        AddOnSpec addOnSpec = new AddOnSpec();
        addOnSpec.setName(addonName);
        addOnSpec.setVersion(addonVersion);
        draftsAddon.delete(clusterId, draftId);
        commitSpec(clusterId, draftId, "Removing the addon");
    }

    public void removeExistingDraftsFromCluster(String clusterId) {
        /*
            One cluster can have only one pending draft.
            This function removes the pending draft for a new cluster
            so that we can create a new one.
         */
        FilterSpec filterSpec = new FilterSpec();
        Set<String> owners = new HashSet<>();
        owners.add(this.getUsername());
        filterSpec.setOwners(owners);
        Map<String, Summary> listOfDrafts
                = drafts.list(clusterId, filterSpec);
        for(String key:listOfDrafts.keySet()){
            drafts.delete(clusterId, key);
        }
    }

    void commitSpec(String clusterId, String draftId, String commitMessage) {
        /*
            Commit the vlcm draft with the given draftId.
         */
        CommitSpec commitSpec = new CommitSpec();
        commitSpec.setMessage(commitMessage);
        drafts.commit_Task(clusterId, draftId, commitSpec);
    }

    @Override
    protected void run() throws Exception {
        // Entry point to test the fu

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
}
