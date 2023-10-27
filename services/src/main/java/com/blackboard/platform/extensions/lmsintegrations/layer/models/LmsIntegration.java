package com.blackboard.platform.extensions.lmsintegrations.layer.models;

import java.util.Map;

public class LmsIntegration
{

  private String siteId;
  
  private String applicationId;
  
  private String devPortalKey;
  
  private String devPortalSecret;
  
  private String bodyPayload;
  
  private String lmsAllowedApplications;
  
  private String courseId;

  private Map<String,String> queryParam;
  
  /**
   * @return the siteId
   */
  public String getSiteId()
  {
    return siteId;
  }

  /**
   * @param siteId the siteId to set
   */
  public void setSiteId( String siteId )
  {
    this.siteId = siteId;
  }

  /**
   * @return the applicationId
   */
  public String getApplicationId()
  {
    return applicationId;
  }

  /**
   * @param applicationId the applicationId to set
   */
  public void setApplicationId( String applicationId )
  {
    this.applicationId = applicationId;
  }

  /**
   * @return the devPortalKey
   */
  public String getDevPortalKey()
  {
    return devPortalKey;
  }

  /**
   * @param devPortalKey the devPortalKey to set
   */
  public void setDevPortalKey( String devPortalKey )
  {
    this.devPortalKey = devPortalKey;
  }

  /**
   * @return the devPortalSecret
   */
  public String getDevPortalSecret()
  {
    return devPortalSecret;
  }

  /**
   * @param devPortalSecret the devPortalSecret to set
   */
  public void setDevPortalSecret( String devPortalSecret )
  {
    this.devPortalSecret = devPortalSecret;
  }

  /**
   * @return the bodyPayload
   */
  public String getBodyPayload()
  {
    return bodyPayload;
  }

  /**
   * @param bodyPayload the bodyPayload to set
   */
  public void setBodyPayload( String bodyPayload )
  {
    this.bodyPayload = bodyPayload;
  }

  /**
   * @return the lmsAllowedApplications
   */
  public String getLmsAllowedApplications()
  {
    return lmsAllowedApplications;
  }

  /**
   * @param lmsAllowedApplications the lmsAllowedApplications to set
   */
  public void setLmsAllowedApplications( String lmsAllowedApplications )
  {
    this.lmsAllowedApplications = lmsAllowedApplications;
  }

  /**
   * @return the courseId
   */
  public String getCourseId()
  {
    return courseId;
  }

  /**
   * @param courseId the courseId to set
   */
  public void setCourseId( String courseId )
  {
    this.courseId = courseId;
  }

  /**
   * @return the queryParam
   */
  public Map<String, String> getQueryParam()
  {
    return queryParam;
  }

  /**
   * @param queryParam the queryParam to set
   */
  public void setQueryParam( Map<String, String> queryParam )
  {
    this.queryParam = queryParam;
  }
  
}
