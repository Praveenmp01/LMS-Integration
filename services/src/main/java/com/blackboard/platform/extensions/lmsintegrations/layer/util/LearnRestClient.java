package com.blackboard.platform.extensions.lmsintegrations.layer.util;

import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.*;
import static com.blackboard.platform.extensions.restapi.model.Constants.*;

import com.blackboard.platform.extensions.lmsintegrations.layer.exceptions.*;
import com.blackboard.platform.extensions.lmsintegrations.layer.models.*;
import com.blackboard.platform.extensions.restapi.client.RestAPIExternalClient;
import com.blackboard.platform.extensions.restapi.exceptions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for calling Learn REST API
 */
public class LearnRestClient
{

  private static final Logger LOG = LoggerFactory.getLogger( LearnRestClient.class );

  /**
   * @param clientId
   * @param clientSecret
   * @param url
   * @return rest token
   * @throws MalformedURLException
   * @throws URISyntaxException
   * @throws Exception
   */
  @SuppressWarnings( "unchecked" )
  public static String generateToken( String consumerKey, String sharedSecret, String hostname )
  {
    String auth = consumerKey + ":" + sharedSecret;
    String hash = Base64.getEncoder().encodeToString( auth.getBytes() );
    String oauthTokenUrl = hostname + OAUTH_TOKEN_URL_PATH;

    RestAPIExternalClient restAPIExternalClient = new RestAPIExternalClient();
    try
    {
      MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
      headers.add( "Authorization", "Basic " + hash );

      MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
      body.add( "grant_type", "client_credentials" );
      Map<String, String> response = restAPIExternalClient
          .executeRestApiClient( oauthTokenUrl, Map.class, null, HttpMethod.POST, "application/x-www-form-urlencoded",
                                 headers, body );

      if ( null != response )
      {
        return response.get( "access_token" );
      }
    }
    catch ( Exception e )
    {
      LOG.error( "Exception raised when generating the token {}", e );
    }
    return null;
  }

