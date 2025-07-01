ImageOperations.java contains samples for making vSphere lifecyle manager API calls:

### Testbed Requirement:
    - 1 vCenter Server
    - 2 Datacenter in the vCenter Server
    - 3 vLCM cluster in the datacenter
    - 4 Esx hosts in the cluster
    - 5 All the vlcm depots required for the desired image should be present

### Setting the desired image
Sample                                                                | Description
----------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
setDraftsBaseImage(clusterId, baseImageVersion)                       | Sets the baseImageVersion in the desired image for the given cluster
setComponent(clusterId, compName, compVersion)                        | Sets the component of the given version in the desired image for the given cluster
removeComponent(clusterId, compName)                                  | Removes the component from the desired image of the cluster
setAddon(clusterId, addonName, addonVersion)                          | Sets the addon in the desired image for the given cluster
removeAddon(clusterId, addonName, addonVersion)                       | Removes the addon from the desired image of the cluster

### Running scan, stage and remediate tasks
Sample                                                                      | Description
----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------
runApplyTask(clusterId)                                                     | Scan the cluster with the currently set desired image
runStageTask(String clusterId)                                              | Stage the current desired image on all the hosts of a cluster
runApplyTask(String clusterId)                                              | Remediate all the hosts in the cluster with the set desired image
getTaskInfo(String taskId)                                                  | Tries to poll the result of the task till it fails or succeeds