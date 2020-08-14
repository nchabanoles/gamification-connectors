package org.exoplatform.gamification.connectors.slack.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.gamification.connectors.slack.dao.SlackAccountDAO;
import org.exoplatform.gamification.connectors.slack.entity.SlackAccountEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class BaseSlackConnectorsTest {

  protected static PortalContainer container;

  protected List<Serializable>     entitiesToClean = new ArrayList<>();

  @BeforeClass
  public static void beforeTest() {
    RootContainer rootContainer = RootContainer.getInstance();
    container = rootContainer.getPortalContainer("portal");
    assertNotNull("Container shouldn't be null", container);
    assertTrue("Container should have been started", container.isStarted());
  }

  @Before
  public void beforeMethodTest() {
    RequestLifeCycle.begin(container);
  }

  @After
  public void afterMethodTest() {
    SlackAccountDAO slackAccountDAO = getService(SlackAccountDAO.class);

    RequestLifeCycle.end();
    RequestLifeCycle.begin(container);

    if (!entitiesToClean.isEmpty()) {
      for (Serializable entity : entitiesToClean) {
        if (entity instanceof SlackAccountEntity) {
          slackAccountDAO.delete((SlackAccountEntity) entity);
        } else {
          throw new IllegalStateException("Entity not managed" + entity);
        }
      }
    }

    RequestLifeCycle.end();
  }

  protected <T> T getService(Class<T> componentType) {
    return container.getComponentInstanceOfType(componentType);
  }

  protected SlackAccountEntity newSlackAccountEntity(Long identityId, String slackTeamId, String slackId, String email, String token) {
    SlackAccountDAO slackAccountDAO = getService(SlackAccountDAO.class);
    SlackAccountEntity slackAccountEntity = new SlackAccountEntity();
    slackAccountEntity.setIdentityId(identityId);
    slackAccountEntity.setSlackId(slackId);
    slackAccountEntity.setSlackTeamId(slackTeamId);
    slackAccountEntity.setEmail(email);
    slackAccountEntity.setToken(token);
    slackAccountEntity = slackAccountDAO.create(slackAccountEntity);
    entitiesToClean.add(slackAccountEntity);
    return slackAccountEntity;
  }

}
