/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.bucket.persistence;

import static com.ibm.fhir.bucket.persistence.SchemaConstants.*;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.ibm.fhir.database.utils.api.IDatabaseAdapter;
import com.ibm.fhir.database.utils.model.Generated;
import com.ibm.fhir.database.utils.model.PhysicalDataModel;
import com.ibm.fhir.database.utils.model.Sequence;
import com.ibm.fhir.database.utils.model.Table;
import com.ibm.fhir.model.type.code.FHIRResourceType;

/**
 * Defines and manages the little schema used to coordinate multiple
 * servers and maintain a list of resource logical ids generated by
 * the FHIR server.
 */
public class FhirBucketSchema {
    private final String schemaName;

    /**
     * Public constructor
     * @param schemaName
     */
    public FhirBucketSchema(String schemaName) {
        this.schemaName = schemaName;
    }
    
    /**
     * Populate the resource types table with all the resource type names
     * defined in the FHIR R4 model
     */
    private void fillResourceTypes(IDatabaseAdapter adapter) {
        Set<String> resourceTypes = Arrays.stream(FHIRResourceType.ValueSet.values())
                .map(FHIRResourceType.ValueSet::value)
                .collect(Collectors.toSet());

    }

    /**
     * Create the model
     * @param pdm
     */
    public void constructModel(PhysicalDataModel pdm) {
        
        addSequences(pdm);
        
        // each time this program runs it registers an entry in
        // the loader_instances table
        addLoaderInstances(pdm);
        
        // the bundle files discovered during the bucket scan
        addBucketPaths(pdm);
        addResourceBundles(pdm);

        // for recording the ids generated for each resource by the FHIR server
        // as we load the bundles discovered during the scan phase
        Table resourceTypes = addResourceTypes(pdm);        
        addLogicalResources(pdm, resourceTypes);
    }
    
    protected void addSequences(PhysicalDataModel pdm) {
        Sequence jobAllocationSeq = new Sequence(schemaName, JOB_ALLOCATION_SEQ, 1, 0, 1000);
        pdm.addObject(jobAllocationSeq);
    }

    /**
     * Add the definition of the BUCKET_PATHS table to the model
     * @param pdm
     * @return
     */
    protected Table addLoaderInstances(PhysicalDataModel pdm) {

        Table bucketPaths = Table.builder(schemaName, LOADER_INSTANCES)
                .addBigIntColumn(   LOADER_INSTANCE_ID,              false)
                .setIdentityColumn( LOADER_INSTANCE_ID, Generated.ALWAYS)
                .addVarcharColumn( LOADER_INSTANCE_KEY,          36, false)
                .addVarcharColumn(            HOSTNAME,          64, false)
                .addIntColumn(                     PID,              false)
                .addTimestampColumn(  HEARTBEAT_TSTAMP,              false)
                .addUniqueIndex(UNQ + "_loader_instances_key", LOADER_INSTANCE_KEY)
                .addPrimaryKey(LOADER_INSTANCES + "_PK", LOADER_INSTANCE_ID)
                .build(pdm);
        
        pdm.addTable(bucketPaths);
        pdm.addObject(bucketPaths);
        
        return bucketPaths;
    }

    /**
     * Add the definition of the BUCKET_PATHS table to the model
     * @param pdm
     * @return
     */
    protected Table addBucketPaths(PhysicalDataModel pdm) {
        
        // The FK BUCKET_PATH_ID starts as null, but is set when the
        // scanner thread of a loader claims the bucket/path for scanning,
        // and is set to NULL again after the scan is complete
        Table bucketPaths = Table.builder(schemaName, BUCKET_PATHS)
                .addBigIntColumn(       BUCKET_PATH_ID,              false)
                .setIdentityColumn(BUCKET_PATH_ID, Generated.ALWAYS)
                .addVarcharColumn(         BUCKET_NAME,          64, false)
                .addVarcharColumn(         BUCKET_PATH,         256, false)
                .addUniqueIndex(UNQ + "_bucket_paths_nmpth", BUCKET_NAME, BUCKET_PATH)
                .addPrimaryKey(BUCKET_PATHS + "_PK", BUCKET_PATH_ID)
                .build(pdm);
        
        pdm.addTable(bucketPaths);
        pdm.addObject(bucketPaths);
        
        return bucketPaths;
    }
   
