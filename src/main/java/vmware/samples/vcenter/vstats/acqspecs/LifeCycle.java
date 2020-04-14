
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

package vmware.samples.vcenter.vstats.acqspecs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.cli.Option;

import com.vmware.vstats.AcqSpecs;
import com.vmware.vstats.AcqSpecsTypes.CreateSpec;
import com.vmware.vstats.CidMid;
import com.vmware.vstats.AcqSpecsTypes.CounterSpec;
import com.vmware.vstats.AcqSpecsTypes.ListResult;
import com.vmware.vstats.AcqSpecsTypes.UpdateSpec;
import com.vmware.vstats.Providers;
import com.vmware.vstats.ProvidersTypes.Summary;
import com.vmware.vstats.RsrcId;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates create, get, list, update and delete operations of
 * Acquisition Specifications.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: vCenter 7.0x with 7.0x ESXi hosts
 */
public class LifeCycle extends SamplesAbstractBase {

	private Providers providerService;
	private AcqSpecs acqSpecService;
	private String acqSpecId;
	private long _interval, _expiration;
	private static final String cid = "cpu.capacity.demand.HOST";
	private static final String new_counterId = "mem.capacity.usage.HOST";
	private static final String hostType = "HOST";
	private static final String memo = "user-definition of Acquisition "
			+ "Specification";
	private static final String printLine = "\n-------------------------------";

	/**
	 * Define the options specific to this sample and configure the sample using
	 * command-line arguments or a config file
	 *
	 * @param args command line arguments passed to the sample
	 */
	protected void parseArgs(String[] args) {

		Option interval = Option.builder()
				.longOpt("interval")
				.desc("Create an Acquisition Specification to collect data "
						+ "with interval")
				.argName("INTERVAL")
				.required(true)
				.hasArg()
				.build();
		Option expiration = Option.builder()
				.longOpt("expiration")
				.desc("Create an Acquisition Specification that expires within"
						+ " expiration time")
				.argName("EXPIRATION")
				.required(true)
				.hasArg()
				.build();
		List<Option> optionList = Arrays.asList(interval, expiration);
		super.parseArgs(optionList, args);
		this._interval = Long.parseLong((String) parsedOptions.get("interval"));
		this._expiration = Long
				.parseLong((String) parsedOptions.get("expiration"));
	}

	protected void setup() throws Exception {

		this.providerService = vapiAuthHelper.getStubFactory()
				.createStub(Providers.class, sessionStubConfig);
		this.acqSpecService = vapiAuthHelper.getStubFactory()
				.createStub(AcqSpecs.class, sessionStubConfig);
	}

	protected void run() throws Exception {

		CreateSpec createSpec = new CreateSpec();
		CounterSpec counterSpec = new CounterSpec();
		List<RsrcId> resourceSpec = new ArrayList<RsrcId>();
		CidMid cidMid = new CidMid();

		// The counter and associated resources can be chosen using discovery
		// APIs. Please refer to samples in discovery package to obtain this
		// metadata. In this sample, we create an Acquisition Specification
		// for a HOST counter.

		cidMid.setCid(cid);
		counterSpec.setCidMid(cidMid);
		createSpec.setCounters(counterSpec);

		// Choose a random host from which stats data needs to be collected.
		List<Summary> providersList = providerService.list();
		Random rand = new Random();
		String hostIdValue = providersList
				.get(rand.nextInt(providersList.size())).getId();
		RsrcId hostRsrcId = new RsrcId();
		hostRsrcId.setIdValue(hostIdValue);
		hostRsrcId.setType(hostType);
		resourceSpec.add(hostRsrcId);
		createSpec.setResources(resourceSpec);

		// Choose an interval and expiration.
		createSpec.setInterval(_interval);
		createSpec.setExpiration(_expiration);
		createSpec.setMemo_(memo);

		// Create an Acquisition Specification.
		acqSpecId = acqSpecService.create(createSpec);
		System.out.println(printLine);
		System.out.println("Acquisition Specification created is \n"
				+ acqSpecService.get(acqSpecId) + "\nwith id: " + acqSpecId);
		System.out.println(printLine);

		// List Acquisition Specifications.
		ListResult acqSpecsList = acqSpecService.list(null);
		System.out.println(
				"\nList of Acquisition Specifications: \n" + acqSpecsList);
		System.out.println(printLine);

		// Update the existing Acquisition Specification by only modifying the
		// intended field in UpdateSpec, keeping all other fields as it is.
		UpdateSpec updateSpec = new UpdateSpec();
		updateSpec.setResources(createSpec.getResources());
		updateSpec.setCounters(createSpec.getCounters());
		updateSpec.setInterval(createSpec.getInterval());
		updateSpec.setExpiration(createSpec.getExpiration());
		updateSpec.setMemo_(createSpec.getMemo_());

		// Update the cid field in the already created Acquisition
		// Specification previously.
		CounterSpec updatedCounterSpec = new CounterSpec();
		CidMid updatedCidMid = new CidMid();
		updatedCidMid.setCid(new_counterId);
		updatedCounterSpec.setCidMid(updatedCidMid);
		updateSpec.setCounters(updatedCounterSpec);
		acqSpecService.update(acqSpecId, updateSpec);
		System.out.println("\nThe updated Acquisition Specification is: \n"
				+ acqSpecService.get(acqSpecId));
		System.out.println(printLine);

		// Delete the Acquisition Specification.
		acqSpecService.delete(acqSpecId);
		System.out.println("\nThe Acquisition Specification with id: "
				+ acqSpecId + " is deleted");
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
		new LifeCycle().execute(args);
	}
}
