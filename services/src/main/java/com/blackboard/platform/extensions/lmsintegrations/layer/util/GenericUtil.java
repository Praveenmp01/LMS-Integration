package com.blackboard.platform.extensions.lmsintegrations.layer.util;

import com.blackboard.platform.extensions.lmsintegrations.layer.services.LmsIntegrationsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenericUtil
{

  private static final Logger LOG = LogManager.getLogger( LmsIntegrationsService.class );
  
  @SuppressWarnings( "unchecked" )
  public static Map<String, Object> getValueAsMap( String response )
  {
    try
    {
      ObjectMapper map = new ObjectMapper();
      return map.readValue( response, HashMap.class );
    }
    catch ( JsonMappingException e )
    {
      LOG.error( "Exception raised when mapping content from the response: ", e );
    }
    catch ( JsonProcessingException e )
    {
      LOG.error( "Exception raised when processing response: ", e );
    }
    return null;
  }
  
}
