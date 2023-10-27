package com.blackboard.platform.extensions.lmsintegrations.layer.db;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.blackboard.platform.extensions.restapi.db.AbstractDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LmsIntegrationsDAO extends AbstractDAO
{

  private static final Logger LOG = LoggerFactory.getLogger( LmsIntegrationsDAO.class );
  private final String tableName;
  
  public LmsIntegrationsDAO( DynamoDB dynamoDB, String tableName )
  {
    this.dynamoDB = dynamoDB;
    this.tableName = tableName;
  }

}
