package com.blackboard.platform.extensions.lmsintegrations.layer.models;

import java.util.Map;

public class CreateCourseMemebershipPayload
{

  private String applicationId;
  
  private String siteId;
  
  private String courseId;
  
  private String userId;
  
  private String lmsType;
  
  private String childCourseId;
  
  private String dataSourceId;
  
  private Map<String,Object> availability;
  
  private String courseRoleId;
  

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
   * @return the userId
   */
  public String getUserId()
  {
    return userId;
  }

  /**
   * @param userId the userId to set
   */
  public void setUserId( String userId )
  {
    this.userId = userId;
  }

  /**
   * @return the lmsType
   */
  public String getLmsType()
  {
    return lmsType;
  }

  /**
   * @param lmsType the lmsType to set
   */
  public void setLmsType( String lmsType )
  {
    this.lmsType = lmsType;
  }

  /**
   * @return the childCourseId
   */
  public String getChildCourseId()
  {
    return childCourseId;
  }

  /**
   * @param childCourseId the childCourseId to set
   */
  public void setChildCourseId( String childCourseId )
  {
    this.childCourseId = childCourseId;
  }

  /**
   * @return the dataSourceId
   */
  public String getDataSourceId()
  {
    return dataSourceId;
  }

  /**
   * @param dataSourceId the dataSourceId to set
   */
  public void setDataSourceId( String dataSourceId )
  {
    this.dataSourceId = dataSourceId;
  }

  /**
   * @return the availability
   */
  public Map<String, Object> getAvailability()
  {
    return availability;
  }

  /**
   * @param availability the availability to set
   */
  public void setAvailability( Map<String, Object> availability )
  {
    this.availability = availability;
  }

  /**
   * @return the courseRoleId
   */
  public String getCourseRoleId()
  {
    return courseRoleId;
  }

  /**
   * @param courseRoleId the courseRoleId to set
   */
  public void setCourseRoleId( String courseRoleId )
  {
    this.courseRoleId = courseRoleId;
  }
  
}
