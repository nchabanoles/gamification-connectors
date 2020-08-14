package org.exoplatform.gamification.connectors.slack.dao;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.gamification.connectors.slack.entity.SlackAccountEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class SlackAccountDAO extends GenericDAOJPAImpl<SlackAccountEntity, Long> {

    private static final Log LOG = ExoLogger.getLogger(SlackAccountDAO.class);
  
    public SlackAccountEntity getAccountBySlackId(String slackId) {
  
      TypedQuery<SlackAccountEntity> query = getEntityManager()
                                                                .createNamedQuery("SlackAccountEntity.getAccountBySlackId",
                                                                                  SlackAccountEntity.class)
                                                                .setParameter("slackId", slackId);
  
      try {
        return query.getSingleResult();
      } catch (NoResultException e) {
        return null;
      } catch (Exception e) {
        LOG.error("Error occurred when trying to get Slack account for the Id {}", slackId, e);
        return null;
      }
    }
  
    public SlackAccountEntity getAccountByIdentityId(Long identityId) {
  
      TypedQuery<SlackAccountEntity> query = getEntityManager()
                                                                .createNamedQuery("SlackAccountEntity.getAccountByIdentityId",
                                                                                  SlackAccountEntity.class)
                                                                .setParameter("identityId", identityId);
  
      try {
        return query.getSingleResult();
      } catch (NoResultException e) {
        return null;
      } catch (Exception e) {
        LOG.error("Error occurred when trying to get Slack account for the identity Id {}", identityId, e);
        return null;
      }
    }
  
    public SlackAccountEntity getAccountByEmail(String email) {
  
      TypedQuery<SlackAccountEntity> query = getEntityManager()
                                                                .createNamedQuery("SlackAccountEntity.getAccountByEmail",
                                                                                  SlackAccountEntity.class)
                                                                .setParameter("email", email);
  
      try {
        return query.getSingleResult();
      } catch (NoResultException e) {
        return null;
      } catch (Exception e) {
        LOG.error("Error occurred when trying to get Slack account for the user {}", email, e);
        return null;
      }
    }

    public SlackAccountEntity getAccountByToken(String token) {
  
      TypedQuery<SlackAccountEntity> query = getEntityManager()
                                                                .createNamedQuery("SlackAccountEntity.getAccountByToken",
                                                                                  SlackAccountEntity.class)
                                                                .setParameter("token", token);
  
      try {
        return query.getSingleResult();
      } catch (NoResultException e) {
        return null;
      } catch (Exception e) {
        LOG.error("Error occurred when trying to get Slack account for the user {}", token, e);
        return null;
      }
    }
  }