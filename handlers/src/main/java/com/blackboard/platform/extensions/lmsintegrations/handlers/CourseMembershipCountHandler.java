package com.blackboard.platform.extensions.lmsintegrations.handlers;

import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.*;
import static com.blackboard.platform.extensions.restapi.model.Constants.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.blackboard.platform.extensions.lmsintegrations.layer.exceptions.*;
import com.blackboard.platform.extensions.lmsintegrations.layer.models.LmsIntegration;
import com.blackboard.platform.extensions.lmsintegrations.layer.services.LmsIntegrationsHandlerService;
import com.blackboard.platform.extensions.restapi.exceptions.*;
import com.blackboard.platform.extensions.restapi.handler.ResponseEventHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.joda.time.DateTime;

public class CourseMembershipCountHandler
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>
{
  APIGatewayProxyResponseEvent response;

  private static final Logger LOGGER = LogManager.getLogger( CourseMembershipCountHandler.class );
  private Map<String, String> paramMap;
  private DateTime paramLastLoaded = new DateTime();

  /**
   * Class constructor
   * 
   * @param input is a APIGatewayProxyRequestEvent with event request
   * @param context is a Context with lambda context
   */
  public APIGatewayProxyResponseEvent handleRequest( final APIGatewayProxyRequestEvent apiGatewayReqEvent,
                                                     final Context apiGatewayReqContext )
  {
    LOGGER.info( "CourseMembership Count Handler is invoked {}", apiGatewayReqEvent.toString() );
    ResponseEventHandler responseEventHandler = new ResponseEventHandler();
    LmsIntegrationsHandlerService lmsIntegrationhandlerService = null;
    try
    {
      ObjectMapper objectMapper = new ObjectMapper();
      lmsIntegrationhandlerService = new LmsIntegrationsHandlerService( apiGatewayReqEvent, apiGatewayReqContext );
      String siteId = lmsIntegrationhandlerService.getRequestEventHandler().getPathParam( SITE_ID );
      String applicationId = lmsIntegrationhandlerService.getRequestEventHandler().getPathParam( APPLICATION_ID );
      String courseId = lmsIntegrationhandlerService.getRequestEventHandler().getPathParam( "courseId" );

      String availability = lmsIntegrationhandlerService.getRequestEventHandler().getQueryParam( AVAILABILITY_FILTER );
      String courseRoleId = lmsIntegrationhandlerService.getRequestEventHandler().getQueryParam( COURSE_ROLE_ID );

      Map<String, String> queryParam = new HashMap<>();

      if ( StringUtils.isNotBlank( courseRoleId ) )
      {
        queryParam.put( COURSE_ROLE_ID, courseRoleId );
      }
      if ( StringUtils.isNotBlank( availability ) )
      {
        queryParam.put( AVAILABILITY_FILTER, availability );
      }

      String cacheTime = System.getenv( CACHE_TIME );
      if ( StringUtils.isNotBlank( cacheTime ) )
      {
        //Refresh time is static at the moment with 60 secs, this can be moved to env variable
        if ( paramLastLoaded.isBefore( DateTime.now().minusMinutes( Integer.parseInt( cacheTime ) ) ) )
        {
          LOGGER.debug( "Time to refresh the param cache" );
          paramMap = null;
          paramLastLoaded = new DateTime();
        }
      }

      paramMap = lmsIntegrationhandlerService.getParameterMap();

      String devPortalKey = Optional.ofNullable( ( paramMap.get( DEV_PORTAL_KEY ) ) ).orElse( "" );
      String devPortalSecret = Optional.ofNullable( ( paramMap.get( DEV_PORTAL_SECRET ) ) ).orElse( "" );
      String lmsAllowedApplications = Optional.ofNullable( ( paramMap.get( ALLOWED_APPLICATIONS ) ) ).orElse( "" );

      LOGGER.debug( "Get the devPortalKey:  {}", devPortalKey );

      LmsIntegration lmsIntegration = setLmsIntegration( siteId, applicationId, courseId, devPortalKey, devPortalSecret,
                                                         lmsAllowedApplications, queryParam );

      Integer courseMembershipCount = 0;
      courseMembershipCount = lmsIntegrationhandlerService.getService().courseMembershipCount( lmsIntegration );
      String responseJSON = objectMapper.writeValueAsString( successResponse( courseMembershipCount, lmsIntegration ) );
      LOGGER.debug( "responseJSON {}", responseJSON );

      LOGGER.info( "CourseMembership Count Handler : completed successfully" );
      return responseEventHandler
          .prepareSuccess( responseJSON, apiGatewayReqEvent.getHeaders().get( CORRELATION_ID ), "" )
          .withStatusCode( HttpStatus.OK_200 );

    }
    catch ( BadGatewayException | ResourceBadRequestException | ResourceNotFoundException
        | ResourceAlreadyExistsException | LmsIntegrationException | ResourceForbiddenException
        | RestExternalClientException re )
    {
      LOGGER.error( "ERROR on Membership Count Handler.", re );
      return responseEventHandler.prepareEntityError( re );
    }
    catch ( RestAPILTIInvalidException e )
    {
      LOGGER.error( "ERROR on Membership Count Handler.", e );
      return responseEventHandler.prepareError( e );
    }
    catch ( LearnRestException e )
    {
      LOGGER.error( "Learn REST API ERROR on Membership Count Handler", e );
      int statusCode = HttpStatus.NOT_FOUND_404;
      try
      {
        statusCode = lmsIntegrationhandlerService.getService().getStatusCode( e.toString() );
      }
      catch ( JsonProcessingException jpe )
      {
        LOGGER.error( "ERROR when returning the status code from Learn ", jpe );
        return responseEventHandler.prepareException( jpe );
      }
      return responseEventHandler.withBody( e.toString() ).withStatusCode( statusCode );
    }
    catch ( Exception e )
    {
      LOGGER.error( "ERROR on Membership Count Handler.", e );
      return responseEventHandler.prepareException( e );
    }
  }

  private Map<String, Object> successResponse( int count, LmsIntegration lmsIntegration )
  {
    Map<String, Object> response = new HashMap<>();
    response.put( "siteId", lmsIntegration.getSiteId() );
    response.put( "applicationId", lmsIntegration.getApplicationId() );
    response.put( "courseId", lmsIntegration.getCourseId() );
    response.put( "membershipCount", count );
    if ( MapUtils.isNotEmpty( lmsIntegration.getQueryParam() ) )
    {
      response.put( "filters", lmsIntegration.getQueryParam() );
    }
    else
    {
      response.put( "filters", new HashMap<>() );
    }
    return response;
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
}
