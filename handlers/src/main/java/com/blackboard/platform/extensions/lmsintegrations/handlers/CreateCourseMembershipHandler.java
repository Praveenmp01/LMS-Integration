package com.blackboard.platform.extensions.lmsintegrations.handlers;

import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.*;
import static com.blackboard.platform.extensions.restapi.model.Constants.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.blackboard.platform.extensions.lmsintegrations.layer.exceptions.*;
import com.blackboard.platform.extensions.lmsintegrations.layer.services.LmsIntegrationsHandlerService;
import com.blackboard.platform.extensions.restapi.exceptions.*;
import com.blackboard.platform.extensions.restapi.handler.RequestEventHandler;
import com.blackboard.platform.extensions.restapi.handler.ResponseEventHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.joda.time.DateTime;

import software.amazon.awssdk.utils.StringUtils;

public class CreateCourseMembershipHandler
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>
{
  APIGatewayProxyResponseEvent response;

  private static final Logger LOGGER = LogManager.getLogger( CreateCourseMembershipHandler.class );
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
    LOGGER.info( "Create CourseMembership Handler is invoked {}", apiGatewayReqEvent.toString() );
    ResponseEventHandler responseEventHandler = new ResponseEventHandler();
    try
    {
      ObjectMapper objectMapper = new ObjectMapper();
      LmsIntegrationsHandlerService lmsIntegrationhandlerService = new LmsIntegrationsHandlerService( apiGatewayReqEvent,
                                                                                                      apiGatewayReqContext );
      String siteId = lmsIntegrationhandlerService.getRequestEventHandler().getPathParam( SITE_ID );
      String applicationId = lmsIntegrationhandlerService.getRequestEventHandler().getPathParam( APPLICATION_ID );
      RequestEventHandler requestEventHandler = new RequestEventHandler( apiGatewayReqEvent );
      requestEventHandler.validateSiteAppInformation( applicationId );
      String bodyPayload = apiGatewayReqEvent.getBody();

      String fields = lmsIntegrationhandlerService.getRequestEventHandler().getQueryParam( "fields" );

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
      LOGGER.debug( "lmsAllowedApplications: {}", lmsAllowedApplications );

      LOGGER.debug( "Get the devPortalKey:  {}", devPortalKey );

      Map<String, Object> courseMembershipInfo;
      courseMembershipInfo = lmsIntegrationhandlerService.getService()
          .createCourseMembership( bodyPayload, siteId, applicationId, devPortalKey, devPortalSecret, fields,
                                   lmsAllowedApplications );

      String responseJSON = objectMapper.writeValueAsString( courseMembershipInfo );
      LOGGER.debug( "responseJSON {}", responseJSON );

      String id = null != courseMembershipInfo.get( ID ) ? courseMembershipInfo.get( ID ).toString() : "";

      LOGGER.info( "Create CourseMembership Handler : completed successfully" );
      String locationStr = LOCATION_URL.replace( "{url}", apiGatewayReqEvent.getRequestContext().getPath() )
          .replace( "{id}", id );
      return responseEventHandler
          .prepareSuccess( responseJSON, apiGatewayReqEvent.getHeaders().get( CORRELATION_ID ), locationStr )
          .withStatusCode( HttpStatus.CREATED_201 );

    }
    catch ( BadGatewayException | ResourceBadRequestException | ResourceNotFoundException
        | ResourceAlreadyExistsException | LmsIntegrationException | ResourceForbiddenException
        | RestExternalClientException re )
    {
      LOGGER.error( "ERROR on Create Membership Handler.", re );
      return responseEventHandler.prepareEntityError( re );
    }
    catch ( RestAPIInvalidAppSiteException | RestAPILTIInvalidException e )
    {
      LOGGER.error( "ERROR on Create Membership Handler.", e );
      return responseEventHandler.prepareError( e );
    }
    catch ( LearnRestException e )
    {
      LOGGER.error( "Learn REST API ERROR on Membership Count Handler", e );
      int statusCode = HttpStatus.NOT_FOUND_404;
      try
      {
        statusCode = getStatusCode( e.toString() );
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
      LOGGER.error( "ERROR on Create Membership Handler.", e );
      return responseEventHandler.prepareException( e );
    }
  }

  /**
   * Get the status code from Learn REST API error response
   * 
   * @param response
   * @return HttpStatus code as integer
   * @throws JsonMappingException
   * @throws JsonProcessingException
   */
  private Integer getStatusCode( String response ) throws JsonMappingException, JsonProcessingException
  {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> map = objectMapper.readValue( response, new TypeReference<Map<String, Object>>()
      {
      } );

    int status = (int) map.get( "status" );
    return status;
  }
}