    /**
     * Add the definition of the RESOURCE_BUNDLES table to the model
     * @param pdm
     * @return
     */
    protected Table addResourceBundles(PhysicalDataModel pdm) {
        // Note that the object_name is relative to the bundle path associated
        // with each record
        Table resourceBundles = Table.builder(schemaName, RESOURCE_BUNDLES)
                .addBigIntColumn(  RESOURCE_BUNDLE_ID,             false)
                .setIdentityColumn(RESOURCE_BUNDLE_ID,  Generated.ALWAYS)
                .addBigIntColumn(      BUCKET_PATH_ID,             false)
                .addVarcharColumn(        OBJECT_NAME,         64, false)
                .addBigIntColumn(         OBJECT_SIZE,             false)
                .addVarcharColumn(          FILE_TYPE,         12, false)
                .addBigIntColumn(       ALLOCATION_ID,              true)
                .addBigIntColumn(  LOADER_INSTANCE_ID,              true)
                .addTimestampColumn(     LOAD_STARTED,              true)
                .addTimestampColumn(   LOAD_COMPLETED,              true)
                .addUniqueIndex(UNQ + "_resource_bundle_bktnm", BUCKET_PATH_ID, OBJECT_NAME)
                .addIndex(IDX + "_resource_bundle_allocid", ALLOCATION_ID)
                .addPrimaryKey(RESOURCE_BUNDLES + "_PK", RESOURCE_BUNDLE_ID)
                .addForeignKeyConstraint(FK + "_" + RESOURCE_BUNDLES + "_BKT", schemaName, BUCKET_PATHS, BUCKET_PATH_ID)
                .build(pdm);
        
        pdm.addTable(resourceBundles);
        pdm.addObject(resourceBundles);
        
        return resourceBundles;
    }
    
    /**
     * Add the definition of the RESOURCE_TYPES table to the model
     * @param pdm
     * @return
     */
    protected Table addResourceTypes(PhysicalDataModel pdm) {
        Table resourceTypesTable = Table.builder(schemaName, RESOURCE_TYPES)
                .addIntColumn(     RESOURCE_TYPE_ID,            false)
                .setIdentityColumn(RESOURCE_TYPE_ID, Generated.ALWAYS)
                .addVarcharColumn(    RESOURCE_TYPE,        64, false)
                .addUniqueIndex(UNQ + "_resource_types_rt", RESOURCE_TYPE)
                .addPrimaryKey(RESOURCE_TYPES + "_PK", RESOURCE_TYPE_ID)
                .build(pdm);
        
        pdm.addTable(resourceTypesTable);
        pdm.addObject(resourceTypesTable);
        
        return resourceTypesTable;
    }
    
    protected void addLogicalResources(PhysicalDataModel pdm, Table resourceTypes) {
        final String tableName = LOGICAL_RESOURCES;

        // note that the same bundle can be loaded multiple times,
        // and also that each bundle may contain several resources
        Table tbl = Table.builder(schemaName, tableName)
                .addBigIntColumn(LOGICAL_RESOURCE_ID, false)
                .setIdentityColumn(LOGICAL_RESOURCE_ID, Generated.ALWAYS)
                .addIntColumn(RESOURCE_TYPE_ID, false)
                .addVarcharColumn(LOGICAL_ID, LOGICAL_ID_BYTES, false)
                .addBigIntColumn(RESOURCE_BUNDLE_ID, false)
                .addPrimaryKey(tableName + "_PK", LOGICAL_RESOURCE_ID)
                .addUniqueIndex("UNQ_" + LOGICAL_RESOURCES, RESOURCE_TYPE_ID, LOGICAL_ID)
                .addForeignKeyConstraint(FK + tableName + "_RTID", schemaName, RESOURCE_TYPES, RESOURCE_TYPE_ID)
                .addForeignKeyConstraint(FK + tableName + "_RBID", schemaName, RESOURCE_BUNDLES, RESOURCE_BUNDLE_ID)
                .build(pdm);
        
        pdm.addTable(tbl);
        pdm.addObject(tbl);
    }
    
    /**
     * Apply the model to the database. Will generate the DDL and execute it
     * @param pdm
     */
    protected void applyModel(IDatabaseAdapter adapter, PhysicalDataModel pdm) {
        pdm.apply(adapter);
    }
    
}