package com.blackboard.platform.extensions.lmsintegrations.layer.fixtures;

import com.blackboard.platform.extensions.lmsintegrations.layer.models.CreateCourseMemebershipPayload;
import com.blackboard.platform.extensions.lmsintegrations.layer.models.CreateUserPayload;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.util.Map;

public class LmsIntegrationsFixture {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public CreateCourseMemebershipPayload getCourseMembershipPayload() throws Exception {
		MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		MAPPER.registerModule(new JodaModule());
		CreateCourseMemebershipPayload result = MAPPER
				.readValue(getClass().getClassLoader().getResource("fixtures/createCourseMembership.json"), CreateCourseMemebershipPayload.class);
		return result;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getCourseMembershipData() throws Exception {
		MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		MAPPER.registerModule(new JodaModule());
		Map<String, Object> result = MAPPER.readValue(
				getClass().getClassLoader().getResource("fixtures/courseMembershipResponse.json"), Map.class);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> postCourseMembershipData() throws Exception {
		MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		MAPPER.registerModule(new JodaModule());
		Map<String, Object> result = MAPPER.readValue(
				getClass().getClassLoader().getResource("fixtures/createCourseMembershipLearn.json"), Map.class);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	  public Map<String,Object> getCourseMembershipCount() throws Exception
	  {
	    MAPPER.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
	    MAPPER.registerModule( new JodaModule() );
	    Map<String,Object> result = MAPPER.readValue( getClass().getClassLoader().getResource( "fixtures/courseMembershipCountResponse.json" ), Map.class );
	    return result;
	  }
	
	public CreateUserPayload getUserPayload() throws Exception {
      MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      MAPPER.registerModule(new JodaModule());
      CreateUserPayload result = MAPPER
              .readValue(getClass().getClassLoader().getResource("fixtures/createUser.json"), CreateUserPayload.class);
      return result;
    }
	
	@SuppressWarnings( "unchecked" )
	  public Map<String,Object> getUserData() throws Exception
	  {
	    MAPPER.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
	    MAPPER.registerModule( new JodaModule() );
	    Map<String,Object> result = MAPPER.readValue( getClass().getClassLoader().getResource( "fixtures/userResponse.json" ), Map.class );
	    return result;
	  }
}
