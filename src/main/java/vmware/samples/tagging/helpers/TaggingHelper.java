/*
 * *******************************************************
 * Copyright VMware, Inc. 2014, 2016.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package vmware.samples.tagging.helpers;

import java.util.HashSet;
import java.util.Set;

import com.vmware.cis.tagging.CategoryTypes;
import com.vmware.cis.tagging.TagTypes;
import com.vmware.cis.tagging.CategoryModel.Cardinality;
import com.vmware.cis.tagging.CategoryTypes.CreateSpec;
import com.vmware.cis.tagging.TagTypes.UpdateSpec;

/**
 * Helper class to create specs for various Tagging vAPIs.
 */
public class TaggingHelper {
    public static CreateSpec createTagCategorySpec(String name,
            String description, Cardinality cardinality) {
        CategoryTypes.CreateSpec createSpec = new CategoryTypes.CreateSpec();
        createSpec.setName(name);
        createSpec.setDescription(description);
        createSpec.setCardinality(cardinality);

        Set<String> associableTypes = new HashSet<String>();
        createSpec.setAssociableTypes(associableTypes); // empty set of
                                                        // associableTypes
        return createSpec;
    }

    public static com.vmware.cis.tagging.TagTypes.CreateSpec createTagSpec(
            String name, String description, String categoryId) {
        TagTypes.CreateSpec createSpec = new TagTypes.CreateSpec();
        createSpec.setName(name);
        createSpec.setDescription(description);
        createSpec.setCategoryId(categoryId);
        return createSpec;
    }

    public static UpdateSpec updateTagSpec(String tagId, String description) {
        TagTypes.UpdateSpec updateSpec = new TagTypes.UpdateSpec();
        updateSpec.setDescription(description);
        return updateSpec;
    }
}
