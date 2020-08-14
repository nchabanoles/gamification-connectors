# eXo Add-on:: eXo Gamification Connectors

## Github connector


## Slack connector

### Slack Bot Setup
1. Create Slack Bot
2. OAuth Setup
    1. Redirect URL: `https://{MEEDS_HOST}/portal/rest/gamification/connectors/slack/oauth`
    2. Scope permissions
        1. app_mentions:read
        2. channels:history
        3. channels:read
        4. chat:write
        5. commands
        6. groups:read
        7. im:read
        8. im:write
        9. mpim:read
        10. pins:read
        11. reactions:read
        12. users:read

3. Event Subscriptions
   1. Request URL: `https://{MEEDS_HOST}/portal/rest/gamification/connectors/slack/events`
   2. Subscribe to bot events (optional)
      1. app_mentions
      2. message.channels
      3. pin_added
      4. reaction_added
      5. team_join

4. Create Slash commands

| Command           | Description                             | Usage Hint                   |
|-------------------|-----------------------------------------|------------------------------|
| /meeds_hello      | Hello                                   |                              |
| /meeds_sign_in    | Signin to Meeds                         |                              |
| /meeds_whoami     | Identify who is asking                  |                              |
| /meeds_clear      | Clear stored Slack authentication token |                              |
| /meeds_kudos      | Get Kudos statistics                    |                              |
| /meeds_send_kudos | Send kudos to another user              | {KUDOS} {RECEIVER} [MESSAGE] |

Request URL: `https://{MEEDS_HOST}/portal/rest/gamification/connectors/slack/commands`
 
### Meeds config

Update gatein/conf/exo.properties file with Slack token and secret keys from Slack bot:

```
# Slack
gamification.connectors.slack.userAuthUrl=https://slack.com/oauth/v2/authorize?user_scope=identity.basic,identity.email&client_id={CLIENT_ID}
gamification.connectors.slack.token={BOT_TOKEN}
gamification.connectors.slack.signingSecret={SIGNING_SECRET}
gamification.connectors.slack.clientId={CLIENT_ID}
gamification.connectors.slack.clientSecret={CLIENT_SECRET}

```

The `{BOT_TOKEN}` can be obtained after install Slack bot to Slack workspace.
When install Slack bot you will be redirected to Meeds OAuth URL `https://{MEEDS_HOST}/portal/rest/gamification/connectors/slack/oauth` and `{BOT_TOKEN}` will display in the response and from tomcat log also.


### Usage
- The `/meeds_hello` can be used to check the connection between Slack and Meeds.
- Other commands will needs authenticated first. Invoke `/meeds_sign_in` to get the signin url which is configured as `gamification.connectors.slack.userAuthUrl`.
- Send `/meeds_whoami` to verify your Slack and Meeds identity.
- Send `/meeds_clear` to clear authenticated token from Meeds's database
- Send `/meeds_kudos` to get Kudos statistics with sent, received and remaining kudos points in the current period
- Send `/meeds_send_kudos {KUDOS} {RECEIVER} [MESSAGE]` to send kudos to another user. If the `[MESSAGE]` was opmitted the default message `Sent from Slack Bot` will be used.