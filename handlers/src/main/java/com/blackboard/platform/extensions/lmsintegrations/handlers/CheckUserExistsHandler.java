package com.blackboard.platform.extensions.lmsintegrations.handlers;

import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.CACHE_TIME;
import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.STATUS_CODE;
import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.STATUS;
import static com.blackboard.platform.extensions.restapi.model.Constants.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.blackboard.platform.extensions.lmsintegrations.layer.exceptions.*;
import com.blackboard.platform.extensions.lmsintegrations.layer.services.LmsIntegrationsHandlerService;
import com.blackboard.platform.extensions.restapi.exceptions.*;
import com.blackboard.platform.extensions.restapi.handler.ResponseEventHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.joda.time.DateTime;

import software.amazon.awssdk.utils.StringUtils;

public class CheckUserExistsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>
{
  APIGatewayProxyResponseEvent response;

  private static final Logger LOGGER = LogManager.getLogger( CheckUserExistsHandler.class );
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
    LOGGER.info( "Check Learn user exists handler is invoked {}", apiGatewayReqEvent.toString() );
    ResponseEventHandler responseEventHandler = new ResponseEventHandler();
    try
    {
      ObjectMapper objectMapper = new ObjectMapper();
      LmsIntegrationsHandlerService lmsIntegrationhandlerService = new LmsIntegrationsHandlerService( apiGatewayReqEvent,
                                                                                                      apiGatewayReqContext );
      String siteId = lmsIntegrationhandlerService.getRequestEventHandler().getPathParam( SITE_ID );
      String applicationId = lmsIntegrationhandlerService.getRequestEventHandler().getPathParam( APPLICATION_ID );
      String lmsType = lmsIntegrationhandlerService.getRequestEventHandler().getPathParam( "lmsType" );
      String userName = lmsIntegrationhandlerService.getRequestEventHandler().getPathParam( "userName" );
      String cacheTime = System.getenv( CACHE_TIME );
      if ( StringUtils.isNotBlank( cacheTime ) )
      {
        if ( paramLastLoaded.isBefore( DateTime.now().minusMinutes( Integer.parseInt( cacheTime ) ) ) )
        {
          LOGGER.debug( "Time to refresh the param cache" );
          paramMap = null;
          paramLastLoaded = new DateTime();
        }
      }

      paramMap = lmsIntegrationhandlerService.getParameterMap();

      Integer userExistsResponseCode = lmsIntegrationhandlerService.getService()
          .checkUserExists( siteId, applicationId, paramMap, lmsType, userName );
      
        if ( 200 == userExistsResponseCode )
        {
          return responseEventHandler.prepareSuccess( "", apiGatewayReqEvent.getHeaders().get( CORRELATION_ID ), "" )
              .withStatusCode( HttpStatus.OK_200 );
        }
        else if ( 404 == userExistsResponseCode )
        {
          return responseEventHandler.prepareSuccess( "", apiGatewayReqEvent.getHeaders().get( CORRELATION_ID ), "" )
              .withStatusCode( HttpStatus.NOT_FOUND_404 );
        }
      LOGGER.info( "Check User Exists Handler : completed successfully" );
    }
    catch ( BadGatewayException | ResourceBadRequestException | ResourceNotFoundException
        | ResourceAlreadyExistsException | LmsIntegrationException | ResourceForbiddenException
        | RestExternalClientException re )
    {
      LOGGER.error( "ERROR on check user exists handler.", re );
      return responseEventHandler.prepareEntityError( re );
    }
    catch ( RestAPILTIInvalidException e )
    {
      LOGGER.error( "ERROR on check user exists handler", e );
      return responseEventHandler.prepareError( e );
    }
    catch ( LearnRestException e )
    {
      LOGGER.error( "Learn REST API ERROR on Check User Exists Handler", e );
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
      LOGGER.error( "ERROR on check user exists handler", e );
      return responseEventHandler.prepareException( e );
    }
    return responseEventHandler;
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

    int status = (int) map.get( STATUS );
    return status;
  }
}
