package org.exoplatform.gamification.connectors.github.dao;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.gamification.connectors.github.entity.GitHubAccountEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import java.util.List;

public class GitHubAccountDAO extends GenericDAOJPAImpl<GitHubAccountEntity, Long> {

    private static final Log LOG                   = ExoLogger.getLogger(GitHubAccountDAO.class);

    public GitHubAccountEntity getAccountByGithubId(String gitHubId){

        TypedQuery<GitHubAccountEntity> query = getEntityManager().createNamedQuery("GitHubAccountEntity.getAccountByGithubId", GitHubAccountEntity.class)
                .setParameter("gitHubId", gitHubId);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }catch (Exception e){
            LOG.error("Error occurred when trying to get Github account for the Id {}",gitHubId,e);
            return null;
        }
    }
    public GitHubAccountEntity getAccountByUserName(String userName) {

        TypedQuery<GitHubAccountEntity> query = getEntityManager().createNamedQuery("GitHubAccountEntity.getAccountByUserName", GitHubAccountEntity.class)
                .setParameter("userName", userName);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }catch (Exception e){
            LOG.error("Error occurred when trying to get Github account for the user {}",userName,e);
            return null;
        }
    }
}
