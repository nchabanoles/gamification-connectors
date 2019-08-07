package org.exoplatform.gamification.connectors.github.listener;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.gamification.connectors.github.dao.GitHubAccountDAO;
import org.exoplatform.gamification.connectors.github.entity.GitHubAccountEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GithubProfileListener  extends ProfileListenerPlugin {

    private static final Log LOG = ExoLogger.getLogger(GithubProfileListener.class);

    protected GitHubAccountDAO gitHubAccountDAO;

    public static final String  GITHUB_TYPE  = "github";

    public GithubProfileListener(GitHubAccountDAO gitHubAccountDAO) {
        this.gitHubAccountDAO = gitHubAccountDAO;
    }

    @Override
    public void avatarUpdated(ProfileLifeCycleEvent event) {

    }

    @Override
    public void bannerUpdated(ProfileLifeCycleEvent event) {

    }

    @Override
    public void basicInfoUpdated(ProfileLifeCycleEvent event) {
    }

    @Override
    public void contactSectionUpdated(ProfileLifeCycleEvent event) {
        String gitHubId="";
        List<HashMap<String, String>> ims = (List<HashMap<String, String>>) event.getProfile().getProperty("ims");
        for(HashMap<String, String> map : ims){
            if(map.get("key").equals(GITHUB_TYPE)){
                gitHubId=map.get("value");
            }
        }
        if(!gitHubId.equals("")){
            GitHubAccountEntity entity = gitHubAccountDAO.getAccountByUserName(event.getUsername());
            if(entity==null ||!entity.getGitHubId().equals(gitHubId)){
                GitHubAccountEntity  existingEntity = gitHubAccountDAO.getAccountByGithubId(gitHubId);
                if(existingEntity==null){
                    if(entity==null){
                        entity=new GitHubAccountEntity();
                        entity.setUserName(event.getUsername());
                        entity.setGitHubId(gitHubId);
                        gitHubAccountDAO.create(entity);
                    } else{
                        entity.setGitHubId(gitHubId);
                        gitHubAccountDAO.update(entity);
                    }

                }else{
                    LOG.warn("The provided Github ID {} is already used by {}",gitHubId,existingEntity.getUserName());
                }
            }
        }
    }

    @Override
    public void experienceSectionUpdated(ProfileLifeCycleEvent event) {

    }

    @Override
    public void headerSectionUpdated(ProfileLifeCycleEvent event) {

    }

    @Override
    public void createProfile(ProfileLifeCycleEvent event) {

    }


    @Override
    public void aboutMeUpdated(ProfileLifeCycleEvent event) {

    }


}
