package com.blackboard.platform.extensions.lmsintegrations.layer.services;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.blackboard.platform.extensions.lmsintegrations.layer.exceptions.BadGatewayException;
import com.blackboard.platform.extensions.lmsintegrations.layer.fixtures.LmsIntegrationsFixture;
import com.blackboard.platform.extensions.lmsintegrations.layer.models.*;
import com.blackboard.platform.extensions.lmsintegrations.layer.util.GenericUtil;
import com.blackboard.platform.extensions.lmsintegrations.layer.util.LearnRestClient;
import com.blackboard.platform.extensions.restapi.client.RestAPIClient;
import com.blackboard.platform.extensions.restapi.client.registrar.SiteService;
import com.blackboard.platform.extensions.restapi.model.profiles.association.Association;
import com.blackboard.platform.extensions.restapi.model.registrar.Site;
import com.blackboard.platform.extensions.restapi.model.registrar.SiteView;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { GenericUtil.class, LearnRestClient.class } )
@PowerMockIgnore( { "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*", "org.w3c.dom.*",
                    "javax.net.ssl.*", "jdk.internal.net.*", "java.net.http.HttpResponse.*" } )
public class LmsIntegrationsServiceTest
{

  private SiteService siteService;
  private RestAPIClient restAPIClient;

  private LmsIntegrationsFixture fixture = new LmsIntegrationsFixture();
  private LmsIntegrationsService service;
  private ObjectMapper mapper = new ObjectMapper();
  private MockedStatic<GenericUtil> genericUtilMock;
  private MockedStatic<LearnRestClient> learnRestClient;

  @Before
  public void setUp() throws Exception
  {
    mapper = new ObjectMapper();
    restAPIClient = Mockito.mock( RestAPIClient.class );
    siteService = Mockito.mock( SiteService.class );
    genericUtilMock = mockStatic( GenericUtil.class );
    learnRestClient = mockStatic( LearnRestClient.class );
    service = new LmsIntegrationsService( siteService, restAPIClient );
  }

  @Test
  public void createCourseMembership_resourceBadRequestException() throws Exception
  {
    CreateCourseMemebershipPayload courseMembershipPayloadLearn = fixture.getCourseMembershipPayload();
    String payload = mapper.writeValueAsString( courseMembershipPayloadLearn );
    Site mockSite = mock( Site.class );
    SiteView mockSiteView = mock( SiteView.class );
    when( siteService.getCompleteSite( anyString() ) ).thenReturn( mockSite );
    when( mockSite.getSiteView() ).thenReturn( mockSiteView );
    when( mockSiteView.getHostUrl() ).thenReturn( "https://playground.blackboard.com" );
    Association association = new Association();
    association.setAvailable( true );
    association.setEnabled( true );
    when( restAPIClient.getEntityById( anyString(), eq( Association.class ), any() ) ).thenReturn( association );
    assertThrows( BadGatewayException.class, () -> service
        .createCourseMembership( payload, "site123", "paymentGateway", "key", "secret", "", "paymentGateway" ) );
  }

  @Test
  public void createCourseMembership() throws Exception
  {
    Map<String, Object> membershipResponse = fixture.postCourseMembershipData();

    CreateCourseMemebershipPayload courseMembershipPayloadLearn = fixture.getCourseMembershipPayload();
    String payload = mapper.writeValueAsString( courseMembershipPayloadLearn );
    Site mockSite = mock( Site.class );
    SiteView mockSiteView = mock( SiteView.class );
    when( siteService.getCompleteSite( anyString() ) ).thenReturn( mockSite );
    when( mockSite.getSiteView() ).thenReturn( mockSiteView );
    when( mockSiteView.getHostUrl() ).thenReturn( "https://playground.blackboard.com" );
    PowerMockito.when( LearnRestClient.generateToken( anyString(), anyString(), anyString() ) ).thenReturn( "token" );
    PowerMockito.when( LearnRestClient.createCourseMembership( anyString(), anyString(), any(), anyString() ) )
        .thenReturn( membershipResponse );
    Association association = new Association();
    association.setAvailable( true );
    association.setEnabled( true );
    when( restAPIClient.getEntityById( anyString(), eq( Association.class ), any() ) ).thenReturn( association );
    Map<String, Object> map = service.createCourseMembership( payload, "site123", "paymentGateway", "key", "secret", "",
                                                              "paymentGateway" );
    assertNotNull( map );
  }

  @Test
  public void courseMembershipCount() throws Exception
  {
    Site mockSite = mock( Site.class );
    SiteView mockSiteView = mock( SiteView.class );
    when( siteService.getCompleteSite( anyString() ) ).thenReturn( mockSite );
    when( mockSite.getSiteView() ).thenReturn( mockSiteView );
    when( mockSiteView.getHostUrl() ).thenReturn( "https://playground.blackboard.com" );
    PowerMockito.when( LearnRestClient.generateToken( anyString(), anyString(), anyString() ) ).thenReturn( "token" );
    PowerMockito
        .when( LearnRestClient.getMembershipCount( anyString(), anyString(), any( Integer.class ),
                                                   any( LmsIntegration.class ), anyString(), anyList() ) )
        .thenReturn( 10 );
    Association association = new Association();
    association.setAvailable( true );
    association.setEnabled( true );
    LmsIntegration lmsIntegration = setLmsIntegration( "site1", "programView", "test_course1", "key", "secret",
                                                       "programView", null );

    Integer count = service.courseMembershipCount( lmsIntegration );
    assertTrue( 10 == count );

    PowerMockito.when( LearnRestClient.generateToken( anyString(), anyString(), anyString() ) ).thenReturn( null );
    assertThrows( BadGatewayException.class, () -> service.courseMembershipCount( lmsIntegration ) );

  }

