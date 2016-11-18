/**
 * (C) Copyright IBM Corp. 2016,2017,2018,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.client;

import javax.json.JsonObject;
import javax.ws.rs.client.WebTarget;

import com.ibm.watsonhealth.fhir.model.Bundle;
import com.ibm.watsonhealth.fhir.model.Resource;

/**
 * This interface provides a client API for invoking the FHIR Server's REST API.
 */
public interface FHIRClient {
    
    /**
     * FHIR REST API endpoint base URL (e.g. https://localhost:9443/fhir-server/api/v1).
     */
    public static final String PROPNAME_BASE_URL            = "fhirclient.rest.base.url";

    /**
     * Specifies the default mimetype to be used by the FHIRClient instance when invoking
     * FHIR REST APIs.   If not specified a value of "application/json+fhir" will be used.
     */
    public static final String PROPNAME_DEFAULT_MIMETYPE    = "fhirclient.default.mimetype";
    
    /**
     * Indicates whether OAuth 2.0 should be used when invoking REST API requests.
     * Valid values are "true" and "false" (the default).   If enabled, then the authorizeURL, tokenURL and grantType properties
     * are required as well.
     */
    public static final String PROPNAME_OAUTH2_ENABLED    = "fhirclient.oAuth2.enabled";
    
    /**
     * The accessToken to use with OAuth 2.0 Authorization.
     */
    public static final String PROPNAME_OAUTH2_TOKEN      = "fhirclient.oAuth2.accessToken";
    
    /**
     * Indicates whether Basic Authentication should be used when invoking REST API requests.
     * Valid values are "true" and "false" (the default).   If enabled, then the username and password properties
     * are required as well.
     */
    public static final String PROPNAME_BASIC_AUTH_ENABLED    = "fhirclient.basicauth.enabled";
    
    /**
     * The username to use with Basic Authentication.
     */
    public static final String PROPNAME_CLIENT_USERNAME       = "fhirclient.basicauth.username";
    
    /** 
     * The password to use with Basic Authentication.
     */
    public static final String PROPNAME_CLIENT_PASSWORD       = "fhirclient.basicauth.password";

    /**
     * Indicates whether Client Certificate-based Authentication should be used when invoking REST API requests.
     * Valid values are "true" and "false" (the default).  If enabled, then the rest of the "clientauth"-related properties
     * are required as well.
     */
    public static final String PROPNAME_CLIENT_AUTH_ENABLED   = "fhirclient.clientauth.enabled";
    
    /**
     * The client truststore's filename.
     * The client truststore is used to store the server's public key certificates and is used
     * to verify the server's identity.
     */
    public static final String PROPNAME_TRUSTSTORE_LOCATION   = "fhirclient.truststore.location";
    
    /**
     * The client truststore's password.
     */
    public static final String PROPNAME_TRUSTSTORE_PASSWORD   = "fhirclient.truststore.password";
    
    /**
     * The client keystore's filename.
     * The client keystore is used to store the client's private/public key pair certificates.
     * When using client certificate-based authentication, this is now the client supplies its identity 
     * to the server.
     */
    public static final String PROPNAME_KEYSTORE_LOCATION     = "fhirclient.keystore.location";
    
    /**
     * The client keystore's password.
     */
    public static final String PROPNAME_KEYSTORE_PASSWORD     = "fhirclient.keystore.password";
    
    /**
     * The password associated with the client's certificate within the keystore file.
     */
    public static final String PROPNAME_KEYSTORE_KEY_PASSWORD = "fhirclient.keystore.key.password";

    /**
     * Indicates whether or not the REST API request and response payloads should be encrypted.
     * Valid values are "true" and "false" (the default).  If enabled, then the rest of the encryption-related
     * properties are required as well.
     */
    public static final String PROPNAME_ENCRYPTION_ENABLED    = "fhirclient.encryption.enabled";
    
    /**
     * The name of the JCEKS-type keystore that contains the client's AES 256-bit encryption key.
     */
    public static final String PROPNAME_ENCRYPTION_KSLOC      = "fhirclient.encryption.keystore.location";
    
    /**
     * The password associated with the keystore that contains the encryption key.
     */
    public static final String PROPNAME_ENCRYPTION_KSPW       = "fhirclient.encryption.keystore.password";
    
    /**
     * The password associated with the encryption key within the keystore.
     */
    public static final String PROPNAME_ENCRYPTION_KEYPW      = "fhirclient.encryption.key.password";
    
    /**
     * The name of the alias under which the encryption key is stored within the encryption key keystore file.
     */
    public static final String ENCRYPTION_KEY_ALIAS = "fhirEncryptionKey";
    
    
    /**
     * Returns a JAX-RS 2.0 WebTarget object associated with the REST API endpoint.
     * @return a WebTarget instance that can be used to invoke REST APIs.
     * @throws Exception
     */
    WebTarget getWebTarget() throws Exception;
    
    /**
     * Returns a JAX-RS 2.0 WebTarget object associated with a given REST API endpoint.
     * @return a WebTarget instance that can be used to invoke REST APIs.
     * @throws Exception
     */
    WebTarget getWebTarget(String baseURL) throws Exception;
    
    /**
     * Returns a JAX-RS 2.0 WebTarget object associated with a given REST API endpoint.
     * @return a WebTarget instance that can be used to invoke REST APIs.
     * @throws Exception
     */
    WebTarget getWebTargetUsingBasicAuth(String baseURL, String username, String pwd) throws Exception;
    
    /**
     * Sets the default mime-type to be used by the FHIRClient interface when invoking REST API operations.
     * @param mimeType a string containing the mime-type (e.g. "application/json+fhir")
     * @throws Exception
     */
    void setDefaultMimeType(String mimeType) throws Exception;
    
