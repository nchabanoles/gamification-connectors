package org.exoplatform.gamification.connectors.github.dao;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.gamification.connectors.github.entity.GitHubHookEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class GitHubHookDAO extends GenericDAOJPAImpl<GitHubHookEntity, Long> {

  private static final Log LOG = ExoLogger.getLogger(GitHubHookDAO.class);

}
