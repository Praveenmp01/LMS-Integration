package com.blackboard.platform.extensions.lmsintegrations.layer.util;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.blackboard.platform.extensions.lmsintegrations.layer.models.*;
import com.blackboard.platform.extensions.restapi.client.RestAPIExternalClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { LearnRestClient.class, RestAPIExternalClient.class } )
@PowerMockIgnore( { "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*", "org.w3c.dom.*",
                    "javax.net.ssl.*", "jdk.internal.net.*", "java.net.http.HttpResponse.*" } )
public class LearnRestClientTest
{

  private RestAPIExternalClient restApiExternalClient;

  @SuppressWarnings( "unchecked" )
  @Test
  public void generateToken() throws Exception
  {
    String tokenData = "{\n  \"access_token\": \"token\",\n  \"token_type\":\"oauth2\",\n  \"expires_in\":123,\n  \"user_id\":\"user1\"\n}";

    Map<String, String> tokenResponse = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    tokenResponse = mapper.readValue( tokenData, Map.class );

    restApiExternalClient = mock( RestAPIExternalClient.class );
    PowerMockito.whenNew( RestAPIExternalClient.class ).withAnyArguments().thenReturn( restApiExternalClient );

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add( "Authorization", "Basic a2V5OnNlY3JldA==" );

    MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
    body.add( "grant_type", "client_credentials" );

    when( restApiExternalClient.executeRestApiClient( "https://learn.blackboard.com/learn/api/public/v1/oauth2/token",
                                                      Map.class, null, HttpMethod.POST,
                                                      "application/x-www-form-urlencoded", headers, body ) )
                                                          .thenReturn( tokenResponse );

    assertNotNull( LearnRestClient.generateToken( "key", "secret", "https://learn.blackboard.com" ) );

  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void getMembershipCount() throws Exception
  {
    String courseMembershipData = "{\n  \"results\":\n  [\n    {\"id\":\"_54_1\",\"userId\":\"_19_1\",\"courseId\":\"_14_1\",\"dataSourceId\":\"_2_1\",\"created\":\"2022-06-01T10:24:50.437Z\",\"modified\":\"2022-06-01T10:24:56.199Z\",\"availability\":{\"available\":\"Yes\"},\"courseRoleId\":\"Student\"}\n  ]\n}\n";
    Response mockedResponse = Mockito.mock( Response.class );
    Map<String, Object> courseMemInfo = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    courseMemInfo = mapper.readValue( courseMembershipData, Map.class );

    when( mockedResponse.getStatus() ).thenReturn( 200 );
    when( mockedResponse.readEntity( Map.class ) ).thenReturn( courseMemInfo );

    LmsIntegration lmsIntegration = new LmsIntegration();
    lmsIntegration.setCourseId( "course1" );

    List<Map<String, Object>> membershipResultSet = new ArrayList<>();

    restApiExternalClient = mock( RestAPIExternalClient.class );
    PowerMockito.whenNew( RestAPIExternalClient.class ).withAnyArguments().thenReturn( restApiExternalClient );

    PowerMockito.when( restApiExternalClient.executeRestApiClient( anyString(), any(), anyString(), anyString(), any(),
                                                                   any() ) )
        .thenReturn( mockedResponse );

    assertNotNull( LearnRestClient.getMembershipCount( "https://learn.blackboard.com/api/v2/learn", "test_user1", 0,
                                                       lmsIntegration, "", membershipResultSet ) );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void createMembership() throws Exception
  {
    String courseMembershipData = "{\"id\":\"_54_1\",\"userId\":\"_19_1\",\"courseId\":\"_14_1\",\"dataSourceId\":\"_2_1\",\"availability\":{\"available\":\"Yes\"},\"courseRoleId\":\"Student\"}";
    Response mockedResponse = Mockito.mock( Response.class );
    Map<String, Object> courseMemInfo = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    courseMemInfo = mapper.readValue( courseMembershipData, Map.class );

    when( mockedResponse.getStatus() ).thenReturn( 201 );
    when( mockedResponse.readEntity( Map.class ) ).thenReturn( courseMemInfo );

    CreateCourseMemebershipPayload payload = new CreateCourseMemebershipPayload();
    payload.setCourseId( "course1" );
    payload.setUserId( "user1" );
    payload.setLmsType( "learn" );

    restApiExternalClient = mock( RestAPIExternalClient.class );
    PowerMockito.whenNew( RestAPIExternalClient.class ).withAnyArguments().thenReturn( restApiExternalClient );

    PowerMockito.when( restApiExternalClient.executeRestApiClient( anyString(), any(), anyString(), anyString(), any(),
                                                                   any() ) )
        .thenReturn( mockedResponse );

    assertNotNull( LearnRestClient.createCourseMembership( "https://learn.blackboard.com/", "token", payload, "" ) );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void createUser() throws Exception
  {
    String userData = "{\"id\":\"_54_1\",\"userId\":\"testUser_121223\",\"dataSourceId\":\"_2_1\",\"availability\":{\"available\":\"Yes\"}}";
    Response mockedResponse = Mockito.mock( Response.class );
    Map<String, Object> courseMemInfo = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    courseMemInfo = mapper.readValue( userData, Map.class );

    when( mockedResponse.getStatus() ).thenReturn( 201 );
    when( mockedResponse.readEntity( Map.class ) ).thenReturn( courseMemInfo );

    CreateUserPayload user = new CreateUserPayload();

    user.setExternalId( "1231212" );
    user.setUserName( "testUser_121223" );
    Map<String, Object> name = new HashMap<>();
    name.put( "given", "name1" );
    name.put( "family", "name2" );
    user.setName( name );
    user.setDataSourceId( "_2_1" );
    user.setPassword( "pass" );

    MultivaluedMap<String, String> multiQueryParams = new MultivaluedHashMap<>();

    restApiExternalClient = mock( RestAPIExternalClient.class );
    PowerMockito.whenNew( RestAPIExternalClient.class ).withAnyArguments().thenReturn( restApiExternalClient );

    PowerMockito.when( restApiExternalClient.executeRestApiClient( anyString(), any(), anyString(), anyString(), any(),
                                                                   any() ) )
        .thenReturn( mockedResponse );

    assertNotNull( LearnRestClient.createUser( "https://learn.blackboard.com/", "token", user, multiQueryParams ) );
  }

  @Test
  public void checkUserExists() throws Exception
  {
    Response mockedResponse = Mockito.mock( Response.class );
    when( mockedResponse.getStatus() ).thenReturn( 200 );

    restApiExternalClient = mock( RestAPIExternalClient.class );
    PowerMockito.whenNew( RestAPIExternalClient.class ).withAnyArguments().thenReturn( restApiExternalClient );

    PowerMockito.when( restApiExternalClient.executeRestApiClient( anyString(), any(), anyString(), anyString(), any(),
                                                                   any() ) )
        .thenReturn( mockedResponse );

    assertNotNull( LearnRestClient.checkUserExists( "https://learn.blackboard.com/", "token", "user1" ) );
  }
}
