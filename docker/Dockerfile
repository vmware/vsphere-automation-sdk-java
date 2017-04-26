FROM maven:latest
MAINTAINER renoufa@vmware.com

# Set the working directory to /work
WORKDIR /work

# Update and install packages
RUN apt-get update
RUN apt-get -y install vim \
    apt-utils \
    git

# Clone the project
RUN git clone https://github.com/vmware/vsphere-automation-sdk-java.git

# Build the samples
WORKDIR /work/vsphere-automation-sdk-java/
RUN mvn initialize; mvn clean install
CMD ["/bin/bash"]
