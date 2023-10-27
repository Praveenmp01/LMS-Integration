package com.blackboard.platform.extensions.lmsintegrations.handlers;

import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.DEV_PORTAL_KEY;
import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.DEV_PORTAL_SECRET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.blackboard.platform.extensions.lmsintegrations.LmsIntegrationsFixture;
import com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants;
import com.blackboard.platform.extensions.lmsintegrations.layer.services.LmsIntegrationsHandlerService;
import com.blackboard.platform.extensions.lmsintegrations.layer.services.LmsIntegrationsService;
import com.blackboard.platform.extensions.restapi.handler.RequestEventHandler;
import com.blackboard.platform.extensions.restapi.ssm.ParamStoreClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { CheckUserExistsHandler.class, LmsIntegrationsHandlerService.class } )
@PowerMockIgnore( { "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*", "org.w3c.dom.*" } )
public class CheckUserExistsHandlerTest
{

  private RequestEventHandler requestEventHandler = mock( RequestEventHandler.class );
  private LmsIntegrationsHandlerService lmsIntegrationsHandlerServiceMock = mock( LmsIntegrationsHandlerService.class );
  private LmsIntegrationsService lmsIntegrationsServiceMock = mock( LmsIntegrationsService.class );

  private CheckUserExistsHandler checkUserExistHandler = new CheckUserExistsHandler();

  private ObjectMapper mockMapper = mock( ObjectMapper.class );
  String path = "/api/v2/lmsIntegrations/sites/site123/applications/lmsType/learn/users/user1";

  private ParamStoreClient paramStoreClient = mock( ParamStoreClient.class );

  @Before
  public void setup()
  {
    try
    {

      PowerMockito.whenNew( LmsIntegrationsHandlerService.class )
          .withArguments( any( APIGatewayProxyRequestEvent.class ), any( Context.class ) )
          .thenReturn( lmsIntegrationsHandlerServiceMock );
      PowerMockito.whenNew( ParamStoreClient.class ).withNoArguments().thenReturn( paramStoreClient );

    }
    catch ( Exception e )
    {
      e.printStackTrace();
    }
  }

  @SuppressWarnings( "rawtypes" )
  @Test
  public void shouldSuccessHandleEvent() throws Exception
  {
    APIGatewayProxyRequestEvent input = mock( APIGatewayProxyRequestEvent.class );
    Context context = mock( Context.class );

    PowerMockito.whenNew( RequestEventHandler.class ).withAnyArguments().thenReturn( requestEventHandler );

    PowerMockito.whenNew( ObjectMapper.class ).withNoArguments().thenReturn( mockMapper );
    PowerMockito.when( mockMapper.configure( any( DeserializationFeature.class ), anyBoolean() ) )
        .thenAnswer( (Answer<ObjectMapper>) invocation -> mockMapper );
    PowerMockito.when( lmsIntegrationsHandlerServiceMock.getRequestEventHandler() )
        .thenAnswer( (Answer<RequestEventHandler>) invocation -> requestEventHandler );
    PowerMockito.when( requestEventHandler.getPathParam( anyString() ) )
        .thenAnswer( (Answer<String>) invocation -> "site123" )
        .thenAnswer( (Answer<String>) invocation -> "userWorkflowManager" );
    PowerMockito.when( lmsIntegrationsHandlerServiceMock.getService() )
        .thenAnswer( (Answer<LmsIntegrationsService>) invocation -> lmsIntegrationsServiceMock );

    PowerMockito.when( lmsIntegrationsHandlerServiceMock.getParameterMap() ).thenReturn( getParam() );

    Map<String, String> paramMap = new HashMap<>();
    paramMap.put( Constants.DEV_PORTAL_KEY, "key" );
    paramMap.put( Constants.DEV_PORTAL_SECRET, "secret" );
    paramMap.put( Constants.CREATE_USER_ALLOWED_APPLICATIONS, "userWorkflowManager" );

    

    PowerMockito
        .when( lmsIntegrationsHandlerServiceMock.getService().checkUserExists( anyString(), anyString(), any(),
                                                                               anyString(), anyString() ) )
        .thenAnswer( (Answer<Integer>) invocation -> 200 );

    ProxyRequestContext proxyRequestContext = mock( ProxyRequestContext.class );
    PowerMockito.when( input.getRequestContext() )

        .thenAnswer( (Answer<ProxyRequestContext>) invocation -> proxyRequestContext );

    PowerMockito.when( proxyRequestContext.getPath() ).thenAnswer( (Answer<String>) invocation -> path );

    APIGatewayProxyResponseEvent response = checkUserExistHandler.handleRequest( input, context );

    assertEquals( Integer.valueOf( "200" ), response.getStatusCode() );
  }

  private Map<String, String> getParam()
  {
    Map<String, String> paramMap = new HashMap<>();
    paramMap.put( DEV_PORTAL_KEY, "key" );
    paramMap.put( DEV_PORTAL_SECRET, "secret" );
    return paramMap;
  }

}
