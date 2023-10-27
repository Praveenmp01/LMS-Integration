package com.blackboard.platform.extensions.lmsintegrations.layer.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties( ignoreUnknown = true )
public class CreateUserPayload
{

  private String externalId;
  
  private String id;
  
  private String uuid;
  
  private String dataSourceId;
  
  private String userName;
  
  private String studentId;
  
  private String password;
  
  private String educationLevel;
  
  private String gender;
  
  private String pronouns;
  
  private String birthDate;
  
  private String [] institutionRoleIds;
  
  private String [] systemRoleIds;
  
  private Map<String,Object> availability;
  
  private Map<String,Object> name;
  
  private Map<String,Object> job;
  
  private Map<String,Object> contact;
  
  private Map<String,Object> address;
  
  private Map<String,Object> locale;
  
  private Map<String,Object> avatar;
  
  private String pronunciation;
  
  private Map<String,Object> pronunciationAudio;
  
  
  /**
   * @return the id
   */
  public String getId()
  {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId( String id )
  {
    this.id = id;
  }

  /**
   * @return the uuid
   */
  public String getUuid()
  {
    return uuid;
  }

  /**
   * @param uuid the uuid to set
   */
  public void setUuid( String uuid )
  {
    this.uuid = uuid;
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
   * @return the externalId
   */
  public String getExternalId()
  {
    return externalId;
  }

  /**
   * @param externalId the externalId to set
   */
  public void setExternalId( String externalId )
  {
    this.externalId = externalId;
  }

  /**
   * @return the userName
   */
  public String getUserName()
  {
    return userName;
  }

  /**
   * @param userName the userName to set
   */
  public void setUserName( String userName )
  {
    this.userName = userName;
  }

  /**
   * @return the studentId
   */
  public String getStudentId()
  {
    return studentId;
  }

  /**
   * @param studentId the studentId to set
   */
  public void setStudentId( String studentId )
  {
    this.studentId = studentId;
  }

  /**
   * @return the password
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword( String password )
  {
    this.password = password;
  }

  /**
   * @return the educationLevel
   */
  public String getEducationLevel()
  {
    return educationLevel;
  }

  /**
   * @param educationLevel the educationLevel to set
   */
  public void setEducationLevel( String educationLevel )
  {
    this.educationLevel = educationLevel;
  }

  /**
   * @return the gender
   */
  public String getGender()
  {
    return gender;
  }

  /**
   * @param gender the gender to set
   */
  public void setGender( String gender )
  {
    this.gender = gender;
  }

  /**
   * @return the pronouns
   */
  public String getPronouns()
  {
    return pronouns;
  }

  /**
   * @param pronouns the pronouns to set
   */
  public void setPronouns( String pronouns )
  {
    this.pronouns = pronouns;
  }

  /**
   * @return the birthDate
   */
  public String getBirthDate()
  {
    return birthDate;
  }

  /**
   * @param birthDate the birthDate to set
   */
  public void setBirthDate( String birthDate )
  {
    this.birthDate = birthDate;
  }

  /**
   * @return the institutionRoleIds
   */
  public String[] getInstitutionRoleIds()
  {
    return institutionRoleIds;
  }

  /**
   * @param institutionRoleIds the institutionRoleIds to set
   */
  public void setInstitutionRoleIds( String[] institutionRoleIds )
  {
    this.institutionRoleIds = institutionRoleIds;
  }

  /**
   * @return the systemRoleIds
   */
  public String[] getSystemRoleIds()
  {
    return systemRoleIds;
  }

  /**
   * @param systemRoleIds the systemRoleIds to set
   */
  public void setSystemRoleIds( String[] systemRoleIds )
  {
    this.systemRoleIds = systemRoleIds;
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
   * @return the name
   */
  public Map<String, Object> getName()
  {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName( Map<String, Object> name )
  {
    this.name = name;
  }

  /**
   * @return the job
   */
  public Map<String, Object> getJob()
  {
    return job;
  }

  /**
   * @param job the job to set
   */
  public void setJob( Map<String, Object> job )
  {
    this.job = job;
  }

  /**
   * @return the contact
   */
  public Map<String, Object> getContact()
  {
    return contact;
  }

  /**
   * @param contact the contact to set
   */
  public void setContact( Map<String, Object> contact )
  {
    this.contact = contact;
  }

  /**
   * @return the address
   */
  public Map<String, Object> getAddress()
  {
    return address;
  }

  /**
   * @param address the address to set
   */
  public void setAddress( Map<String, Object> address )
  {
    this.address = address;
  }

  /**
   * @return the locale
   */
  public Map<String, Object> getLocale()
  {
    return locale;
  }

  /**
   * @param locale the locale to set
   */
  public void setLocale( Map<String, Object> locale )
  {
    this.locale = locale;
  }

  /**
   * @return the avatar
   */
  public Map<String, Object> getAvatar()
  {
    return avatar;
  }

  /**
   * @param avatar the avatar to set
   */
  public void setAvatar( Map<String, Object> avatar )
  {
    this.avatar = avatar;
  }

  /**
   * @return the pronunciation
   */
  public String getPronunciation()
  {
    return pronunciation;
  }

  /**
   * @param pronunciation the pronunciation to set
   */
  public void setPronunciation( String pronunciation )
  {
    this.pronunciation = pronunciation;
  }

  /**
   * @return the pronunciationAudio
   */
  public Map<String, Object> getPronunciationAudio()
  {
    return pronunciationAudio;
  }

  /**
   * @param pronunciationAudio the pronunciationAudio to set
   */
  public void setPronunciationAudio( Map<String, Object> pronunciationAudio )
  {
    this.pronunciationAudio = pronunciationAudio;
  }

  
}
