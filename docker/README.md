# vSphere Automation SDK for Java Dockerfile

This contains a dockerfile to deploy the vSphere Automation SDK for Java and be up and running in seconds, this will allow you to simply try out the samples or produce your applications in a containerized environment.

## Building the container

Install a docker client for your chosen OS and run the following command to build the container directly from this repository:

```console
docker build https://github.com/vmware/vsphere-automation-sdk-java.git#:docker -t vmware/vsphere-automation-sdk-java
```

## Run the Container

Once the Container has been successfully built, you can run it using the following command:

```console
$ docker run --rm -it vmware/vsphere-automation-sdk-java
```

At this point you are now logged into the container that you have just built and you can run the samples as per the following "listVMs" example:

```console
root@b5f3a6e06c2f:/work/vsphere-automation-sdk-java# java -ea -cp target/vsphere-samples-6.5.0.jar vmware.samples.vcenter.vm.list.ListVMs --server myvcserver.mydomain.com --username administrator@mydomain.com --password Admin\!23 --skip-server-verification
----------------------------------------
List of VMs
Summary (com.vmware.vcenter.VM.summary) => {
    vm = vm-23,
    name = VM01,
    powerState = POWERED_OFF,
    cpuCount = 1,
    memorySizeMiB = 1024
}
----------------------------------------
```
