package com.blackboard.platform.extensions.lmsintegrations.layer.services;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.blackboard.platform.extensions.restapi.client.registrar.SiteService;
import com.blackboard.platform.extensions.restapi.exceptions.ResourceBadRequestException;
import com.blackboard.platform.extensions.restapi.exceptions.RestAPILTIInvalidException;
import com.blackboard.platform.extensions.restapi.handler.RequestEventHandler;
import com.blackboard.platform.extensions.restapi.ssm.ParamStoreClient;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LmsIntegrationsHandlerService.class, AmazonDynamoDBClientBuilder.class, System.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*", "org.w3c.dom.*" })
public class LmsIntegrationsHandlerServiceTest {
    private RequestEventHandler requestEventHandler = mock(RequestEventHandler.class);
    private ParamStoreClient paramStoreClient = mock(ParamStoreClient.class);
    private APIGatewayProxyRequestEvent input = mock(APIGatewayProxyRequestEvent.class);
    private SiteService siteService = mock(SiteService.class);
    private Context context = mock(Context.class);
    private AmazonDynamoDBClientBuilder externalDynamoBuilder = mock(AmazonDynamoDBClientBuilder.class);
    private AmazonDynamoDBClientBuilder builderToBuild = mock(AmazonDynamoDBClientBuilder.class);
    private DynamoDB dynamoDB = mock(DynamoDB.class);
    private DynamoDBMapper dynamoDBMapper = mock(DynamoDBMapper.class);

    @Before
    public void setup() {
        try {
            PowerMockito.whenNew(DynamoDB.class).withArguments(AmazonDynamoDB.class).thenReturn(dynamoDB);
            PowerMockito.whenNew(DynamoDBMapper.class).withArguments(any()).thenReturn(dynamoDBMapper);
            PowerMockito.whenNew(RequestEventHandler.class).withArguments(any()).thenReturn(requestEventHandler);
            PowerMockito.whenNew(ParamStoreClient.class).withNoArguments().thenReturn(paramStoreClient);
            PowerMockito.whenNew(SiteService.class).withArguments(anyString(), anyString(), anyString())
                    .thenReturn(siteService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldSuccessHandleEvent() throws JsonProcessingException, RestAPILTIInvalidException, ResourceBadRequestException {
        PowerMockito.mockStatic(AmazonDynamoDBClientBuilder.class);
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv("AWS_REGION")).thenAnswer((Answer<String>) invocation -> "us-east-1");
        PowerMockito.when(builderToBuild.withRegion(anyString()))
                .thenAnswer((Answer<AmazonDynamoDBClientBuilder>) invocation -> externalDynamoBuilder);
        PowerMockito.when(AmazonDynamoDBClientBuilder.standard())
                .thenAnswer((Answer<AmazonDynamoDBClientBuilder>) invocation -> builderToBuild);
        LmsIntegrationsHandlerService lmsIntegrationsService = new LmsIntegrationsHandlerService(input, context);
        assertNotNull(lmsIntegrationsService);
    }
}