  @SuppressWarnings( "unchecked" )
  public static Map<String, Object> createCourseMembership( String hostname, String oauth2Token,
                                                            CreateCourseMemebershipPayload payload, String fields )
    throws ResourceAlreadyExistsException, ResourceNotFoundException, ResourceBadRequestException, BadGatewayException,
    LmsIntegrationException, LearnRestException, RestExternalClientException
  {

    RestAPIExternalClient restAPIExternalClient = new RestAPIExternalClient();
    String courseId = payload.getCourseId();
    String userId = payload.getUserId();

    String queryParam = StringUtils.isNotBlank( fields ) ? "?fields=" + fields : "";

    String courseMembershipUrl = hostname + LEARN_COURSE_MEMBERSHIP_PATH.replace( "{courseId}", courseId )
        .replace( "{userId}", userId ) + queryParam;

    if ( StringUtils.isBlank( payload.getCourseRoleId() ) )
    {
      payload.setCourseRoleId( STUDENT );
    }

    Map<String, Object> createCourseMembershipPayload = reBuildCourseMembershipPayloadForLearn( payload );

    LOG.debug( "CreateCourseMembershipPayload: {}", createCourseMembershipPayload );

    Map<String, Object> courseMembershipInfo = new HashMap<String, Object>();

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add( "Authorization", "Bearer " + oauth2Token );

    Entity<Map<String, Object>> entity = Entity.entity( createCourseMembershipPayload,
                                                        MediaType.APPLICATION_JSON_TYPE );

    Response response = restAPIExternalClient.executeRestApiClient( courseMembershipUrl, null, HttpMethod.PUT,
                                                                    "application/x-www-form-urlencoded", headers,
                                                                    entity );

    if ( null != response )
    {
      if ( response.getStatus() == Status.CREATED.getStatusCode() )
      {
        try
        {
          courseMembershipInfo = response.readEntity( Map.class );
        }
        catch ( Exception e )
        {
          ExceptionSource exceptionSource = new ExceptionSource( FIELDS, LMS_INTEGRATION );
          throw new LmsIntegrationException( LMS_INTEGRATION_ERROR, exceptionSource, null,
                                             LMS_INTEGRATION_ERROR_MESSAGE );
        }
        LOG.debug( "courseMembershipInfo: {} ", courseMembershipInfo );
      }
      else if ( response.getStatus() == Status.CONFLICT.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
      else if ( response.getStatus() == Status.NOT_FOUND.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
      else if ( response.getStatus() == Status.BAD_REQUEST.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
      else if ( response.getStatus() == Status.UNAUTHORIZED.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
      else if ( response.getStatus() == Status.FORBIDDEN.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
    }
    return courseMembershipInfo;
  }

  /**
   * Re-build the fields in payload that is required for Learn
   * 
   * @param payload
   * @return payload as map
   */
  private static Map<String, Object> reBuildCourseMembershipPayloadForLearn( CreateCourseMemebershipPayload payload )
  {
    Map<String, Object> payloadMap = getPayloadAsMap( payload );

    payloadMap.remove( COURSE_ID );
    payloadMap.remove( USER_ID );
    payloadMap.remove( LMS_TYPE );
    payloadMap.remove( APPLICATION_ID );
    payloadMap.remove( SITE_ID );
    return payloadMap;
  }

  private static Map<String, Object> getPayloadAsMap( Object payload )
  {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> payloadMap = mapper.convertValue( payload, new TypeReference<Map<String, Object>>()
      {
      } );
    return payloadMap;
  }

  @SuppressWarnings( "unchecked" )
  public static int getMembershipCount( String hostname, String oauth2token, Integer courseMembershipCount,
                                        LmsIntegration lmsIntegration, String nextPageUrl,
                                        List<Map<String, Object>> membershipResultSet )
    throws JsonMappingException, JsonProcessingException, LmsIntegrationException, LearnRestException,
    ResourceBadRequestException, GenericServiceException, RestExternalClientException
  {
    Map<String, String> queryParam = lmsIntegration.getQueryParam();
    String courseRoleId = "";
    String availability = "";
    StringBuilder queryParamBuilder = new StringBuilder();

    if ( null != queryParam )
    {
      courseRoleId = queryParam.get( COURSE_ROLE_ID ) != null ? queryParam.get( COURSE_ROLE_ID ) : "";
      availability = queryParam.get( AVAILABILITY_FILTER ) != null ? queryParam.get( AVAILABILITY_FILTER ) : "";
    }

    if ( StringUtils.isNotBlank( availability ) )
    {
      String availabilityValues[] = availability.split( COMMA_DELIMITER );
      for ( String s : availabilityValues )
      {
        if ( !s.equals( "Yes" ) && !s.equals( "No" ) && !s.equals( "Disabled" ) )
        {
          ExceptionSource exceptionSource = new ExceptionSource( FIELDS, AVAILABILITY_FILTER );
          throw new ResourceBadRequestException( INVALID_REQUEST, exceptionSource, null, INVALID_VALUES );
        }
      }
    }

    if ( StringUtils.isNotBlank( courseRoleId ) && courseRoleId.split( COMMA_DELIMITER ).length == 1 )
    {
      queryParamBuilder.append( QUERY_PARAM_DELIMITER );
      queryParamBuilder.append( "role=" ).append( courseRoleId );
    }

    if ( StringUtils.isNotBlank( availability ) && availability.split( COMMA_DELIMITER ).length == 1 )
    {
      if ( queryParamBuilder.indexOf( QUERY_PARAM_DELIMITER ) < 0 )
      {
        queryParamBuilder.append( QUERY_PARAM_DELIMITER );
      }
      else
      {
        queryParamBuilder.append( "&" );
      }
      queryParamBuilder.append( "availability.available=" ).append( availability );
    }

    Integer filteredMembershipCount = 0;
    String courseMembershipUrl = "";

    if ( StringUtils.isBlank( nextPageUrl ) )
    {
      courseMembershipUrl = hostname
                            + GET_LEARN_COURSE_MEMBERSHIP_PATH.replace( "{courseId}", lmsIntegration.getCourseId() )
                            + queryParamBuilder.toString();
    }
    else
    {
      courseMembershipUrl = nextPageUrl;
    }

    LOG.debug( "courseMembershipUrl: {}", courseMembershipUrl );

    Response membershipResponseObj = getHttpResponse( courseMembershipUrl, oauth2token );
    Map<String, Object> membership = new HashMap<>();

    if ( null != membershipResponseObj )
    {
      if ( membershipResponseObj.getStatus() == 200 )
      {
        membership = membershipResponseObj.readEntity( Map.class );
      }
      if ( Status.BAD_REQUEST.getStatusCode() == membershipResponseObj.getStatus() )
      {
        throw new LearnRestException( membershipResponseObj.readEntity( String.class ) );
      }
      else if ( Status.UNAUTHORIZED.getStatusCode() == membershipResponseObj.getStatus() )
      {
        throw new LearnRestException( membershipResponseObj.readEntity( String.class ) );
      }
      else if ( Status.NOT_FOUND.getStatusCode() == membershipResponseObj.getStatus() )
      {
        throw new LearnRestException( membershipResponseObj.readEntity( String.class ) );
      }
      else if ( Status.FORBIDDEN.getStatusCode() == membershipResponseObj.getStatus() )
      {
        throw new LearnRestException( membershipResponseObj.readEntity( String.class ) );
      }

      if ( null != membership )
      {

        List<Map<String, Object>> results = (List<Map<String, Object>>) membership.get( "results" );

        if ( null != results )
        {
          LOG.debug( "results size: {}", results.size() );
          membershipResultSet.addAll( results );
          courseMembershipCount += results.size();

          if ( membership.toString().contains( NEXT_PAGE ) )
          {
            Map<String, Object> paging = (Map<String, Object>) membership.get( "paging" );
            String nextPage = paging.get( NEXT_PAGE ) != null ? paging.get( NEXT_PAGE ).toString() : "";
            if ( StringUtils.isNotBlank( nextPage ) )
            {
              LOG.debug( " next Page: {}", nextPage );
              return getMembershipCount( hostname, oauth2token, courseMembershipCount, lmsIntegration,
                                         hostname + nextPage, membershipResultSet );
            }
          }
        }
      }
    }

    List<Map<String, Object>> totalResultSet = membershipResultSet;

    List<Map<String, Object>> filterMembershipResultsBasedOnAvailability = filterMembershipBasedOnAvailability( availability,
                                                                                                                totalResultSet );
    List<Map<String, Object>> filterMembershipResultsBasedOnRole = filterMembershipBasedOnRole( courseRoleId,
                                                                                                filterMembershipResultsBasedOnAvailability );
    filteredMembershipCount = filterMembershipResultsBasedOnRole.size();

    LOG.debug( "filteredMembershipCount: {}", filteredMembershipCount );

    return filteredMembershipCount;

  }

  private static List<Map<String, Object>> filterMembershipBasedOnRole( String courseRoleId,
                                                                        List<Map<String, Object>> results )
  {

    if ( courseRoleId.trim().length() == 0 )
    {
      return results;
    }

    String[] courseRoleType = courseRoleId.split( COMMA_DELIMITER );

    List<Map<String, Object>> filteredResults = null;
    if ( null != results )
    {
      filteredResults = new ArrayList<>();
      for ( Map<String, Object> membershipData : results )
      {
        for ( String role : courseRoleType )
        {
          if ( membershipData.get( "courseRoleId" ).toString().equalsIgnoreCase( role.trim() ) )
          {
            filteredResults.add( membershipData );
          }
        }
      }
    }
    return filteredResults;
  }

  @SuppressWarnings( "unchecked" )
  private static List<Map<String, Object>> filterMembershipBasedOnAvailability( String availability,
                                                                                List<Map<String, Object>> results )
  {
    if ( availability.trim().length() == 0 )
    {
      return results;
    }
    String[] availabilityTypes = availability.split( COMMA_DELIMITER );
    List<Map<String, Object>> filteredResults = null;

    if ( null != results )
    {
      filteredResults = new ArrayList<>();

      for ( Map<String, Object> membershipData : results )
      {
        Map<String, String> availabilityMap = (Map<String, String>) membershipData.get( "availability" );
        for ( String availabilityType : availabilityTypes )
        {
          if ( availabilityMap.get( "available" ).toString().equalsIgnoreCase( availabilityType.trim() ) )
          {
            filteredResults.add( membershipData );
          }
        }
      }
    }
    return filteredResults;
  }

  private static Response getHttpResponse( String url, String oauth2Token )
    throws GenericServiceException, RestExternalClientException
  {
    RestAPIExternalClient restAPIExternalClient = new RestAPIExternalClient();
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add( "Authorization", "Bearer " + oauth2Token );
    Response response = restAPIExternalClient.executeRestApiClient( url, null, HttpMethod.GET,
                                                                    MediaType.APPLICATION_JSON, headers, null );
    return response;
  }

  @SuppressWarnings( "unchecked" )
  public static Map<String, Object> createUser( String hostname, String oauth2Token, CreateUserPayload payload,
                                                MultivaluedMap<String, String> queryParams )
    throws ResourceAlreadyExistsException, ResourceNotFoundException, ResourceBadRequestException, BadGatewayException,
    LmsIntegrationException, LearnRestException, RestExternalClientException
  {

    String fields = queryParams.getFirst( "fields" );
    String queryParam = StringUtils.isNotBlank( fields ) ? "?fields=" + fields : "";

    LOG.debug( "queryParam {}", queryParam );
    String createUserUrl = hostname + LEARN_CREATE_USER_PATH + queryParam;
    LOG.debug( "createUserUrl {}", createUserUrl );

    RestAPIExternalClient restAPIExternalClient = new RestAPIExternalClient();

    Map<String, Object> userInfo = new HashMap<String, Object>();

    Map<String, Object> payloadMap = getPayloadAsMap( payload );

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add( "Authorization", "Bearer " + oauth2Token );

    Entity<Map<String, Object>> entity = Entity.entity( payloadMap, MediaType.APPLICATION_JSON_TYPE );

    Response response = restAPIExternalClient.executeRestApiClient( createUserUrl, null, HttpMethod.POST,
                                                                    "application/x-www-form-urlencoded", headers,
                                                                    entity );

    if ( null != response )
    {
      LOG.debug( "user Response status Code: {}", response.getStatus() );
      if ( response.getStatus() == Status.CREATED.getStatusCode() )
      {
        try
        {
          userInfo = response.readEntity( Map.class );
        }
        catch ( Exception e )
        {
          ExceptionSource exceptionSource = new ExceptionSource( FIELDS, LMS_INTEGRATION );
          throw new LmsIntegrationException( LMS_INTEGRATION_ERROR, exceptionSource, null,
                                             LMS_INTEGRATION_ERROR_MESSAGE );
        }
      }
      else if ( response.getStatus() == Status.CONFLICT.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
      else if ( response.getStatus() == Status.NOT_FOUND.getStatusCode() )
      {

        throw new LearnRestException( response.readEntity( String.class ) );
      }
      else if ( response.getStatus() == Status.BAD_REQUEST.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
      else if ( response.getStatus() == Status.UNAUTHORIZED.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
      else if ( response.getStatus() == Status.FORBIDDEN.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
    }
    return userInfo;
  }
  
  public static Integer checkUserExists( String hostname, String oauth2Token, String userName )
    throws LearnRestException, RestExternalClientException
  {

    String checkUserExistsUrl = hostname + LEARN_CHECK_USER_EXISTS_PATH.replace( "{userName}",userName );
    LOG.debug( "checkUserExistsUrl {}", checkUserExistsUrl );

    RestAPIExternalClient restAPIExternalClient = new RestAPIExternalClient();

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add( "Authorization", "Bearer " + oauth2Token );

    Response response = restAPIExternalClient.executeRestApiClient( checkUserExistsUrl, null, HttpMethod.GET,
                                                                    "application/x-www-form-urlencoded", headers,
                                                                    null );
    if ( null != response )
    {
      LOG.debug( "user Response status Code: {}", response.getStatus() );
      if ( response.getStatus() == Status.OK.getStatusCode() )
      {
        return 200;
      }
      else if ( response.getStatus() == Status.NOT_FOUND.getStatusCode() )
      {
        return 404;
      }
      else if ( response.getStatus() == Status.BAD_REQUEST.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
      else if ( response.getStatus() == Status.UNAUTHORIZED.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
      else if ( response.getStatus() == Status.FORBIDDEN.getStatusCode() )
      {
        throw new LearnRestException( response.readEntity( String.class ) );
      }
    }
    return 200;
  }

}
