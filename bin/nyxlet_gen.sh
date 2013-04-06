#******************************************************************************
# Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification,
# are permitted provided that the following conditions are met:
#
#    Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
#    Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
#    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
#    may be used to endorse or promote products derived from this software without
#    specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
# LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
# OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
# OF THE POSSIBILITY OF SUCH DAMAGE.
#*******************************************************************************
#!/bin/bash

SCRIPT_DIR="$(dirname $0)"
PNAME=`echo "$1" | tr '[:upper:]' '[:lower:]'`
PACKAGE_NAME=`echo "$2" | tr '[:upper:]' '[:lower:]'`

if [ "${PNAME}" == "" ]; then
    echo "ERROR: Please provide nyxlet name."
    exit 1
fi

PNAME_DIR=nyxlet-${PNAME}
if [ -a "${PNAME_DIR}" ]; then
    echo "ERROR: File or directory \"${PNAME_DIR}\" already exist. Cannot create nyxlet template."
    exit 1
fi

# Check if this repo already exists
gitResult=`git ls-remote "git@github.com:cycladessoa/${PNAME_DIR}.git" 2> /dev/null | wc -l`
echo $gitResult
if [ $gitResult -ne 0 ]; then
    echo "ERROR: The specified nyxlet name \"${PNAME_DIR}\" is already in-used by another GIT repository. Please choose a different name."
    exit 1
fi

if [ "${PACKAGE_NAME}" == "" ]; then
    PACKAGE_NAME="org.cyclades.nyxlet"
fi
PACKAGE_DIR=`echo $PACKAGE_NAME | sed 's/\./\//g'`

mkdir -p ${PNAME_DIR}

mkdir -p ${PNAME_DIR}/src/main/java/${PACKAGE_DIR}/${PNAME}/actionhandler
cp ${SCRIPT_DIR}/.templates/Main.java ${PNAME_DIR}/src/main/java/${PACKAGE_DIR}/${PNAME}/
cp ${SCRIPT_DIR}/.templates/HelloWorldActionHandler.java ${PNAME_DIR}/src/main/java/${PACKAGE_DIR}/${PNAME}/actionhandler/
cp ${SCRIPT_DIR}/.templates/HelloWorldChainableActionHandler.java ${PNAME_DIR}/src/main/java/${PACKAGE_DIR}/${PNAME}/actionhandler/
mkdir -p ${PNAME_DIR}/src/test/java/${PACKAGE_DIR}/${PNAME}
cp ${SCRIPT_DIR}/.templates/NyxletBaseJUnitTest.java ${PNAME_DIR}/src/test/java/${PACKAGE_DIR}/${PNAME}/
cp ${SCRIPT_DIR}/.templates/*.gradle ${PNAME_DIR}/
cp ${SCRIPT_DIR}/.templates/gradle.properties ${PNAME_DIR}/
cp ${SCRIPT_DIR}/.templates/INSTALL    ${PNAME_DIR}/
cp ${SCRIPT_DIR}/.templates/REQUEST_EXAMPLES    ${PNAME_DIR}/
cp ${SCRIPT_DIR}/.templates/README     ${PNAME_DIR}/
cp ${SCRIPT_DIR}/.templates/doc_gen.sh ${PNAME_DIR}/
cp -r ${SCRIPT_DIR}/.templates/config  ${PNAME_DIR}/
cp -r ${SCRIPT_DIR}/.templates/conf    ${PNAME_DIR}/
cp -r ${SCRIPT_DIR}/.templates/docs    ${PNAME_DIR}/
find ${PNAME_DIR}/ -type f | while read file; do
    cat $file | sed "s/PACKAGE_NAME/${PACKAGE_NAME}/g" > ${file}.new
    mv ${file}.new ${file}
    cat $file | sed "s/NYXLET_NAME/${PNAME}/g" > ${file}.new
    mv ${file}.new ${file}
done

touch ${PNAME_DIR}/README.TXT

#cd ${PNAME_DIR}/ && git init && git add -A && git commit -m 'Project created from template'
cd ${PNAME_DIR}/ && git init && git add -A 
chmod a+x doc_gen.sh
git commit -a -m 'Project created from template'

cat <<EOF

The Nyxlet template has been created for you in the directory "${PNAME_DIR}"

Now there are a set of steps and notes you need to take before proceeding with coding:

1. There has been a set of template SOA documentation generated for you in the "docs"
   directory within your template.

2. The default package name is ${PACKAGE_NAME}.

3. You have some example Action Handlers 
   ${PNAME_DIR}/src/main/java/${PACKAGE_DIR}/${PNAME}/actionhandler/HelloWorldActionHandler.java and 
   ${PNAME_DIR}/src/main/java/${PACKAGE_DIR}/${PNAME}/actionhandler/HelloWorldChainableActionHandler.java
   which you can modify and/or copy and paste to create more Action Handlers. Action
   Handlers are the primary code point for adding functionality to your service.

4. X-STROMA service requests can be created and executed with the Cyclades Java DSL. Example HTTP service requests can be executed with the following commands:

     gradle javaClientDSLExample
       Runs a single X-STROMA request
     gradle javaClientDSLExamples
       Runs mutiple high level X-STROMA request examples
     gradle javaClientDSLExample_STROMA
       Runs a STROMA request (direct service request, not via X-STROMA)

    Please see the following file for the HTTP client examples: /cycladessoa/nyxlets/nyxlet-my_first_nyxlet/cyclades_java_client_dsl.gradle

   Example Message Queue X-STROMA service requests and general client to queue access can be executed with the following commands (you must have at least one Message Queue installed to use its targets):

     gradle rabbitMQConsumerTargetExample/activeMQConsumerTargetExample
       Runs a multi threaded consumer for a specified queue
       Run this command first to initialize the queues for RabbitMQ
     gradle rabbitMQXSTROMAProducerTargetExample/activeMQXSTROMAProducerTargetExample
       Produces a X-STROMA message to the specified queue
       Good example of how to submit an asynchronous X-STROMA request to a Cyclades instance consuming from the specified queue
     gradle rabbitMQTextProducerTargetExample/activeMQTextProducerTargetExample
       Produces a Text message to the specified queue
     gradle rabbitMQBinaryProducerTargetExample/activeMQBinaryProducerTargetExample
       Produces a binary message to the specified queue

    Please see the following file for the Message Queue client examples: /cycladessoa/nyxlets/nyxlet-my_first_nyxlet/cyclades_java_client_servicebroker_targets.gradle

   [NOTE]
   Please feel free to modify and experiment with these Groovy/Gradle targets. Gradle and/or Groovy provide a convenient mechanism for rapidly developing clients using the Cyclades Java DSL, and clients in general.

or

   Please see the REQUEST_EXAMPLES file for some high level examples on how to make external requests
   to your Nyxlet once deployed. You can simply cut and paste the examples to a browser. Further documentation
   will be provided for advanced Nyxlet invocations, like Java APIs for making STROMA/X-STROMA requests
   from within a Nyxlet.

5. You can modify ${PNAME_DIR}/build.gradle to include additional compile and runtime dependencies.

6. The local GIT repository has been initialized for your project "${PNAME_DIR}" 

LASTLY, Read and follow the INSTALL and README files for further instruction
NOTE: This is a one-time setup

EOF
