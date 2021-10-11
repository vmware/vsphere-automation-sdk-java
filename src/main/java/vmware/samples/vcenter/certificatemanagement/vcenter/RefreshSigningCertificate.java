/*
 * *******************************************************
 * Copyright VMware, Inc. 2021.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.vcenter.certificatemanagement.vcenter;

import com.vmware.vcenter.certificate_management.X509CertChain;
import com.vmware.vcenter.certificate_management.vcenter.SigningCertificate;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Sample code to refresh the Signing Certificate for the vCenter Server.
 * Use the force option to attempt to force the refresh in environments
 * that would otherwise fail. On success, the new signing certificates
 * will be printed.
 */
public class RefreshSigningCertificate extends SamplesAbstractBase {
    private SigningCertificate certService;
    protected boolean force;

    @Override
    protected void parseArgs(String args[]) {
        Option forceOption = Option.builder()
            .required(false)
            .argName("FORCE")
            .longOpt("force")
            .desc("Attempt to force refresh")
            .build();
        List<Option> optionList = Arrays.asList(forceOption);
        super.parseArgs(optionList, args);

        this.force = parsedOptions.get("force") != null;
    }

    @Override
    protected void setup() throws Exception {
        this.certService =
            vapiAuthHelper.getStubFactory().createStub(SigningCertificate.class,
                                                         sessionStubConfig);
    }

    @Override
    protected void run() throws Exception {
        X509CertChain newCert= certService.refresh(this.force);
        if(newCert == null ) {
            System.out.println("ERROR: refresh signing certificate did not return a certificate");
        } else {
            System.out.println("New vCenter signing certificate \n"+newCert.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        RefreshSigningCertificate get = new RefreshSigningCertificate();
        get.execute(args);
    }

    @Override
    protected void cleanup() throws Exception {
        // No cleanup required
    }
}
