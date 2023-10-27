import { Endpoint } from "@bb-platform-extensions/cdk-constructs/src/types/Endpoints";

export const isApi: boolean = true;
export const apiVersion: string = 'v2';
export const routes: Endpoint[] = [
    { 
        id: 'createCourseMembership', 
        path: 'sites/{siteId}/applications/{applicationId}/courseMemberships', 
        method: 'PUT', 
        lambdaId: 'createCourseMembership' 
    },
    { 
        id: 'courseMembershipCount', 
        path: 'sites/{siteId}/applications/{applicationId}/courses/{courseId}/membershipCount', 
        method: 'GET', 
        lambdaId: 'courseMembershipCount',
        parent: 'createCourseMembership-{applicationId}'
    },
    { 
        id: 'createUser', 
        path: 'sites/{siteId}/applications/{applicationId}/lmsType/{lmsType}/users', 
        method: 'POST', 
        lambdaId: 'createUser',
        parent: 'createCourseMembership-{applicationId}'
    },
    { 
        id: 'checkUserExists', 
        path: 'sites/{siteId}/applications/{applicationId}/lmsType/{lmsType}/users/{userName}', 
        method: 'HEAD', 
        lambdaId: 'checkUserExists',
        parent: 'createUser-users'
    }
];