  @Test
  public void createUser() throws Exception
  {
    Map<String, Object> userResponse = fixture.getUserData();

    CreateUserPayload userPayloadLearn = fixture.getUserPayload();
    String payload = mapper.writeValueAsString( userPayloadLearn );
    Site mockSite = mock( Site.class );
    SiteView mockSiteView = mock( SiteView.class );
    when( siteService.getCompleteSite( anyString() ) ).thenReturn( mockSite );
    when( mockSite.getSiteView() ).thenReturn( mockSiteView );
    when( mockSiteView.getHostUrl() ).thenReturn( "https://playground.blackboard.com" );
    PowerMockito.when( LearnRestClient.generateToken( anyString(), anyString(), anyString() ) ).thenReturn( "token" );
    PowerMockito.when( LearnRestClient.createUser( anyString(), anyString(), any(), any() ) )
        .thenReturn( userResponse );
    Association association = new Association();
    association.setAvailable( true );
    association.setEnabled( true );
    Map<String, Object> configuration = new HashMap<>();

    List<String> learnerIr = new ArrayList<>();
    learnerIr.add( "STUDENT" );

    List<String> learnerSr = new ArrayList<>();
    learnerSr.add( "TDM_ADMIN" );

    configuration.put( "learner_ir", learnerIr );
    configuration.put( "learner_sr", learnerSr );
    association.setConfiguration( configuration );

    Map<String, String> paramMap = new HashMap<>();
    paramMap.put( Constants.DEV_PORTAL_KEY, "key" );
    paramMap.put( Constants.DEV_PORTAL_SECRET, "secret" );
    paramMap.put( Constants.CREATE_USER_ALLOWED_APPLICATIONS, "userWorkflowManager" );

    MultivaluedMap<String, String> multiQueryParams = new MultivaluedHashMap<>();
    multiQueryParams.add( "fields", "" );

    when( restAPIClient.getEntityById( anyString(), eq( Association.class ), any() ) ).thenReturn( association );
    Map<String, Object> map = service.createUser( payload, "site123", "userWorkflowManager", paramMap, multiQueryParams,
                                                  "learn" );
    assertNotNull( map );
  }

  @Test
  public void checkUserExists() throws Exception
  {
    Site mockSite = mock( Site.class );
    SiteView mockSiteView = mock( SiteView.class );
    when( siteService.getCompleteSite( anyString() ) ).thenReturn( mockSite );
    when( mockSite.getSiteView() ).thenReturn( mockSiteView );
    when( mockSiteView.getHostUrl() ).thenReturn( "https://playground.blackboard.com" );
    PowerMockito.when( LearnRestClient.generateToken( anyString(), anyString(), anyString() ) ).thenReturn( "token" );
    PowerMockito.when( LearnRestClient.checkUserExists( anyString(), anyString(), anyString() ) )
        .thenReturn( 200 );
    Association association = new Association();
    association.setAvailable( true );
    association.setEnabled( true );

    Map<String, String> paramMap = new HashMap<>();
    paramMap.put( Constants.DEV_PORTAL_KEY, "key" );
    paramMap.put( Constants.DEV_PORTAL_SECRET, "secret" );
    paramMap.put( Constants.CREATE_USER_ALLOWED_APPLICATIONS, "userWorkflowManager" );

    when( restAPIClient.getEntityById( anyString(), eq( Association.class ), any() ) ).thenReturn( association );
    Integer userExistStatusCode = service.checkUserExists( "site123", "userWorkflowManager", paramMap, "learn", "user1" );
    assertNotNull( userExistStatusCode );
  }

  private LmsIntegration setLmsIntegration( String siteId, String applicationId, String courseId, String devPortalKey,
                                            String devPortalSecret, String lmsAllowedApplications,
                                            Map<String, String> queryParam )
  {
    LmsIntegration lmsIntegration = new LmsIntegration();

    lmsIntegration.setApplicationId( applicationId );
    lmsIntegration.setDevPortalKey( devPortalKey );
    lmsIntegration.setDevPortalSecret( devPortalSecret );
    lmsIntegration.setLmsAllowedApplications( lmsAllowedApplications );
    lmsIntegration.setSiteId( siteId );
    lmsIntegration.setQueryParam( queryParam );
    lmsIntegration.setCourseId( courseId );

    return lmsIntegration;

  }

  @After
  public void tearDown()
  {
    genericUtilMock.close();
    learnRestClient.close();
  }
}
