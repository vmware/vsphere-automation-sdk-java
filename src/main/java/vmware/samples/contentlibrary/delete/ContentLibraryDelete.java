/*
 * *******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.contentlibrary.delete;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;

import com.vmware.content.LibraryModel;
import com.vmware.content.LibraryTypes;
import com.vmware.content.library.StorageBacking;

import vmware.samples.common.SamplesAbstractBase;
import vmware.samples.contentlibrary.client.ClsApiClient;
import vmware.samples.vcenter.helpers.DatastoreHelper;

public class ContentLibraryDelete extends SamplesAbstractBase {
	
    private String dsName;
    private String libName;
    private ClsApiClient client;
    private StorageBacking sbacking;
    
    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */    
    @Override
	protected void parseArgs(String[] args) {
        // Parse the command line options or use config file
        Option libNameOption = Option.builder()
            .required(false)
            .hasArg()
            .argName("CONTENT LIBRARY")
            .longOpt("contentlibraryname")
            .desc("OPTIONAL: If provided Content Library with this name"
            		+ " will be deleted.")
            .build();
        Option dsNameOption = Option.builder()
            .required(true)
            .hasArg()
            .argName("DATASTORE")
            .longOpt("datastore")
            .desc("REQUIRED: Content Librarie(s) under this VC datastore"
            		+ " will be deleted.")
            .build();
        List<Option> optionList = Arrays.asList(dsNameOption, libNameOption);
        super.parseArgs(optionList, args);
        this.dsName = (String) parsedOptions.get("datastore");
        this.libName = (String) parsedOptions.get("contentlibraryname");
    }

    @Override
	protected void setup() throws Exception {
        this.client = new ClsApiClient(this.vapiAuthHelper.getStubFactory(),
                sessionStubConfig);
    	//Get the Storage Backing on the Data Store
    	//Compare it with the Content Library's to Identify
    	//if it that can be deleted. 
        this.sbacking = DatastoreHelper.createStorageBacking(
                this.vapiAuthHelper, this.sessionStubConfig,
                this.dsName );
    }

    @Override
	protected void run() throws Exception {
    	
    	if(!StringUtils.isBlank(this.libName)){
            LibraryTypes.FindSpec findSpec = new LibraryTypes.FindSpec();
            findSpec.setName(this.libName);
            List<String> libraryIds = this.client.libraryService().
                find(findSpec);
            assert !libraryIds.isEmpty() : 
            	"Unable to find a library with name: "+ this.libName;
            String libraryId = libraryIds.get(0);
            System.out.println("Found library : " + libraryId);
            deleteContentLibrary(libraryId);
    		
    	}else
    	{
    		//Delete All the Content Libraries in the Datastore 
    		//Managed by this VC
            List<String> visibleCls = client.localLibraryService().list();
            System.out.println("All libraries : " + visibleCls);
            for (String libId : visibleCls) {
            	deleteContentLibrary(libId);
            }
    	}
    }
    
    /**
     * Deletes the Content Library matching the library ID and 
     * within the DataStore his.sbacking
     * @param libraryId
     */
    private void deleteContentLibrary(String libraryId){
    	boolean canDelete = false;
        //Retrieve the local content library
    	LibraryModel localLibrary = this.client.localLibraryService().
        		get(libraryId);
        for (Iterator<StorageBacking> iterator = 
            localLibrary.getStorageBackings().iterator(); iterator
            .hasNext();) {
            StorageBacking sbackingtmp = (StorageBacking) iterator.next();
            if(this.sbacking.equals(sbackingtmp))
            {
                canDelete = true;
                break;
            }
        }
        if(canDelete)
        {
            this.client.localLibraryService().delete(libraryId);
            System.out.println("Deleted Content Library : "+libraryId);
        }else {
            System.out.println("Can not delete Content Library : "+libraryId);
        }
    }

    @Override
    protected void cleanup() throws Exception {

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
        new ContentLibraryDelete().execute(args);
    }

}