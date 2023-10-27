package com.blackboard.platform.extensions.lmsintegrations.handlers;

import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.DEV_PORTAL_KEY;
import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.DEV_PORTAL_SECRET;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.blackboard.platform.extensions.lmsintegrations.LmsIntegrationsFixture;
import com.blackboard.platform.extensions.lmsintegrations.layer.exceptions.BadGatewayException;
import com.blackboard.platform.extensions.lmsintegrations.layer.models.LmsIntegration;
import com.blackboard.platform.extensions.lmsintegrations.layer.services.LmsIntegrationsHandlerService;
import com.blackboard.platform.extensions.lmsintegrations.layer.services.LmsIntegrationsService;
import com.blackboard.platform.extensions.restapi.handler.RequestEventHandler;
import com.blackboard.platform.extensions.restapi.ssm.ParamStoreClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CourseMembershipCountHandler.class, LmsIntegrationsHandlerService.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*", "org.w3c.dom.*" })
public class CourseMembershipCountHandlerTest {

    private RequestEventHandler requestEventHandler = mock(RequestEventHandler.class);
    private LmsIntegrationsHandlerService lmsIntegrationsHandlerServiceMock = mock(LmsIntegrationsHandlerService.class);
    private LmsIntegrationsService lmsIntegrationsServiceMock = mock(LmsIntegrationsService.class);
    
    private CourseMembershipCountHandler courseMembershipHandlerCount = new CourseMembershipCountHandler();

    private ObjectMapper mockMapper = mock(ObjectMapper.class);
    private ObjectMapper realOM = new ObjectMapper();
    private LmsIntegrationsFixture fixture = new LmsIntegrationsFixture();
    String path = "/api/v2/lmsIntegrations/sites/site123/applications/programView";

    private ParamStoreClient paramStoreClient = mock(ParamStoreClient.class);

