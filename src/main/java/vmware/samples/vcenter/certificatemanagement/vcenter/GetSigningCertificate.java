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

import com.vmware.vcenter.certificate_management.vcenter.SigningCertificate;
import com.vmware.vcenter.certificate_management.vcenter.SigningCertificateTypes.Info;

import java.util.ArrayList;

import org.apache.commons.cli.Option;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Sample code to get the Signing Certificate for the vCenter Server. This
 * will enable users to view the certificate actively used to sign tokens and
 * certificates used for token signature verification.
 */
public class GetSigningCertificate extends SamplesAbstractBase {
    private SigningCertificate certService;

    @Override
    protected void parseArgs(String args[]) {
        super.parseArgs(new ArrayList<Option>(), args);
    }

    @Override
    protected void setup() throws Exception {
        this.certService =
            vapiAuthHelper.getStubFactory().createStub(SigningCertificate.class,
                                                       sessionStubConfig);
    }
    @Override
    protected void run() throws Exception {
        Info certInfo= certService.get();
        if(certInfo == null)
        {
            System.out.println("ERROR: Signing certificates not found on this vCenter");
        }
        System.out.println("vCenter signing certificate \n"+certInfo);
    }

    public static void main(String[] args) throws Exception {
        GetSigningCertificate get = new GetSigningCertificate();
        get.execute(args);
    }

    @Override
    protected void cleanup() throws Exception {
        // No cleanup required
    }
}
