This directory contains samples for managing vSphere infrastructure and virtual machines:

### Virtual machine Create/List/Delete operations
Sample                                                                | Description
----------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
vmware.samples.vcenter.vm.list.ListVMs.java                           | Demonstrates how to get list of VMs present in vCenter.
vmware.samples.vcenter.vm.create.defaultvm.CreateDefaultVM.java       | Demonstrates how to create a VM with system provided defaults
vmware.samples.vcenter.vm.create.basicvm.CreateBasicVM.java           | Demonstrates how to create a basic VM with following configuration - 2 disks, 1 nic
vmware.samples.vcenter.vm.create.exhaustivevm.CreateExhaustiveVM.java | Demonstrates how to create a exhaustive VM with the \following configuration - 3 disks, 2 nics, 2 vcpu, 2 GB, memory, boot=BIOS, 1 cdrom, 1 serial port, 1 parallel port, 1 floppy, boot_device=[CDROM, DISK, ETHERNET])

### Virtual machine hardware configuration
Sample                                                                      | Description
----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------
vmware.samples.vcenter.vm.hardware.adapter.SataAdapterConfiguration.java    | Demonstrates how to configure virtual SATA adapters of a virtual machine.
vmware.samples.vcenter.vm.hardware.adapter.ScsiAdapterConfiguration.java    | Demonstrates how to configure virtual SCSI adapters of a virtual machine.
vmware.samples.vcenter.vm.hardware.boot.BootConfiguration.java              | Demonstrates how to configure the settings used when booting a virtual machine.
vmware.samples.vcenter.vm.hardware.bootdevices.BootDeviceConfiguration.java | Demonstrates how to modify the boot devices used by a virtual machine, and in what order they are tried.
vmware.samples.vcenter.vm.hardware.cdrom.CdromConfiguration.java            | Demonstrates how to configure a CD-ROM device for a VM.
vmware.samples.vcenter.vm.hardware.cpu.CpuConfiguration.java                | Demonstrates how to configure a CPU for a VM.
vmware.samples.vcenter.vm.hardware.ethernet.EthernetConfiguration.java      | Demonstrates how to configure virtual ethernet adapters of a virtual machine.
vmware.samples.vcenter.vm.hardware.memory.MemoryConfiguration.java          | Demonstrates how to configure the memory related settings of a virtual machine.


### Testbed Requirement:
    - 1 vCenter Server
    - 2 ESX hosts
    - 1 datastore
    - Some samples need additional configuration like a cluster, vm folder, standard portgroup, iso file on a datastore and distributed portgroup