    @Before
    public void setup() {
        try {

            PowerMockito.whenNew(LmsIntegrationsHandlerService.class)
                    .withArguments(any(APIGatewayProxyRequestEvent.class), any(Context.class))
                    .thenReturn(lmsIntegrationsHandlerServiceMock);
            PowerMockito.whenNew(ParamStoreClient.class).withNoArguments().thenReturn(paramStoreClient);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldSuccessHandleEvent() throws Exception {
        APIGatewayProxyRequestEvent input = mock(APIGatewayProxyRequestEvent.class);
        Context context = mock(Context.class);
        
        PowerMockito.whenNew(RequestEventHandler.class).withAnyArguments().thenReturn(requestEventHandler);

        Map<String, Object> courseMembershipCountResponse = fixture.getCourseMembershipCount();
        String responseBody = realOM.writeValueAsString(courseMembershipCountResponse);

        PowerMockito.whenNew(ObjectMapper.class).withNoArguments().thenReturn(mockMapper);
        PowerMockito.when(mockMapper.configure(any(DeserializationFeature.class), anyBoolean()))
                .thenAnswer((Answer<ObjectMapper>) invocation -> mockMapper);
        PowerMockito.when(mockMapper.writeValueAsString(any(Map.class)))
                .thenAnswer((Answer<String>) invocation -> responseBody);
        PowerMockito.when(lmsIntegrationsHandlerServiceMock.getRequestEventHandler())
                .thenAnswer((Answer<RequestEventHandler>) invocation -> requestEventHandler);
        PowerMockito.when(requestEventHandler.getPathParam(anyString()))
                .thenAnswer((Answer<String>) invocation -> "site123")
                .thenAnswer((Answer<String>) invocation -> "paymentGateway");
        PowerMockito.when(lmsIntegrationsHandlerServiceMock.getService())
                .thenAnswer((Answer<LmsIntegrationsService>) invocation -> lmsIntegrationsServiceMock);
        
        PowerMockito.when(lmsIntegrationsHandlerServiceMock.getParameterMap()).thenReturn( getParam() );
        
        LmsIntegration lmsIntegration = setLmsIntegration( "site1", "programView", "test_course1", "key", "secret", "programView", null );
        
        PowerMockito
                .when(lmsIntegrationsHandlerServiceMock.getService().courseMembershipCount( lmsIntegration ))
                .thenAnswer((Answer<Map>) invocation -> courseMembershipCountResponse);

        PowerMockito.when(mockMapper.writeValueAsString(any())).thenAnswer((Answer<String>) invocation -> responseBody);

        ProxyRequestContext proxyRequestContext = mock(ProxyRequestContext.class);
        PowerMockito.when(input.getRequestContext())
        
                .thenAnswer((Answer<ProxyRequestContext>) invocation -> proxyRequestContext);
        
        PowerMockito.when(proxyRequestContext.getPath())
        .thenAnswer((Answer<String>) invocation -> path);
        
        APIGatewayProxyResponseEvent response = courseMembershipHandlerCount.handleRequest(input, context);

        JSONObject responseJSON = new JSONObject(response.getBody());
        assertNotNull(responseJSON);
        assertEquals(Integer.valueOf("200"), response.getStatusCode());
    }


    @Test
    public void shouldFailureHandleEvent() throws Exception {
        APIGatewayProxyRequestEvent input = mock(APIGatewayProxyRequestEvent.class);
        Context context = mock(Context.class);
        
        PowerMockito.whenNew(RequestEventHandler.class).withAnyArguments().thenReturn(requestEventHandler);

        Map<String, Object> courseMembershipCountResponse = fixture.getCourseMembershipCount();
        String responseBody = realOM.writeValueAsString(courseMembershipCountResponse);

        PowerMockito.whenNew(ObjectMapper.class).withNoArguments().thenReturn(mockMapper);
        PowerMockito.when(mockMapper.configure(any(DeserializationFeature.class), anyBoolean()))
                .thenAnswer((Answer<ObjectMapper>) invocation -> mockMapper);
        PowerMockito.when(mockMapper.writeValueAsString(any(Map.class)))
                .thenAnswer((Answer<String>) invocation -> responseBody);
        PowerMockito.when(lmsIntegrationsHandlerServiceMock.getRequestEventHandler())
                .thenAnswer((Answer<RequestEventHandler>) invocation -> requestEventHandler);
        PowerMockito.when(requestEventHandler.getPathParam(anyString()))
                .thenAnswer((Answer<String>) invocation -> "site123")
                .thenAnswer((Answer<String>) invocation -> "paymentGateway");
        PowerMockito.when(lmsIntegrationsHandlerServiceMock.getService())
                .thenAnswer((Answer<LmsIntegrationsService>) invocation -> lmsIntegrationsServiceMock);
        PowerMockito.when(lmsIntegrationsHandlerServiceMock.getParameterMap()).thenReturn( getParam() );
        
        doThrow(new BadGatewayException("", null, "", "")).when(lmsIntegrationsServiceMock).courseMembershipCount( any() );
        
        
        PowerMockito.when(mockMapper.writeValueAsString(any())).thenAnswer((Answer<String>) invocation -> responseBody);

        ProxyRequestContext proxyRequestContext = mock(ProxyRequestContext.class);
        PowerMockito.when(input.getRequestContext())
        
                .thenAnswer((Answer<ProxyRequestContext>) invocation -> proxyRequestContext);
        
        PowerMockito.when(proxyRequestContext.getPath())
        .thenAnswer((Answer<String>) invocation -> path);
        
        assertThrows( Exception.class, () -> courseMembershipHandlerCount.handleRequest(input, context) );

    }
    
    private LmsIntegration setLmsIntegration( String siteId, String applicationId, String courseId, String devPortalKey, String devPortalSecret, String lmsAllowedApplications, Map<String,String> queryParam )
    {
      LmsIntegration lmsIntegration = new LmsIntegration();
      
      lmsIntegration.setApplicationId( applicationId );
      lmsIntegration.setDevPortalKey( devPortalKey );
      lmsIntegration.setDevPortalSecret( devPortalSecret );
      lmsIntegration.setLmsAllowedApplications( lmsAllowedApplications );
      lmsIntegration.setSiteId( siteId );
      lmsIntegration.setQueryParam(queryParam);
      lmsIntegration.setCourseId( courseId );
      
      return lmsIntegration;
      
    }
    
    private Map<String,String> getParam()
    {
      Map<String, String> paramMap = new HashMap<>();
      paramMap.put(DEV_PORTAL_KEY, "key");
      paramMap.put(DEV_PORTAL_SECRET, "secret");
      return paramMap;
    }

}
