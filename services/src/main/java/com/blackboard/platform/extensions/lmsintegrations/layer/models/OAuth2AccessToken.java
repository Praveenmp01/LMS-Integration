package com.blackboard.platform.extensions.lmsintegrations.layer.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties( ignoreUnknown = true )
public class OAuth2AccessToken
{

  @JsonProperty( "access_token" )
  private String access_token;

  @JsonProperty( "token_type" )
  private String token_type;

  @JsonProperty( "expires_in" )
  private int expires_in;

  @JsonProperty( "refresh_token" )
  private String refresh_token;

  @JsonProperty( "scope" )
  private String scope;

  @JsonProperty( "user_id" )
  private String user_id;

  public String getAccesstoken()
  {
    return access_token;
  }

  public void setAccesstoken( String access_token )
  {
    this.access_token = access_token;
  }

  public String getTokentype()
  {
    return token_type;
  }

  public void setTokentype( String token_type )
  {
    this.token_type = token_type;
  }

  public int getExpiresin()
  {
    return expires_in;
  }

  public void setExpiresin( int expires_in )
  {
    this.expires_in = expires_in;
  }

  public String getRefreshToken()
  {
    return refresh_token;
  }

  public void setRefreshToken( String refresh_token )
  {
    this.refresh_token = refresh_token;
  }

  public String getScope()
  {
    return scope;
  }

  public void setScope( String scope )
  {
    this.scope = scope;
  }

  public String getUserId()
  {
    return user_id;
  }

  public void setUserId( String user_id )
  {
    this.user_id = user_id;
  }
}
