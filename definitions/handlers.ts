import { handlerItems } from "@bb-platform-extensions/cdk-constructs/src/types/Handlers";

export const lambdaItems:handlerItems = [
    {
        id: 'createCourseMembership',
        runtime: "JAVA",
        path: "handlers/target/CreateCourseMembershipHandler-1.0.jar",
        handler: "com.blackboard.platform.extensions.lmsintegrations.handlers.CreateCourseMembershipHandler",
        tableData: [
            { id: "lmsIntegrationTable", permissions: "read/write", envVarName: "LMS_INTEGRATION_TABLE" }
        ],
        environment:{
            LOG_LEVEL: "ERROR"
        }
    },
    {
        id: 'courseMembershipCount',
        runtime: "JAVA",
        path: "handlers/target/CourseMembershipCountHandler-1.0.jar",
        handler: "com.blackboard.platform.extensions.lmsintegrations.handlers.CourseMembershipCountHandler",
        tableData: [
            { id: "lmsIntegrationTable", permissions: "read/write", envVarName: "LMS_INTEGRATION_TABLE" }
        ],
        environment:{
            LOG_LEVEL: "ERROR"
        }
    },
    {
        id: 'createUser',
        runtime: "JAVA",
        path: "handlers/target/CreateUserHandler-1.0.jar",
        handler: "com.blackboard.platform.extensions.lmsintegrations.handlers.CreateUserHandler",
        tableData: [],
        environment:{
            LOG_LEVEL: "ERROR"
        }
    },
    {
        id: 'checkUserExists',
        runtime: "JAVA",
        path: "handlers/target/CheckUserExistsHandler-1.0.jar",
        handler: "com.blackboard.platform.extensions.lmsintegrations.handlers.CheckUserExistsHandler",
        tableData: [],
        environment:{
            LOG_LEVEL: "ERROR"
        }
    }
]