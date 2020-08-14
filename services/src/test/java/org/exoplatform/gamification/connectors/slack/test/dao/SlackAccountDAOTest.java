package org.exoplatform.gamification.connectors.slack.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.exoplatform.gamification.connectors.slack.dao.SlackAccountDAO;
import org.exoplatform.gamification.connectors.slack.entity.SlackAccountEntity;
import org.exoplatform.gamification.connectors.slack.test.BaseSlackConnectorsTest;

public class SlackAccountDAOTest extends BaseSlackConnectorsTest {

  protected String slackId = "slack_id";
  protected String slackTeamId = "slack_team";
  protected Long identityId = 1l;
  protected String email = "root@test.example.com";
  protected String token = "xoxp-xxx";

  private SlackAccountEntity newSlackAccount() {
    return newSlackAccountEntity(identityId, slackTeamId, slackId, email, token);
  }

  /**
   * Check that service is instantiated and functional
   */
  @Test
  public void testServiceInstantiated() {
    SlackAccountDAO slackAccountDAO = getService(SlackAccountDAO.class);
    assertNotNull(slackAccountDAO);
  }

  @Test
  public void getAccountByIdentityId() {
    SlackAccountDAO slackAccountDAO = getService(SlackAccountDAO.class);
    SlackAccountEntity entity = slackAccountDAO.getAccountByIdentityId(identityId);
    assertEquals(null, entity);
    newSlackAccount();
    entity = slackAccountDAO.getAccountByIdentityId(identityId);
    assertNotNull(entity);
  }

  @Test
  public void getAccountBySlackId() {
    SlackAccountDAO slackAccountDAO = getService(SlackAccountDAO.class);
    SlackAccountEntity entity = slackAccountDAO.getAccountBySlackId(slackId);
    assertEquals(null, entity);
    newSlackAccount();
    entity = slackAccountDAO.getAccountBySlackId(slackId);
    assertNotNull(entity);
  }

  @Test
  public void getAccountByToken() {
    SlackAccountDAO slackAccountDAO = getService(SlackAccountDAO.class);
    SlackAccountEntity entity = slackAccountDAO.getAccountByToken(token);
    assertEquals(null, entity);
    newSlackAccount();
    entity = slackAccountDAO.getAccountByToken(token);
    assertNotNull(entity);
  }

}
