package com.blackboard.platform.extensions.lmsintegrations.layer.exceptions;

public class LearnRestException extends Exception
{

  private static final long serialVersionUID = 2494765544145562589L;

  private String body;

  public LearnRestException( String body )
  {
    setBody( body );
  }

  /**
   * @return the body
   */
  public String getBody()
  {
    return body;
  }

  /**
   * @param body the body to set
   */
  public void setBody( String body )
  {
    this.body = body;
  }

  public String toString()
  {
    return body;
  }

}
