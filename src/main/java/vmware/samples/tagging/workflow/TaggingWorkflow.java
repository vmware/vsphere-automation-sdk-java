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

package vmware.samples.tagging.workflow;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.Option;

import com.vmware.cis.tagging.Category;
import com.vmware.cis.tagging.CategoryModel.Cardinality;
import com.vmware.cis.tagging.CategoryTypes;
import com.vmware.cis.tagging.Tag;
import com.vmware.cis.tagging.TagAssociation;
import com.vmware.cis.tagging.TagTypes;
import com.vmware.vapi.std.DynamicID;
import com.vmware.vim25.ManagedObjectReference;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.common.vim.helpers.VimUtil;

/**
 * Descripotion: Demonstrates tagging Create, Read, Update, Delete operations.
 * Step 1: Create a Tag category called Asset.
 * Step 2: Create a Tag called "Server" under the category "Asset".
 * Step 3: Retrieve an existing Cluster using VIM APIs.
 * Step 4: Translates the Cluster's MoRef into vAPI UUID.
 * Step 5: Assign "Server" tag to the Cluster using the UUID.
 *
 * Additional steps when clearData flag is set to TRUE:
 * Step 6: Detach the tag from the Cluster.
 * Step 7: Delete the tag  "Server".
 * Step 8: Delete the tag category "Asset".
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: The sample needs an existing Cluster
 */
public class TaggingWorkflow extends SamplesAbstractBase {
    private String clusterName;
    private Category categoryService;
    private Tag taggingService;
    private TagAssociation tagAssociation;

    private String categoryName = "Asset";
    private String categoryDescription = "All data center assets";
    private String tagName = "Server";
    private String tagDescription = "Cluster running application server";

    private String assetCategoryId;
    private String serverTagId;
    private boolean tagAttached;
    private ManagedObjectReference clusterMoRef;
    private DynamicID clusterDynamicId;

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    public void parseArgs(String[] args) {
        // Parse the command line options or config file
        Option clusterNameOpt = Option.builder()
            .longOpt("cluster")
            .desc("The name of the cluster to be " + "tagged")
            .required(true)
            .hasArg()
            .argName("CLUSTER")
            .build();
        List<Option> optionList = Collections.singletonList(clusterNameOpt);
        super.parseArgs(optionList, args);
        this.clusterName = (String) parsedOptions.get("cluster");
    }

    public void setup() throws Exception {
        this.categoryService =
                this.vapiAuthHelper.getStubFactory().createStub(Category.class,
                                                           sessionStubConfig);
        this.taggingService =
                this.vapiAuthHelper.getStubFactory().createStub(Tag.class,
                                                           sessionStubConfig);
        this.tagAssociation =
                this.vapiAuthHelper.getStubFactory()
                    .createStub(TagAssociation.class,
                                sessionStubConfig);

        // retrieve the Cluster's MoRef
        this.clusterMoRef =
                VimUtil.getCluster(this.vimAuthHelper.getVimPort(),
                                   this.vimAuthHelper.getServiceContent(),
                                   this.clusterName);
        assert this.clusterMoRef != null;
        System.out.println("Cluster MoRef: " + this.clusterMoRef.getValue());
    }

