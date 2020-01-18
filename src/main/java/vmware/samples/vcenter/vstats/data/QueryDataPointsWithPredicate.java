
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

package vmware.samples.vcenter.vstats.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.Option;

import com.vmware.vstats.AcqSpecsTypes.CreateSpec;
import com.vmware.vstats.CidMid;
import com.vmware.vstats.Data;
import com.vmware.vstats.AcqSpecs;
import com.vmware.vstats.AcqSpecsTypes.CounterSpec;
import com.vmware.vstats.DataTypes.DataPointsResult;
import com.vmware.vstats.DataTypes.FilterSpec;
import com.vmware.vstats.Providers;
import com.vmware.vstats.ProvidersTypes;
import com.vmware.vstats.QueryPredicate;
import com.vmware.vstats.RsrcId;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates creation of Acquisition Specification using Query
 * Predicate "ALL" and query for data points filtered by cid.
 *
 * Author: VMware, Inc.
 * Sample Prerequisites: vCenter 7.0x with 7.0x ESXi hosts
 */
public class QueryDataPointsWithPredicate extends SamplesAbstractBase {

	private Providers providerService;
	private AcqSpecs acqSpecService;
	private Data dataService;
	private String acqSpecId;
	private long _interval, _expiration;
	private static final String cid = "disk.throughput.usage.VM";
	private static final int WAIT_TIME = 30;
	private static final String vmType = "VM", hostType = "HOST";
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

		this.acqSpecService = vapiAuthHelper.getStubFactory()
				.createStub(AcqSpecs.class, sessionStubConfig);
		this.dataService = vapiAuthHelper.getStubFactory()
				.createStub(Data.class, sessionStubConfig);
		this.providerService = vapiAuthHelper.getStubFactory()
				.createStub(Providers.class, sessionStubConfig);
	}

	protected void run() throws Exception {

		CreateSpec createSpec = new CreateSpec();
		CounterSpec counterSpec = new CounterSpec();
		List<RsrcId> resourceSpec = new ArrayList<RsrcId>();
		CidMid cidMid = new CidMid();

		// Choose a random VM counter.
		cidMid.setCid(cid);
		counterSpec.setCidMid(cidMid);
		createSpec.setCounters(counterSpec);

		// To collect stats data from all VMs on a particular host, provide
		// QueryPredicate "ALL" for VM resourceId.
		Random rand = new Random();
		List<ProvidersTypes.Summary> providersList = providerService.list();
		String hostIdValue = providersList
				.get(rand.nextInt(providersList.size())).getIdValue();
		RsrcId hostRsrcId = new RsrcId();
		hostRsrcId.setIdValue(hostIdValue);
		hostRsrcId.setType(hostType);
		resourceSpec.add(hostRsrcId);
		RsrcId vmRsrcId = new RsrcId();
		vmRsrcId.setIdValue("");
		vmRsrcId.setType(vmType);
		vmRsrcId.setPredicate(QueryPredicate.ALL);
		resourceSpec.add(vmRsrcId);
		createSpec.setResources(resourceSpec);

		// Choose an interval and expiration.
		createSpec.setInterval(_interval);
		createSpec.setExpiration(_expiration);
		createSpec.setMemo_(memo);

		// Create an Acquisition Specification to collect stats data from all
		// VMs on a host using QueryPredicate "ALL".
		acqSpecId = acqSpecService.create(createSpec);
		System.out.println(printLine);
		System.out.println("Acquisition Specification created is \n"
				+ acqSpecService.get(acqSpecId) + "\nwith id: " + acqSpecId);
		System.out.println(printLine);

		// Wait for 30 seconds for data collection to start.
		TimeUnit.SECONDS.sleep(WAIT_TIME);

		// Query for data points filtered by cid.
		FilterSpec filterSpec = new FilterSpec();
		filterSpec.setCid(cid);
		DataPointsResult datapoints = dataService.queryDataPoints(filterSpec);
		System.out.println("Datapoints collected: \n" + datapoints);
	}

	protected void cleanup() throws Exception {

		// Delete the Acquisition Specification.
		acqSpecService.delete(acqSpecId);
		System.out.println("\n----------------------------------------------");
		System.out.println("\nCleanup: The Acquisition Specification with id: "
				+ acqSpecId + " is deleted");
		System.out.println("\n----------------------------------------------");
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
		new QueryDataPointsWithPredicate().execute(args);
	}
}
