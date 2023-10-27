package com.blackboard.platform.extensions.lmsintegrations.layer.services;

import static com.blackboard.platform.extensions.lmsintegrations.layer.models.Constants.*;
import static com.blackboard.platform.extensions.restapi.model.Constants.*;

import com.blackboard.platform.extensions.lmsintegrations.layer.exceptions.*;
import com.blackboard.platform.extensions.lmsintegrations.layer.models.*;
import com.blackboard.platform.extensions.lmsintegrations.layer.util.LearnRestClient;
import com.blackboard.platform.extensions.restapi.client.RestAPIClient;
import com.blackboard.platform.extensions.restapi.client.registrar.SiteService;
import com.blackboard.platform.extensions.restapi.exceptions.*;
import com.blackboard.platform.extensions.restapi.model.profiles.association.Association;
import com.blackboard.platform.extensions.restapi.model.registrar.Site;
import com.blackboard.platform.extensions.restapi.service.AbstractGenericService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LmsIntegrationsService extends AbstractGenericService
{
  private static final Logger LOG = LogManager.getLogger( LmsIntegrationsService.class );
  private final SiteService siteService;
  private final RestAPIClient restAPIClient;
  private ObjectMapper mapper = new ObjectMapper();

  public LmsIntegrationsService( SiteService siteService, RestAPIClient restAPIClient )
  {
    this.siteService = siteService;
    this.restAPIClient = restAPIClient;
  }

  public Map<String, Object> createCourseMembership( String bodyPayload, String siteId, String applicationId,
                                                     String devPortalKey, String devPortalSecret, String fields,
                                                     String lmsAllowedApplications )
    throws ResourceNotFoundException, ResourceBadRequestException, ResourceAlreadyExistsException, BadGatewayException,
    LmsIntegrationException, LearnRestException, ResourceForbiddenException, RestExternalClientException
  {

    CreateCourseMemebershipPayload payload;
    try
    {
      payload = mapper.readValue( bodyPayload, new TypeReference<CreateCourseMemebershipPayload>()
        {
        } );
    }
    catch ( JsonProcessingException e )
    {
      ExceptionSource exceptionSource = new ExceptionSource( FIELDS, LMS_INTEGRATION );
      throw new LmsIntegrationException( LMS_INTEGRATION_ERROR, exceptionSource, null, LMS_INTEGRATION_ERROR_MESSAGE );
    }

    validateDevPortalAttributes( devPortalKey, devPortalSecret );
    // Validate association for the siteId and applicationId
    validateAssociation( applicationId, siteId );
    validatePayloadItems( payload );
    validateLmsType( payload, "" );

    // validate if the applications defined in the parameter store matches with the application id from the request
    if ( !isApplicationAllowed( lmsAllowedApplications, applicationId ) )
    {
      applicationPermissionException( applicationId );
    }

    Map<String, Object> courseMembership = null;
    String hostname = getLearnHostName( siteId );
    LOG.debug( "hostname: {}", hostname );
    courseMembership = enrollment( payload, hostname, devPortalKey, devPortalSecret, fields );
    LOG.debug( "courseMembership: {}", courseMembership );

    return courseMembership;
  }

  public Integer courseMembershipCount( LmsIntegration lmsIntegration )
    throws ResourceNotFoundException, ResourceBadRequestException, JsonMappingException, JsonProcessingException,
    ResourceAlreadyExistsException, BadGatewayException, LmsIntegrationException, LearnRestException,
    ResourceForbiddenException, GenericServiceException, RestExternalClientException
  {

    String siteId = lmsIntegration.getSiteId();
    String applicationId = lmsIntegration.getApplicationId();

    validateDevPortalAttributes( lmsIntegration.getDevPortalKey(), lmsIntegration.getDevPortalSecret() );

    // validate if the applications defined in the parameter store matches with the application id from the request
    if ( !isApplicationAllowed( lmsIntegration.getLmsAllowedApplications(), applicationId ) )
    {
      applicationPermissionException( applicationId );
    }

    // Validate association for the siteId and applicationId
    validateAssociation( applicationId, siteId );

    int courseMembershipCount = 0;
    // Create course membership in Learn if LMS type is Learn
    String hostname = getLearnHostName( siteId );

    LOG.debug( "hostname: {}", hostname );
    courseMembershipCount = getMembershipCount( lmsIntegration, hostname );

    LOG.debug( "courseMembership: {}", courseMembershipCount );

    return courseMembershipCount;
  }

  public Map<String, Object> createUser( String bodyPayload, String siteId, String applicationId,
                                         Map<String, String> paramMap, MultivaluedMap<String, String> queryParams,
                                         String lmsType )
    throws ResourceNotFoundException, ResourceBadRequestException, ResourceAlreadyExistsException, BadGatewayException,
    LmsIntegrationException, LearnRestException, ResourceForbiddenException, RestExternalClientException
  {
    CreateUserPayload payload;
    try
    {
      payload = mapper.readValue( bodyPayload, new TypeReference<CreateUserPayload>()
        {
        } );
    }
    catch ( JsonProcessingException e )
    {
      LOG.error( e );
      ExceptionSource exceptionSource = new ExceptionSource( FIELDS, LMS_INTEGRATION );
      throw new LmsIntegrationException( LMS_INTEGRATION_ERROR, exceptionSource, null, LMS_INTEGRATION_ERROR_MESSAGE );
    }

    String devPortalKey = Optional.ofNullable( ( paramMap.get( DEV_PORTAL_KEY ) ) ).orElse( "" );
    String devPortalSecret = Optional.ofNullable( ( paramMap.get( DEV_PORTAL_SECRET ) ) ).orElse( "" );
    String lmsAllowedApplications = Optional.ofNullable( ( paramMap.get( CREATE_USER_ALLOWED_APPLICATIONS ) ) )
        .orElse( "" );
    LOG.debug( "lmsAllowedApplications: {}", lmsAllowedApplications );

    LOG.debug( "Get the devPortalKey:  {}", devPortalKey );

    validateDevPortalAttributes( devPortalKey, devPortalSecret );
    // Validate association for the siteId and applicationId
    Association association = validateAssociation( applicationId, siteId );

    //validate institutionRoleIds
    validateInstitutionRoleIds( association, payload.getInstitutionRoleIds() );
    //validate systemRoleIds
    validateSystemRoleIds( association, payload.getSystemRoleIds() );
    //validatePayloadItems( payload );
    validateLmsType( payload, lmsType );

    // validate if the applications defined in the parameter store matches with the application id from the request
    if ( !isApplicationAllowed( lmsAllowedApplications, applicationId ) )
    {
      applicationPermissionException( applicationId );
    }

    Map<String, Object> userMap = null;
    String hostname = getLearnHostName( siteId );
    LOG.debug( "hostname: {}", hostname );

    userMap = createUserREST( payload, hostname, devPortalKey, devPortalSecret, queryParams, siteId );
    return userMap;
  }

  public Integer checkUserExists( String siteId, String applicationId, Map<String, String> paramMap,
                                               String lmsType, String userName )
    throws ResourceNotFoundException, ResourceBadRequestException, ResourceAlreadyExistsException, BadGatewayException,
    LmsIntegrationException, LearnRestException, ResourceForbiddenException, RestExternalClientException
  {

    String devPortalKey = Optional.ofNullable( ( paramMap.get( DEV_PORTAL_KEY ) ) ).orElse( "" );
    String devPortalSecret = Optional.ofNullable( ( paramMap.get( DEV_PORTAL_SECRET ) ) ).orElse( "" );
    String lmsAllowedApplications = Optional.ofNullable( ( paramMap.get( CREATE_USER_ALLOWED_APPLICATIONS ) ) )
        .orElse( "" );
    LOG.debug( "lmsAllowedApplications: {}", lmsAllowedApplications );

    LOG.debug( "Get the devPortalKey:  {}", devPortalKey );

    validateDevPortalAttributes( devPortalKey, devPortalSecret );
    // Validate association for the siteId and applicationId
    Association association = validateAssociation( applicationId, siteId );

    // validate LMS type
    validateLmsType( "", lmsType );

    Integer userExistsResponseCode;
    String hostname = getLearnHostName( siteId );
    LOG.debug( "hostname: {}", hostname );

    userExistsResponseCode = checkLearnUserExists( hostname, devPortalKey, devPortalSecret, siteId, userName );
    return userExistsResponseCode;
  }
  
  private Map<String,Object> getLearnerConfigurationFromAssociation( Association association ) throws ResourceForbiddenException
  {
    Map<String, Object> configuration = association.getConfiguration();
    if ( configuration.isEmpty() )
    {
      InvalidInstitutionRoleException();
    }

    return configuration;
  }

  private static boolean checkRoleExist( List<String> roleList1, List<String> roleList2 )
  {
    List<String> missingRoles = new ArrayList<String>();
    if ( roleList1.size() != roleList2.size() )
    {
      return false;
    }
    missingRoles = roleList1.stream().filter( value -> !roleList2.contains( value ) ).collect( Collectors.toList() );

    if ( !missingRoles.isEmpty() )
    {
      return false;
    }
    return true;
  }

  @SuppressWarnings( "unchecked" )
  private void validateInstitutionRoleIds( Association association, String [] institutionRoleIds ) throws ResourceBadRequestException, ResourceForbiddenException  
  {
    if( null == institutionRoleIds || institutionRoleIds.length == 0 )
    {
      List<String> listItems = new ArrayList<>();
      listItems.add( "institutionRoleIds" );
      throwValidationException(listItems);
    }
    
    Map<String,Object> configuration = getLearnerConfigurationFromAssociation( association );

    if( null != configuration )
    {
      List<String> institutionRoles = Arrays.asList( institutionRoleIds );

      if ( null != configuration.get( LEARNER_IR ) && configuration.get( LEARNER_IR ) instanceof List )
      {
        List<String> institutionRolesList = (List<String>) configuration.get( LEARNER_IR );

        // All the institution roles in the request payload must match with all the roles in the association
        if ( !checkRoleExist( institutionRolesList, institutionRoles ) )
        {
          InvalidInstitutionRoleException();
        }
      }
    }
  }

  @SuppressWarnings( "unchecked" )
  private void validateSystemRoleIds( Association association, String [] systemRoleIds ) throws ResourceForbiddenException, ResourceBadRequestException
  {
    if( null == systemRoleIds || systemRoleIds.length == 0 )
    {
      List<String> listItems = new ArrayList<>();
      listItems.add( "systemRoleIds" );
      throwValidationException(listItems);
    }
    
    Map<String,Object> configuration = getLearnerConfigurationFromAssociation( association );
    
    if( null != configuration )
    {
      List<String> systemRoles = Arrays.asList( systemRoleIds );

      if ( null != configuration.get( LEARNER_SR ) && configuration.get( LEARNER_SR ) instanceof List )
      {
        List<String> systemRolesList = (List<String>) configuration.get( LEARNER_SR );

        // All the system roles in the request payload must match with all the roles in the association
        if ( !checkRoleExist( systemRolesList, systemRoles ) )
        {
          InvalidSystemRoleException();
        }
      }
    }
  }
  
  private void InvalidInstitutionRoleException() throws ResourceForbiddenException
  {
    ExceptionSource exceptionSource = new ExceptionSource( FIELDS, INSTITUTION_ROLE_INVALID_SOURCE_FIELDS );
    throw new ResourceForbiddenException( INSTITUTION_ROLE_INVALID_CODE, exceptionSource, null,
                                          INSTITUTION_ROLE_INVALID_MESSAGE );
  }
  
  private void InvalidSystemRoleException() throws ResourceForbiddenException
  {
    ExceptionSource exceptionSource = new ExceptionSource( FIELDS, SYSTEM_ROLE_INVALID_SOURCE_FIELDS );
    throw new ResourceForbiddenException( SYSTEM_ROLE_INVALID_CODE, exceptionSource, null,
                                          SYSTEM_ROLE_INVALID_MESSAGE );
  }

  private void associationUnavailableException( String applicationId ) throws ResourceForbiddenException
  {
    ExceptionSource exceptionSource = new ExceptionSource( FIELDS, ASSOCIATION_SOURCE_FIELDS );
    throw new ResourceForbiddenException( ASSOCIATION_UNAVAILABLE_ERROR_CODE, exceptionSource, applicationId,
                                          ASSOCIATION_UNAVAILABLE_MESSAGE );
  }

  private void associationNotFoundException( String applicationId ) throws ResourceNotFoundException
  {
    ExceptionSource exceptionSource = new ExceptionSource( FIELDS, ASSOCIATION_SOURCE_FIELDS );
    throw new ResourceNotFoundException( ASSOCIATION_NOT_FOUND, exceptionSource, applicationId,
                                         ASSOCIATION_UNAVAILABLE_MESSAGE );
  }

  private void applicationPermissionException( String applicationId ) throws ResourceForbiddenException
  {
    ExceptionSource exceptionSource = new ExceptionSource( FIELDS, APPLICATION_ID );
    throw new ResourceForbiddenException( APPLICATION_PERMISSION_ERROR, exceptionSource, applicationId,
                                          ALLOWED_APPLICATIONS_ERROR_MESSAGE );
  }

  private void validateDevPortalAttributes( String devPortalKey, String devPortalSecret ) throws LmsIntegrationException
  {
    if ( StringUtils.isEmpty( devPortalKey ) || StringUtils.isEmpty( devPortalSecret ) )
    {
      ExceptionSource exceptionSource = new ExceptionSource( FIELDS, DEV_PORTAL_KEY_SECRET );
      throw new LmsIntegrationException( LMS_INTEGRATION_ERROR, exceptionSource, null, LMS_INTEGRATION_ERROR_MESSAGE );
    }
  }

  private void validatePayloadItems( CreateCourseMemebershipPayload payload ) throws ResourceBadRequestException
  {
    List<String> listItems = new ArrayList<>();
    Map<String, String> items = new HashMap<String, String>();
    items.put( LMS_TYPE, LMS_TYPE + DASH + EMPTYCHECK );
    items.put( USER_ID, USER_ID + DASH + EMPTYCHECK );
    items.put( COURSE_ID, COURSE_ID + DASH + EMPTYCHECK );
    listItems = validatePayload( payload, items );
    if ( !listItems.isEmpty() )
    {
      throwValidationException( listItems );
    }
  }

  private void validateLmsType( Object payload, String lmsType ) throws ResourceBadRequestException
  {
    List<String> listItems = new ArrayList<>();

    if ( payload instanceof CreateCourseMemebershipPayload )
    {
      lmsType = ( (CreateCourseMemebershipPayload) payload ).getLmsType();
    }

    // only Learn lmsType shall be allowed
    if ( !"learn".equalsIgnoreCase( lmsType ) )
    {
      listItems.add( LMS_TYPE );
    }
    if ( !listItems.isEmpty() )
    {
      throwValidationException( listItems );
    }
  }

  private void throwValidationException( List<String> listItems ) throws ResourceBadRequestException
  {
    String missingFields = String.join( ",", listItems );
    ExceptionSource exceptionSource = new ExceptionSource( FIELDS, missingFields );
    throw new ResourceBadRequestException( INVALID_REQUEST, exceptionSource, null, INVALID_VALUES );
  }

  /**
   * Get the host name from siteId
   * 
   * @param siteId
   * @param siteService
   * @return host name
   * @throws ResourceNotFoundException
   * @throws ProcessTemplateException
   */
  private String getLearnHostName( String siteId ) throws ResourceNotFoundException
  {
    Site site = null;
    try
    {
      site = siteService.getCompleteSite( siteId );
    }
    catch ( IOException e )
    {
      ExceptionSource exceptionSource = new ExceptionSource( FIELDS, siteId );
      throw new ResourceNotFoundException( "INVALID_SITE", exceptionSource, null, "Error when loading the site" );
    }
    String hostname = "";
    if ( null != site )
    {
      hostname = site.getSiteView().getHostUrl();
    }
    return hostname;
  }

  private Association validateAssociation( String applicationId, String siteId )
    throws ResourceForbiddenException, ResourceNotFoundException
  {
    Association association = null;
    if ( StringUtils.isNotBlank( applicationId ) && StringUtils.isNotBlank( siteId ) )
    {
      try
      {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put( "{siteId}", siteId );
        pathParams.put( "{applicationId}", applicationId );
        LOG.debug( "siteId : {} and applicationId : {}", siteId, applicationId );
        association = restAPIClient.getEntityById( PATH_GET_ASSOCIATION_BY_APPID_SITEID, Association.class,
                                                   pathParams );

        if ( null != association )
        {
          LOG.debug( "Association available {} and enabled {}", association.isAvailable(), association.isAvailable() );
          if ( !association.isAvailable() || !association.isEnabled() )
          {
            associationUnavailableException( applicationId );
          }
        }
      }
      catch ( ResourceNotFoundException e )
      {
        associationNotFoundException( applicationId );
      }
    }
    return association;
  }

  private boolean isApplicationAllowed( String lmsAllowedApplications, String applicationId )
  {
    if ( StringUtils.isNotBlank( lmsAllowedApplications ) )
    {
      String[] applications = lmsAllowedApplications.split( "," );
      for ( String app : applications )
      {
        if ( StringUtils.isNotBlank( app ) && app.trim().equalsIgnoreCase( applicationId ) )
        {
          return true;
        }
      }
    }
    return false;
  }

  private Map<String, Object> enrollment( CreateCourseMemebershipPayload payload, String baseUrl, String devPortalKey,
                                          String devPortalSecret, String fields )
    throws ResourceAlreadyExistsException, ResourceNotFoundException, ResourceBadRequestException, BadGatewayException,
    LmsIntegrationException, LearnRestException, RestExternalClientException
  {
    Map<String, Object> courseMembership = new HashMap<>();
    String oauth2Token = LearnRestClient.generateToken( devPortalKey, devPortalSecret, baseUrl );

    if ( ObjectUtils.isEmpty( oauth2Token ) )
    {
      LOG.error( "Could not create course membership due to invalid token for the siteId {} ", payload.getSiteId() );
      ExceptionSource exceptionSource = new ExceptionSource( FIELDS, BEARER_TOKEN );
      throw new BadGatewayException( LEARN_BEARER_ERROR, exceptionSource, null, LEARN_BEARER_ERROR_MESSAGE );
    }
    else
    {
      courseMembership = LearnRestClient.createCourseMembership( baseUrl, oauth2Token, payload, fields );
    }
    return courseMembership;
  }

  private Integer getMembershipCount( LmsIntegration lmsIntegration, String baseUrl )
    throws JsonMappingException, JsonProcessingException, ResourceAlreadyExistsException, ResourceNotFoundException,
    ResourceBadRequestException, BadGatewayException, LmsIntegrationException, LearnRestException,
    GenericServiceException, RestExternalClientException
  {
    Integer courseMembershipCount = 0;
    String oauth2Token = LearnRestClient.generateToken( lmsIntegration.getDevPortalKey(),
                                                        lmsIntegration.getDevPortalSecret(), baseUrl );
    List<Map<String, Object>> membershipResultSet = new ArrayList<>();

    if ( ObjectUtils.isEmpty( oauth2Token ) )
    {
      LOG.error( "Could not load course membership due to invalid token for the siteId {} ",
                 lmsIntegration.getSiteId() );
      ExceptionSource exceptionSource = new ExceptionSource( FIELDS, BEARER_TOKEN );
      throw new BadGatewayException( LEARN_BEARER_ERROR, exceptionSource, null, LEARN_BEARER_ERROR_MESSAGE );
    }
    else
    {
      courseMembershipCount = LearnRestClient.getMembershipCount( baseUrl, oauth2Token, courseMembershipCount,
                                                                  lmsIntegration, "", membershipResultSet );
    }
    return courseMembershipCount;
  }

  private Map<String, Object> createUserREST( CreateUserPayload payload, String baseUrl, String devPortalKey,
                                              String devPortalSecret, MultivaluedMap<String, String> queryParams,
                                              String siteId )
    throws ResourceAlreadyExistsException, ResourceNotFoundException, ResourceBadRequestException, BadGatewayException,
    LmsIntegrationException, LearnRestException, RestExternalClientException
  {
    Map<String, Object> createUserResponse = new HashMap<>();

    String oauth2Token = LearnRestClient.generateToken( devPortalKey, devPortalSecret, baseUrl );

    if ( ObjectUtils.isEmpty( oauth2Token ) )
    {
      LOG.error( "Could not create user due to invalid token for the siteId {} ", siteId );
      ExceptionSource exceptionSource = new ExceptionSource( FIELDS, BEARER_TOKEN );
      throw new BadGatewayException( LEARN_BEARER_ERROR, exceptionSource, null, LEARN_BEARER_ERROR_MESSAGE );
    }
    else
    {
      createUserResponse = LearnRestClient.createUser( baseUrl, oauth2Token, payload, queryParams );
    }
    return createUserResponse;
  }

  private Integer checkLearnUserExists( String baseUrl, String devPortalKey, String devPortalSecret,
                                                     String siteId, String userName )
    throws ResourceAlreadyExistsException, ResourceNotFoundException, ResourceBadRequestException, BadGatewayException,
    LmsIntegrationException, LearnRestException, RestExternalClientException
  {
    Integer userExistsResponseCode;

    String oauth2Token = LearnRestClient.generateToken( devPortalKey, devPortalSecret, baseUrl );

    if ( ObjectUtils.isEmpty( oauth2Token ) )
    {
      LOG.error( "Could not check user exists in Learn due to invalid token for the siteId {} ", siteId );
      ExceptionSource exceptionSource = new ExceptionSource( FIELDS, BEARER_TOKEN );
      throw new BadGatewayException( LEARN_BEARER_ERROR, exceptionSource, null, LEARN_BEARER_ERROR_MESSAGE );
    }
    else
    {
      userExistsResponseCode = LearnRestClient.checkUserExists( baseUrl, oauth2Token, userName );
    }
    return userExistsResponseCode;
  }

  /**
   * Get the status code from Learn REST API error response
   * 
   * @param response
   * @return HttpStatus code as integer
   * @throws JsonMappingException
   * @throws JsonProcessingException
   */
  public Integer getStatusCode( String response ) throws JsonMappingException, JsonProcessingException
  {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> map = objectMapper.readValue( response, new TypeReference<Map<String, Object>>()
      {
      } );

    int status = (int) map.get( STATUS );
    return status;
  }

}
