This directory contains samples for managing vSphere stats platform and querying for stats data:

### vStats Discovery APIs - List Counters, List Counter Metadata, Get Resource Address Schema, List Providers, List Resource types, List metrics, List Counter-sets
Sample                                                                      | Description
----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------
vmware.samples.vcenter.vstats.list.Discovery.java                           | Demonstrates all vStats discovery APIs which give current state of the system.


### vStats Acquisition Specification Create/List/Delete/Update operations
Sample                                                                      | Description
----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------
vmware.samples.vcenter.vstats.acqspecs.LifeCycle.java                       | Demonstrates create, get, list, update and delete operations of Acquisition Specifications.


### vStats End to End workflow - Create an Acquisition Specification and query for data points
Sample                                                                      | Description
----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------
vmware.samples.vcenter.vstats.data.QueryDataPoints.java                     | Demonstrates creation of Acquisition Specification and query for data points filtered by cid.
vmware.samples.vcenter.vstats.data.QueryDataPointsWithPredicate.java        | Demonstrates creation of Acquisition Specification using Counter SetId and query for data points filtered by cid.
vmware.samples.vcenter.vstats.data.QueryDataPointsWithSetID.java            | Demonstrates creation of Acquisition Specification using QueryPredicate "ALL" and query for data points filtered by resource.


### Testbed Requirement:
    - 1 vCenter Server on version 7.0x or higher
    - 2 ESXi hosts on version 7.0x or higher with VMs
    - 1 datastore