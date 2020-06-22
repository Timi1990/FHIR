#!/usr/bin/env bash

set -eu -o pipefail

###############################################################################
# (C) Copyright IBM Corp. 2020
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

# deploys the binaries to bintray

# Store the current directory to reset to
pushd $(pwd) > /dev/null

# Change to the release directory
cd "$(dirname ${BASH_SOURCE[0]})"

# Import Scripts
source "$(dirname '$0')/logging.sh"
source "$(dirname '$0')/release.properties"

# Basic information
SCRIPT_NAME="$(basename ${BASH_SOURCE[0]})"
debugging "Script Name is ${SCRIPT_NAME}"

# Creates a temporary output file
OUTPUT_FILE=`mktemp`

# Reset to Original Directory
popd > /dev/null

###############################################################################
# Function Declarations:

# deploy_bintray - executes mvn with a set of goals
function deploy_bintray {
    announce "${FUNCNAME[0]}"
    PROJECT_PATH="$1"
    PROFILES="-Pdeploy-bintray,fhir-javadocs"
    TYPE="${2}"

    mvn ${THREAD_COUNT} -ntp -B ${PROFILES} deploy -f ${PROJECT_PATH} -Dbintray.repo=ibm-fhir-server-${TYPE} -DskipTests -s build/release/.m2/settings.xml -Dmaven.wagon.http.retryHandler.count=3
    check_and_fail $? "${FUNCNAME[0]} - stopped - ${PROJECT_PATH}"
}

# upload_to_bintray - uploads to bintray
function upload_to_bintray {
    TYPE="releases"
    MODULE="${1}"
    FILE="${2}"
    FILE_TARGET_PATH="${3}"
    echo ${MODULE} ${FILE} ${FILE_TARGET_PATH}
    echo " - Uploading: ${FILE}"
    
    STATUS=$(curl -T "${FILE}" -u${BINTRAY_USERNAME}:${BINTRAY_PASSWORD} -H "X-Bintray-Package:${MODULE}" -H "X-Bintray-Version:${BUILD_VERSION}" https://api.bintray.com/content/ibm-watson-health/ibm-fhir-server-${TYPE}${FILE_TARGET_PATH} -o ${OUTPUT_FILE} -w '%{http_code}')
    if [ "${STATUS}" -ne "201" ]
    then 
        echo "Debug Information for Upload Failure" 
        cat ${OUTPUT_FILE}
    fi
    
    if [ "${STATUS}" == "413" ]
    then
        # File is too big (over 300M)
        exit -413
    fi
    echo "[${STATUS}] - Done uploading jar file to ${FILE_TARGET_PATH}"
}

# deploy_via_curl - uploads each artifact via curl
function deploy_via_curl {
    TYPE="${1}"
    for PROJ in `find . -type d -maxdepth 3 -name 'target' | sed 's|\.\/||g' | grep -v '\.' `
    do
        MODULE_DIRECTORY=`dirname ${PROJ}`
        MODULE=`basename ${MODULE_DIRECTORY}`
        echo "Processing [${PROJ}] files"
        # Upload Project File
        POM_FILE="${MODULE_DIRECTORY}/pom.xml"
        if [ -f ${POM_FILE} ]
        then
            FILE="${POM_FILE}"
            FILE_TARGET_PATH="/com/ibm/fhir/${MODULE}/${BUILD_VERSION}/${MODULE}-${BUILD_VERSION}.pom"
            upload_to_bintray "${MODULE}" "${FILE}" "${FILE_TARGET_PATH}"
        fi

        # Sources
        for SOURCES_JAR in `find ${PROJ} -iname "*-sources.jar" -maxdepth 1 -exec basename {} \;`
        do
            FILE="${SOURCES_JAR}"
            FILE_TARGET_PATH="/com/ibm/fhir/${MODULE}/${BUILD_VERSION}/${MODULE}-${BUILD_VERSION}-sources.jar"
            upload_to_bintray "${MODULE}" "${FILE}" "${FILE_TARGET_PATH}"
        done 

        # JavaDoc
        for JAVADOC_JAR in `find ${PROJ} -iname "*${BUILD_VERSION}-javadoc.jar" -maxdepth 1 -exec basename {} \;`
        do
            FILE="${JAVADOC_JAR}"
            FILE_TARGET_PATH="/com/ibm/fhir/${MODULE}/${BUILD_VERSION}/${MODULE}-${BUILD_VERSION}-javadoc.jar"
            upload_to_bintray "${MODULE}" "${FILE}" "${FILE_TARGET_PATH}"
        done

        # Tests Jar
        for TESTS_JAR in `find ${PROJ} -iname "*${BUILD_VERSION}-tests.jar" -maxdepth 1 -exec basename {} \;`
        do
            FILE="${TESTS_JAR}"
            FILE_TARGET_PATH="/com/ibm/fhir/${MODULE}/${BUILD_VERSION}/${MODULE}-${BUILD_VERSION}-tests.jar"
            upload_to_bintray "${MODULE}" "${FILE}" "${FILE_TARGET_PATH}"
        done

        # The following files have potentials for MULTIPLE matching files. 
        # Jar
        for JAR in `find ${PROJ} -maxdepth 1 -not -name '*-tests.jar' -and -not -name '*-javadoc.jar' -and -not -name '*-sources.jar' -and -not -name '*orginal*.jar' -and -name '*.jar' -exec basename {} \;`
        do
            FILE="${JAR}"
            FILE_TARGET_PATH="/com/ibm/fhir/${MODULE}/${BUILD_VERSION}/${JAR}"
            upload_to_bintray "${MODULE}" "${FILE}" "${FILE_TARGET_PATH}"
        done

        # Zip Files
        for ZIP in `find ${PROJ} -name fhir-validation-distribution.zip -or -name fhir-cli.zip -maxdepth 3`
        do 
            FILE="${ZIP}"
            FILE_TARGET_PATH="/com/ibm/fhir/${MODULE}/${BUILD_VERSION}/${JAR}"
            upload_to_bintray "${MODULE}" "${FILE}" "${FILE_TARGET_PATH}"
        done
        echo "Finished Upload for [${MODULE}]"
    done
}

###############################################################################
# check to see if mvn exists
if which mvn | grep -i mvn
then
    debugging 'mvn is found!'
else
    warn 'mvn is not found!'
fi

#RELEASE_CANDIDATE or RELEASE or SNAPSHOT or EXISTING
case $BUILD_TYPE in
    RELEASE_CANDIDATE)
        TYPE="snapshots"
        deploy_via_curl "${TYPE}"
        header_line
    ;;
    RELEASE)
        TYPE="releases"
        deploy_via_curl "${TYPE}"
        header_line
    ;;
    SNAPSHOT)
        info "SNAPSHOT build is not set"
        header_line
    ;;
    EXISTING)
        info "EXISTING build is not set"
        header_line
    ;;
    *)
        warn "invalid function called, dropping through "
    ;;
esac

# EOF
