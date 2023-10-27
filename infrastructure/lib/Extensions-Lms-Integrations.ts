import { pipeline_forge } from '@bb-fnds/cdk-constructs';
import { Endpoints, Handlers, Monitoring, Events, Tables, Integration } from '@bb-platform-extensions/cdk-constructs';
import { lambdaItems } from '../../definitions/handlers';
import { apiVersion, isApi, routes } from '../../definitions/routes';
import { tableDefinitions } from '../../definitions/tables';
import { eventSchemaDefinitions, eventsHubSubsDefinitions, rulesDefinitions } from '../../definitions/events';

export class ExtLmsIntegrations {
  constructor(stack: pipeline_forge.Stack) {

    const TableConstruct = new Tables.Collection(stack, 'TablesCreation', {
      tables: tableDefinitions
    });

    // Creates lambdaFunction proxy to send events
    const HandlerCreation = new Handlers.Collection(stack, 'HandlerCreation', {
      handlers: lambdaItems,
      dynamoCollection: TableConstruct.tableCollection
    });

    // Creates endpoint to publish lambdaFunctions
    const endpointsResults = new Endpoints.Collection(stack, 'EndpointLmsIntegrations',
      {
        endpoints: routes,
        handlersCollection: HandlerCreation.handlerCollection,
        resourceRoot: 'lmsIntegrations',
        isApi: isApi,
        apiVersion: apiVersion
      }
    );

    //Create Event schemas, rules and suscriptions.
    const EventsCreation = new Events.Collection(stack, 'EventsCreation-LmsIntegrations',{
      eventSchemas: eventSchemaDefinitions,
      lambdaCollection: HandlerCreation.handlerCollection,
      rules: rulesDefinitions,
      subscriptions: eventsHubSubsDefinitions
    });

    // Implements integration testing automation - any error and will reject the deployment.
    new Integration.Collection(stack, 'integration-testing', {
        configurationFileLocation: './qaAutomation/config.json',
        collectionsDirLocation: './qaAutomation/collections',
        targetStage: 'test'
      });

     //Creates a Monitoring Dashboard
     const monitoringDashboard = new Monitoring.FndsDashboard(stack, 'DashBoard-LmsIntegrations');


  }
}
