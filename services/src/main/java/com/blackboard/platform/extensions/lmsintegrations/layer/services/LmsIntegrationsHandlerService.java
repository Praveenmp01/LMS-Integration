package com.blackboard.platform.extensions.lmsintegrations.layer.services;

import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.blackboard.platform.extensions.restapi.client.RestAPIClient;
import com.blackboard.platform.extensions.restapi.client.registrar.SiteService;
import com.blackboard.platform.extensions.restapi.exceptions.ResourceBadRequestException;
import com.blackboard.platform.extensions.restapi.exceptions.RestAPILTIInvalidException;
import com.blackboard.platform.extensions.restapi.handler.RequestEventHandler;
import com.blackboard.platform.extensions.restapi.ssm.ParamStoreClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LmsIntegrationsHandlerService
{

  private RequestEventHandler requestEventHandler;
  private LmsIntegrationsService service;
  private ObjectMapper objectMapper;
  private final ParamStoreClient paramStoreClient = new ParamStoreClient();

  private Map<String, String> parameterMap;

  public Map<String, String> getParameterMap()
  {
    return parameterMap;
  }

  private static final Logger LOGGER = LogManager.getLogger( LmsIntegrationsHandlerService.class );

  public LmsIntegrationsHandlerService( final APIGatewayProxyRequestEvent apiGatewayReqEvent,
                                        final Context apiGatewayReqContext ) throws JsonProcessingException,
                                                                             RestAPILTIInvalidException,
                                                                             ResourceBadRequestException
  {

    this.requestEventHandler = new RequestEventHandler( apiGatewayReqEvent );

    Map<String, String> headers = apiGatewayReqEvent.getHeaders();
    MultivaluedHashMap<String, Object> mvHeaders = new MultivaluedHashMap<>();
    for ( Entry<String, String> entryHeader : headers.entrySet() )
    {
      mvHeaders.add( entryHeader.getKey(), entryHeader.getValue() );
    }
    RestAPIClient restAPIClient = new RestAPIClient( mvHeaders );

    this.parameterMap = getParameterStores( parameterMap );

    initLmsIntegrationsService( initSiteService( parameterMap ), restAPIClient );
  }

  private void initLmsIntegrationsService( SiteService siteService, RestAPIClient restAPIClient )
  {
    this.service = new LmsIntegrationsService( siteService, restAPIClient );
  }

  public LmsIntegrationsHandlerService( Map<String, String> paramMap )
  {
    this.parameterMap = getParameterStores( paramMap );
    initLmsIntegrationsService( initSiteService( parameterMap ), null );
  }

  private SiteService initSiteService( Map<String, String> paramMap )
  {
    return new SiteService( paramMap.get( TRUSTED_URL ), paramMap.get( TRUSTED_KEY ), paramMap.get( TRUSTED_SECRET ) );
  }

  private Map<String, String> getParameterStores( Map<String, String> paramMap )
  {
    String trustedKey = null;
    String trustedSecret = null;
    String url = null;
    String devPortalKey = null;
    String devPortalSecret = null;
    String allowedApplications = null;
    String createUserAllowedApplications = null;

    if ( null == paramMap )
    {
      paramMap = new HashMap<>();
    }
    if ( paramMap.get( TRUSTED_KEY ) != null )
    {
      trustedKey = paramMap.get( TRUSTED_KEY );
      trustedSecret = paramMap.get( TRUSTED_SECRET );
      url = paramMap.get( TRUSTED_URL );
      devPortalKey = paramMap.get( DEV_PORTAL_KEY );
      devPortalSecret = paramMap.get( DEV_PORTAL_SECRET );
      allowedApplications = paramMap.get( ALLOWED_APPLICATIONS );
      createUserAllowedApplications = paramMap.get( CREATE_USER_ALLOWED_APPLICATIONS );
      LOGGER.debug( "trusted key is loaded from cache" );
    }
    else
    {
      LOGGER.debug( "ssm Client created" );
      trustedKey = paramStoreClient.getParamValue( TRUSTED_KEY, false );
      trustedSecret = paramStoreClient.getParamValue( TRUSTED_SECRET, true );
      url = paramStoreClient.getParamValue( TRUSTED_URL, false );
      devPortalKey = paramStoreClient.getParamValue( DEV_PORTAL_KEY, false );
      devPortalSecret = paramStoreClient.getParamValue( DEV_PORTAL_SECRET, true );
      allowedApplications = paramStoreClient.getParamValue( ALLOWED_APPLICATIONS, false );
      createUserAllowedApplications = paramStoreClient.getParamValue( CREATE_USER_ALLOWED_APPLICATIONS, false );
      
      LOGGER.debug( "trusted key, url, secret, devPortalKey and devPortalSecret are loaded from parameter store" );
      paramMap.put( TRUSTED_KEY, trustedKey );
      paramMap.put( TRUSTED_SECRET, trustedSecret );
      paramMap.put( TRUSTED_URL, url );
      paramMap.put( DEV_PORTAL_KEY, devPortalKey );
      paramMap.put( DEV_PORTAL_SECRET, devPortalSecret );
      paramMap.put( ALLOWED_APPLICATIONS, allowedApplications );
      paramMap.put( CREATE_USER_ALLOWED_APPLICATIONS, createUserAllowedApplications );
    }
    return paramMap;
  }

  public RequestEventHandler getRequestEventHandler()
  {
    return this.requestEventHandler;
  }

  public void setRequestEventHandler( RequestEventHandler requestEventHandler )
  {
    this.requestEventHandler = requestEventHandler;
  }

  public LmsIntegrationsService getService()
  {
    return this.service;
  }

  public void setService( LmsIntegrationsService service )
  {
    this.service = service;
  }

  public String getValueAsString( Object object ) throws JsonProcessingException
  {
    return this.objectMapper.writeValueAsString( object );
  }

  @SuppressWarnings( "unchecked" )
  public Map<String, Object> getValueAsMap( String json ) throws JsonMappingException, JsonProcessingException
  {
    return this.objectMapper.readValue( json, Map.class );
  }
}
