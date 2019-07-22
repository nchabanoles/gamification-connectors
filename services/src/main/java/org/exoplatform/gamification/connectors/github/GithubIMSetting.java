package org.exoplatform.gamification.connectors.github;

import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.social.core.profile.settings.IMType;
import org.exoplatform.social.core.profile.settings.UserProfileSettingsService;
import org.picocontainer.Startable;

public class GithubIMSetting implements Startable {

    public static final String  GITHUB_TYPE  = "github";

    public static final String  GITHUB_TITLE   = "Github";

    private static UserProfileSettingsService profileSettings;

    /**
     * Instantiates a new github IM provider.
     *
     * @param profileSettings the profile settings Service
     * @throws ConfigurationException the configuration exception
     */
    public GithubIMSetting(UserProfileSettingsService profileSettings) throws ConfigurationException {
        this.profileSettings=profileSettings;
    }

    @Override
    public void start() {
        if (profileSettings != null) {
            profileSettings.addIMType(new IMType(GITHUB_TYPE, GITHUB_TITLE));
        }
    }

    @Override
    public void stop() {

    }
}



