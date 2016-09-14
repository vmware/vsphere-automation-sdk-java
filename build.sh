# This script is used for downloading the latest SDK and vapi jars from buildweb for building the samples
#!/bin/bash
product=vsphereautomationsdkjava
branch=main

buildweb_url="https://buildapi.eng.vmware.com/sb/build/?product=$product&branch=$branch&ondisk=True&buildstate=succeeded&_format=json&_order_by=-id&_limit=1"
echo BUILDWEB URL=$buildweb_url

deliverables_url=`curl -S $buildweb_url | python -c "import sys, json; print(json.load(sys.stdin)['_list'][0]['_deliverables_url'])"`
echo $deliverables_url

build_number=`echo $deliverables_url | cut -d= -f2`

echo "PRODUCT=$product" > buildinfo.txt
echo "BRANCH=$branch" >> buildinfo.txt
echo "BUILD NUMBER=$build_number" >> buildinfo.txt

#download the SDK zip file
wget -O VMware-vSphere-Automation-SDK-Java-6.5.0-$build_number.zip http://buildweb.eng.vmware.com/sb/api/$build_number/deliverable/?file=publish/VMware-vSphere-Automation-SDK-Java-6.5.0-$build_number.zip

# Refresh the libs with the latest ones
rm -rf lib
unzip -j VMware-vSphere-Automation-SDK-Java-6.5.0-$build_number.zip 'VMware-vSphere-Automation-SDK-Java/lib/*' -d './lib/'
rm -rf VMware-vSphere-Automation-SDK-Java-6.5.0-$build_number.zip

mvn initialize
mvn clean
mvn install