    /**
     * Returns a string that represents the default mime-type associated with the FHIRClient interface.
     * @throws Exception
     */
    String getDefaultMimeType() throws Exception;
    
    /**
     * Sets the OAuth 2.0 access token to be used by the FHIRClient interface for authorization when invoking REST API operations.
     * @param accessToken a string containing the OAuth 2.0 access token
     * @throws Exception
     */
    void setOAuth2AccessToken(String accessToken) throws Exception;
    
    /**
     * Returns a string that represents the OAuth 2.0 access token to be used by the FHIRClient interface for authorization requests.
     * @throws Exception
     */
    String getOAuth2AccessToken() throws Exception;
    
    /**
     * Invokes the 'metadata' FHIR REST API operation.
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains a Conformance object which describes the 
     * FHIR Server's capabilities
     * @throws Exception
     */
    FHIRResponse metadata(FHIRRequestHeader... headers) throws Exception;
    
    /**
     * Invokes the 'create' FHIR REST API operation.
     * @param resource the FHIR resource to be created
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'create' operation
     * @throws Exception
     */
    FHIRResponse create(Resource resource, FHIRRequestHeader... headers) throws Exception;
    
    /**
     * Invokes the 'create' FHIR REST API operation.
     * @param resource the resource (in the form of a JsonObject) to be created
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'create' operation
     * @throws Exception
     */
    FHIRResponse create(JsonObject resource, FHIRRequestHeader... headers) throws Exception;
    
    /**
     * Invokes the 'update' FHIR REST API operation.
     * @param resource the FHIR resource to be updated
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'update' operation
     * @throws Exception
     */
    FHIRResponse update(Resource resource, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'update' FHIR REST API operation.
     * @param resource the resource (in the form of a JsonObject) to be updated
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'update' operation
     * @throws Exception
     */
    FHIRResponse update(JsonObject resource, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'read' FHIR REST API operation.
     * @param resourceType a string representing the name of the resource type 
     * to be retrieved (e.g. "Patient")
     * @param resourceId the id of the resource to be retrieved
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'read' operation
     * @throws Exception
     */
    FHIRResponse read(String resourceType, String resourceId, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'vread' FHIR REST API operation.
     * @param resourceType a string representing the name of the resource type 
     * to be retrieved (e.g. "Patient")
     * @param resourceId the id of the resource to be retrieved
     * @param versionId the version id of the resource to be retrieved
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'read' operation
     * @throws Exception
     */
    
    FHIRResponse vread(String resourceType, String resourceId, String versionId, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'history' FHIR REST API operation.
     * @param resourceType a string representing the name of the resource type 
     * to be retrieved (e.g. "Patient")
     * @param resourceId the id of the resource to be retrieved
     * @param parameters an optional collection of request parameters for the 'history' operation; 
     * may be specified as null if no parameters need to be passed to the 'history' operation
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'history' operation
     * @throws Exception
     */
    FHIRResponse history(String resourceType, String resourceId, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;
    
    /**
     * Invokes the 'search' FHIR REST API operation.
     * @param resourceType a string representing the name of the resource type to search for (e.g. "Patient")
     * @param parameters  an optional collection of request parameters for the 'search' operation;
     * may be specified as null if no parameters need to be passed to the 'search' operation
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'search' operation
     * @throws Exception
     */
    FHIRResponse search(String resourceType, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;
    
    /**
     * Invokes the '_search' FHIR REST API operation.
     * @param resourceType a string representing the name of the resource type to search for (e.g. "Patient")
     * @param parameters  an optional collection of request parameters for the '_search' operation;
     * may be specified as null if no parameters need to be passed to the '_search' operation;
     * search parameters for this operation will go in the request body
     * @return a FHIRResponse that contains the results of the '_search' operation
     * @throws Exception
     */
    FHIRResponse _search(String resourceType, FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;
    
    /**
     * Invokes the 'search-all' FHIR REST API operation.
     * @param parameters  an optional collection of request parameters for the 'search-all' operation;
     * may be specified as null if no parameters need to be passed to the 'search' operation;
     * search parameters for this operation will go in the request body
     * @return a FHIRResponse that contains the results of the 'search-all' operation
     * @throws Exception
     */
    FHIRResponse searchAll(FHIRParameters parameters, FHIRRequestHeader... headers) throws Exception;
    
    /**
     * Invokes the 'validate' FHIR REST API operation.
     * @param resource the resource to be validated
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'validate' operation
     * @throws Exception
     */
    FHIRResponse validate(Resource resource, FHIRRequestHeader... headers) throws Exception;

    /**
     * Invokes the 'validate' FHIR REST API operation.
     * @param resource the resource (in the form of a JsonObject) to be validated 
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'validate' operation
     * @throws Exception
     */
    FHIRResponse validate(JsonObject resource, FHIRRequestHeader... headers) throws Exception;
    
    /**
     * Invokes the 'batch/transaction' FHIR REST API operation for a request bundle of type 'batch'.
     * @param bundle the Bundle containing the individual requests
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'batch/transaction' operation
     * @throws Exception
     */
    FHIRResponse batch(Bundle bundle, FHIRRequestHeader... headers) throws Exception;
    
    /**
     * Invokes the 'batch/transaction' FHIR REST API operation for a request bundle of type 'transaction'.
     * @param bundle the Bundle containing the individual requests
     * @param headers an optional list of request headers to be added to the request
     * @return a FHIRResponse that contains the results of the 'batch/transaction' operation
     * @throws Exception
     */
    FHIRResponse transaction(Bundle bundle, FHIRRequestHeader... headers) throws Exception;
}
