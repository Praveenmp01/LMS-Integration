import { TableItems } from "@bb-platform-extensions/cdk-constructs/src/types/Tables";

export const tableDefinitions: TableItems = [
    {
        id: "lmsIntegrationsTable",
        partitionKey: {
            name: "siteId",
            type: "string"
        },        
        sortKey:{
            name:"id",
            type:"string"
        },
        globalSecondaryIndex: [
        {
            indexName: "applicationIdIndex",
            partitionKey: {
                name: "applicationId",
                type: "string"
            } 
        }]
    }
];