    public void run() throws Exception {

        // List all existing tag categories
        List<String> categories = this.categoryService.list();
        System.out.println("Tag Categories:\n" + categories
                           + "\nEnd of tag categories");

        // List all the existing tags
        List<String> tags = this.taggingService.list();
        System.out.println(" Tags: " + tags + "End of tags");

        // create a new tag category "Asset"
        this.assetCategoryId = createTagCategory(this.categoryName,
                                                 this.categoryDescription,
                                                 Cardinality.MULTIPLE);
        System.out.println("Tag category created; Id: " + this.assetCategoryId);

        // create a new Tag "Server"
        this.serverTagId = createTag(this.tagName, this.tagDescription,
                                     this.assetCategoryId);
        System.out.println("Tag created; Id: " + this.serverTagId);

        // update the asset tag
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
        Date dt = new Date();
        String date = sdf.format(dt); // formats to 09/23/2009 13:53:28.238
        updateTag(this.serverTagId, "Server Tag updated at " + date);
        System.out.println("Tag updated; Id: " + this.serverTagId);

        // convert the MoRef to vAPI DyanmicID
        this.clusterDynamicId =
                new DynamicID(this.clusterMoRef.getType(),
                              this.clusterMoRef.getValue());

        // list all the tags that can be attached to the Cluster
        List<String> attachableTags =
                this.tagAssociation.listAttachableTags(this.clusterDynamicId);
        System.out.println("Attachable Tags:\n" + attachableTags
                           + "\nEnd of Attachable tags");
        assert attachableTags.contains(this.serverTagId);

        // tag the Cluster
        this.tagAssociation.attach(this.serverTagId, this.clusterDynamicId);
        assert this.tagAssociation.listAttachedTags(this.clusterDynamicId)
                             .contains(this.serverTagId);
        this.tagAttached = true;
        System.out.println("Cluster: " + this.clusterDynamicId + " tagged");
    }

    public void cleanup() throws Exception {
        if (this.tagAttached) {
            this.tagAssociation.detach(this.serverTagId, this.clusterDynamicId);
            System.out.println("Cluster: " + this.clusterDynamicId
                                       + " untagged");
        }

        if (this.serverTagId != null) {
            deleteTag(this.serverTagId);
            System.out.println("Tag deleted; Id: " + this.serverTagId);
        }

        if (this.assetCategoryId != null) {
            deleteTagCategory(this.assetCategoryId);
            System.out.println("Tag category deleted; Id: "
                               + this.assetCategoryId);
        }
    }

    /**
     * API to create a category. User who invokes this needs create category
     * privilege.
     *
     * @param name
     * @param description
     * @param cardinality
     * @return
     */
    private String createTagCategory(String name, String description,
                                     Cardinality cardinality) {
        CategoryTypes.CreateSpec createSpec = new CategoryTypes.CreateSpec();
        createSpec.setName(name);
        createSpec.setDescription(description);
        createSpec.setCardinality(cardinality);

        Set<String> associableTypes = new HashSet<String>(); // empty hash set
        createSpec.setAssociableTypes(associableTypes);
        return this.categoryService.create(createSpec);
    }

    /**
     * Deletes an existing tag category; User who invokes this API needs delete
     * privilege on the tag category.
     *
     * @param categoryId
     */
    private void deleteTagCategory(String categoryId) {
        this.categoryService.delete(categoryId);
    }

    /**
     * Creates a tag
     *
     * @param name Display name of the tag.
     * @param description Tag description.
     * @param categoryId ID of the parent category in which this tag will be
     *        created.
     * @return Id of the created tag
     */
    private String createTag(String name, String description,
                             String categoryId) {
        TagTypes.CreateSpec spec = new TagTypes.CreateSpec();
        spec.setName(name);
        spec.setDescription(description);
        spec.setCategoryId(categoryId);

        return this.taggingService.create(spec);
    }

    /**
     * Update the description of an existing tag. User who invokes this API
     * needs edit privilege on the tag.
     *
     * @param tagId the ID of the input tag
     * @param description
     */
    private void updateTag(String tagId, String description) {
        TagTypes.UpdateSpec updateSpec = new TagTypes.UpdateSpec();
        updateSpec.setDescription(description);
        this.taggingService.update(tagId, updateSpec);
    }

    /**
     * Delete an existing tag. User who invokes this API needs delete privilege
     * on the tag.
     *
     * @param tagId the ID of the input tag
     */
    private void deleteTag(String tagId) {
        this.taggingService.delete(tagId);
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
        new TaggingWorkflow().execute(args);
    }
}
