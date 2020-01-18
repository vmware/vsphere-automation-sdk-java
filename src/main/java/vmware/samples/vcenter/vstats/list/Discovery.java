
/*
 * *******************************************************
 * Copyright VMware, Inc. 2020.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package vmware.samples.vcenter.vstats.list;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.cli.Option;

import com.vmware.vstats.CounterMetadata;
import com.vmware.vstats.Counters;
import com.vmware.vstats.CountersTypes;
import com.vmware.vstats.Metrics;
import com.vmware.vstats.Providers;
import com.vmware.vstats.ResourceAddressSchemas;
import com.vmware.vstats.ResourceAddressSchemasTypes;
import com.vmware.vstats.ResourceTypes;
import com.vmware.vstats.ResourceTypesTypes;
import com.vmware.vstats.CounterSetsTypes;
import com.vmware.vstats.MetricsTypes;
import com.vmware.vstats.ProvidersTypes;
import com.vmware.vstats.CounterMetadataTypes;
import com.vmware.vstats.CounterSets;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates all vStats discovery APIs which give current state
 * of the system.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: vCenter 7.0x with 7.0x ESXi hosts
 */
public class Discovery extends SamplesAbstractBase {

	private Counters counterService;
	private ResourceAddressSchemas resourceAddrSchemaService;
	private CounterMetadata counterMetadataService;
	private Providers providerService;
	private ResourceTypes resourceTypesService;
	private Metrics metricsService;
	private CounterSets counterSetsService;
	private static final String printLine = "\n-------------------------------";

	/**
	 * Define the options specific to this sample and configure the sample using
	 * command-line arguments or a config file
	 *
	 * @param args command line arguments passed to the sample
	 */
	protected void parseArgs(String[] args) {

		List<Option> optionList = Collections.<Option>emptyList();
		super.parseArgs(optionList, args);
	}

	protected void setup() throws Exception {

		this.counterService = vapiAuthHelper.getStubFactory()
				.createStub(Counters.class, sessionStubConfig);
		this.resourceAddrSchemaService = vapiAuthHelper.getStubFactory()
				.createStub(ResourceAddressSchemas.class, sessionStubConfig);
		this.counterMetadataService = vapiAuthHelper.getStubFactory()
				.createStub(CounterMetadata.class, sessionStubConfig);
		this.providerService = vapiAuthHelper.getStubFactory()
				.createStub(Providers.class, sessionStubConfig);
		this.resourceTypesService = vapiAuthHelper.getStubFactory()
				.createStub(ResourceTypes.class, sessionStubConfig);
		this.metricsService = vapiAuthHelper.getStubFactory()
				.createStub(Metrics.class, sessionStubConfig);
		this.counterSetsService = vapiAuthHelper.getStubFactory()
				.createStub(CounterSets.class, sessionStubConfig);
	}

	protected void run() throws Exception {

		List<CountersTypes.Info> listCountersInfo = counterService.list(null);
		System.out.println(printLine);
		System.out.println("List of vStats supported counters: \n");
		for (CountersTypes.Info counterInfo : listCountersInfo) {
			System.out.println(counterInfo);
		}
		System.out.println(printLine);

		// Choose a random counter.
		Random rand = new Random();
		CountersTypes.Info counter = listCountersInfo
				.get(rand.nextInt(listCountersInfo.size()));
		System.out.println("\nThe Counter is: \n" + counter);
		System.out.println(printLine);
		String cid = counter.getCid();

		// List of counter metadata associated with that counter.
		List<CounterMetadataTypes.Info> counterMetadata = counterMetadataService
				.list(cid, null);
		System.out.println("\nList of Counter Metadata: \n" + counterMetadata);
		System.out.println(printLine);
		String mid = counterMetadata.get(0).getMid();

		// Get resource address schema associated with that counter.
		String rsrcAddrSchemaID = counter.getResourceAddressSchema();
		ResourceAddressSchemasTypes.Info resourceAddrSchema = resourceAddrSchemaService
				.get(rsrcAddrSchemaID);
		System.out.println(
				"\nResource Address Schema is: \n" + resourceAddrSchema);
		System.out.println(printLine);

		// List of vStats providers connected to vCenter Server.
		List<ProvidersTypes.Summary> providers = providerService.list();
		System.out.println("\nList of vStats providers: \n" + providers);
		System.out.println(printLine);

		// List of resource types supported by vStats.
		List<ResourceTypesTypes.Summary> resourceTypes = resourceTypesService
				.list();
		System.out.println("\nList of vStats supported resource types: \n"
				+ resourceTypes);
		System.out.println(printLine);

		// List of metrics supported by vStats.
		List<MetricsTypes.Summary> metrics = metricsService.list();
		System.out.println("\nList of vStats supported metrics: \n" + metrics);
		System.out.println(printLine);

		// List of vStats defined Counter-sets.
		List<CounterSetsTypes.Info> counterSets = counterSetsService.list();
		System.out.println(
				"\nList of vStats defined Counter-sets: \n" + counterSets);
		System.out.println(printLine);
	}

	protected void cleanup() throws Exception {

		// No cleanup required.
	}

	public static void main(String[] args) throws Exception {

		/*
		 * Execute the sample using the command line arguments or parameters
		 * from the configuration file. This executes the following steps:
		 * 1.Parse the arguments required by the sample
		 * 2.Login to the server
		 * 3.Setup any resources required by the sample run
		 * 4.Run the sample
		 * 5.Cleanup any data created by the sample run, if cleardata flag is
		 * provided
		 * 6.Logout of the server
		 */
		new Discovery().execute(args);
	}
}
