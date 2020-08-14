package org.exoplatform.gamification.connectors.slack.rest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.oauth.OAuthV2AccessResponse;
import com.slack.api.methods.response.users.UsersIdentityResponse;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.gamification.connectors.common.Utils;
import org.exoplatform.gamification.connectors.slack.dao.SlackAccountDAO;
import org.exoplatform.gamification.connectors.slack.entity.SlackAccountEntity;
import org.exoplatform.kudos.model.AccountSettings;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.model.KudosEntityType;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.RestChecker;
import org.json.JSONObject;

@Path("/gamification/connectors/slack/")
@Produces(MediaType.APPLICATION_JSON)

public class SlackWebHookRest implements ResourceContainer {

  private final Log LOG = ExoLogger.getLogger(SlackWebHookRest.class);
  private final String[] SUPPORTED_FORMATS = new String[] { "json", "application/json" };

  private final String SLACK_USER_AUTH_URL = System.getProperty("gamification.connectors.slack.userAuthUrl");
  private final String SLACK_CLIENT_ID = System.getProperty("gamification.connectors.slack.clientId");
  private final String SLACK_REDIRECT_URI = System.getProperty("gamification.connectors.slack.redirectUri");
  private final String SLACK_SIGNING_SECRET = System.getProperty("gamification.connectors.slack.signingSecret");
  private final String SLACK_CLIENT_SECRET = System.getProperty("gamification.connectors.slack.clientSecret");
  private String SLACK_BOT_TOKEN = System.getProperty("gamification.connectors.slack.botToken");

  private Slack slack;
  private MethodsClient methods;

  private final ExoContainer container;

  private final IdentityManager identityManager;

  public SlackWebHookRest(final ExoContainer container) {
    this.container = container;
    this.identityManager = container.getComponentInstanceOfType(IdentityManager.class);

    if (this.SLACK_CLIENT_SECRET == null || this.SLACK_BOT_TOKEN == null) {
      LOG.warn("SLACK NOT CONFIGURED");
    } else {
      initSlackClient();
      LOG.warn("SLACK BOT INITIALIZED");
    }
  }

  private void initSlackClient() {
    this.slack = Slack.getInstance();
    this.methods = slack.methods(this.SLACK_BOT_TOKEN);

  }

  protected <T> T getServiceOfType(Class<T> componentType) {
    return container.getComponentInstanceOfType(componentType);
  }

  /**
   * Slack event hook
   * @param obj
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("events")
  public Response slackEvent(final String obj) {

    try {
      final ObjectMapper objectMapper = new ObjectMapper();
      final JsonNode infoNode = objectMapper.readTree(obj);

      LOG.warn("RECEIVED EVENT: " + obj);
      final MediaType mediaType = RestChecker.checkSupportedFormat("json", SUPPORTED_FORMATS);

      // Response for slack challenge request event
      if (infoNode.has("challenge")) {
        LOG.info("Challenge requesting");
        final JSONObject challenge = new JSONObject();
        challenge.put("challenge", infoNode.get("challenge").textValue());
        return Response.ok(challenge.toString().getBytes(StandardCharsets.UTF_8), mediaType).build();
      }

      return Response.ok().build();
    } catch (final Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  /**
   * Slack slash command hook
   */
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("commands")
  public Response slackCommand(@FormParam("token") final String token, @FormParam("team_id") final String team_id,
      @FormParam("team_domain") final String team_domain, @FormParam("enterprise_id") final String enterprise_id,
      @FormParam("enterprise_name") final String enterprise_name, @FormParam("channel_id") final String channel_id,
      @FormParam("channel_name") final String channel_name, @FormParam("user_id") final String user_id,
      @FormParam("user_name") final String user_name, @FormParam("command") final String command,
      @FormParam("text") final String text, @FormParam("response_url") final String response_url,
      @FormParam("trigger_id") final String trigger_id) {

    try {
      LOG.warn("RECEIVED Command: " + command);
      LOG.info("token: " + token);
      LOG.info("team_id: " + team_id);
      LOG.info("team_domain: " + team_domain);
      LOG.info("enterprise_id: " + enterprise_id);
      LOG.info("enterprise_name: " + enterprise_name);
      LOG.info("channel_id: " + channel_id);
      LOG.info("channel_name: " + channel_name);
      LOG.info("user_id: " + user_id);
      LOG.info("user_name: " + user_name);
      LOG.info("text: " + text);
      LOG.info("response_url: " + response_url);
      LOG.info("trigger_id: " + trigger_id);

      JSONObject res = new JSONObject();

      String userToken = null;
      boolean userIdentityVerified = false;

      // Get stored slack account data
      final SlackAccountDAO slackAccountDAO = container.getComponentInstanceOfType(SlackAccountDAO.class);
      final SlackAccountEntity slackEntity = slackAccountDAO.getAccountBySlackId(user_id);

      if (slackEntity != null) {
        userToken = slackEntity.getToken();
      }

      // Verify slack user's token
      if (userToken != null && (command.equals("/meeds_kudos") || command.equals("/meeds_send_kudos"))) {
        final String _userToken = userToken;
        final UsersIdentityResponse usersIdentityRes = this.methods.usersIdentity(r -> r.token(_userToken));

        if (!usersIdentityRes.isOk()) {
          LOG.error("Failed to get slack user identity");
          res.put("text", ":skull_and_crossbones: Failed to get slack user identity");
        } else {
          LOG.warn("Asking by: user={} email={} identityId={}", usersIdentityRes.getUser().getId(),
              usersIdentityRes.getUser().getEmail(), slackEntity.getIdentityId());
          userIdentityVerified = true;
        }
      }

      if (command.equals("/meeds_signin")) {  // Response with signin link
        res.put("text", ":new: Please signin with " + SLACK_USER_AUTH_URL);
      } else if (command.equals("/meeds_whoami")) {
        if (slackEntity == null) {
          res.put("text", ":question: Hey unamed, please signin first. I don't known who you are!");
        } else {
          Identity identity = identityManager.getIdentity(slackEntity.getIdentityId().toString(), false);
          res.put("text", ":wave: Hey `" + user_name + "`, your identity id is `"
            + slackEntity.getIdentityId() + "`, meeds username is `" + identity.getRemoteId()
            + "` and slack account email is `" + slackEntity.getEmail() + "`"
          );
        }
      } else if (command.equals("/meeds_clear")) {  // Clear stored slack account data
        slackAccountDAO.delete(slackEntity);
        res.put("text", ":heavy_check_mark: Successfull cleared slack connection for your account");
      } else if (command.equals("/meeds_kudos")) {  // Kudos statistics

        if (!userIdentityVerified) {
          res.put("text", "You must signin first to use this command");
        } else {
          final LocalDateTime now = LocalDateTime.now();
          // From start of current month
          final long periodStart = Utils.timeToSecondsAtDayStart(now.withDayOfMonth(1));
          // To now
          final long periodEnd = Utils.getCurrentTimeInSeconds();

          KudosService kudosService = getServiceOfType(KudosService.class);
          Identity identity = identityManager.getIdentity(slackEntity.getIdentityId().toString(), false);
          AccountSettings kudosSettings = kudosService.getAccountSettings(identity.getRemoteId());

          final long kudosSent = kudosService.countKudosByPeriodAndSender(slackEntity.getIdentityId(), periodStart,periodEnd);
          final long kudosReceived = kudosService.countKudosByPeriodAndReceiver(slackEntity.getIdentityId(), periodStart, periodEnd);

          LOG.warn("Kudos SENT: " + kudosSent);
          LOG.warn("Kudos RECEIVED: " + kudosReceived);

          res.put("text", ":gift: You have sent `" + kudosSent + "` and received `" + kudosReceived + "` kudos points. Remaining `" + kudosSettings.getRemainingKudos() + "`");
        }
      } else if (command.equals("/meeds_send_kudos")) {  // Send kudos to another user
        String[] commandArgs = text.split(" ");
        LOG.warn("Send kudos command args: " + String.join(",", commandArgs));
        if (commandArgs.length < 2) {
          res.put("text", ":skull_and_crossbones: Wrong syntax. Try: `/meeds_send_kudos {KUDOS} {RECEIVER_USER} [MESSAGE]`");
        } else {
          try {
            final long kudosPoints = Long.parseLong(commandArgs[0]);
            final String receiverUser = commandArgs[1];
            String message = "Sent from Slack Bot";

            if (commandArgs.length > 2) {
              message = commandArgs[2];
            }

            if (!userIdentityVerified) {
              res.put("text", "You must signin first to use this command");
            } else {
              Identity sender = identityManager.getIdentity(slackEntity.getIdentityId().toString(), false);
              Identity receiver = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverUser);

              KudosService kudosService = getServiceOfType(KudosService.class);
              AccountSettings kudosSettings = kudosService.getAccountSettings(sender.getRemoteId());

              if (kudosSettings.getRemainingKudos() <= kudosPoints) {
                res.put("text", ":skull_and_crossbones: You have not enough kudos points");
              } else {
                final LocalDateTime now = LocalDateTime.now();
                // From start of current month
                final long periodStart = Utils.timeToSecondsAtDayStart(now.withDayOfMonth(1));
                // To now
                final long periodEnd = Utils.getCurrentTimeInSeconds();
                
                long kudosSent = kudosService.countKudosByPeriodAndSender(slackEntity.getIdentityId(), periodStart, periodEnd);
                long kudosReceived = kudosService.countKudosByPeriodAndReceiver(slackEntity.getIdentityId(), periodStart, periodEnd);

                LOG.warn("Before Kudos SENT: " + kudosSent);
                LOG.warn("Before Kudos RECEIVED: " + kudosReceived);
                LOG.warn("Before Kudos REMAINING: " + kudosSettings.getRemainingKudos());
                Kudos kudos = new Kudos();
                kudos.setSenderId(sender.getRemoteId());
                kudos.setEntityId(sender.getId());
                kudos.setEntityType(KudosEntityType.USER_TIPTIP.name());
                kudos.setMessage(message);
                kudos.setReceiverId(receiver.getRemoteId());
                kudos.setReceiverType("user");
                
                kudosService.createKudos(kudos, sender.getRemoteId());
                
                res.put("text", ":gift: Sent `" + kudosPoints + "` to `" + receiver.getRemoteId() + "` successfully");

                kudosSent = kudosService.countKudosByPeriodAndSender(slackEntity.getIdentityId(), periodStart, periodEnd);
                kudosReceived = kudosService.countKudosByPeriodAndReceiver(slackEntity.getIdentityId(), periodStart, periodEnd);

                LOG.warn("After Kudos SENT: " + kudosSent);
                LOG.warn("After Kudos RECEIVED: " + kudosReceived);
                LOG.warn("After Kudos REMAINING: " + kudosSettings.getRemainingKudos());
              }
            }
          } catch (NumberFormatException nfe) {
            res.put("text", ":skull_and_crossbones: Invalid kudos number");
          } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            res.put("text", ":skull_and_crossbones: Error occured when trying to send kudos! `" + e.getMessage() + "`");
          }
        }
      } else if (command.equals("/meeds_hello")) {  // Hello
        res.put("text", ":wave: Hello there! How are you?");
      }

      return Response.ok(res.toString().getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_JSON).build();
      
    } catch (final Exception e) {
      e.printStackTrace();
      LOG.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  /**
   * Slack oauth signin
   * @param code
   * @param error
   * @param state
   * @return
   */
  @GET
  @Path("oauth")
  @RolesAllowed("users")
  public Response oauth(@QueryParam("code") final String code, @QueryParam("error") final String error, @QueryParam("state") final String state) {
    try {
      LOG.info("RECEIVED CODE: " + code + " ERROR: " + error);
      if (code != null) {
        final OAuthV2AccessResponse oAuthRes = Slack.getInstance().methods().oauthV2Access(r -> r
          .clientId(SLACK_CLIENT_ID)
          .clientSecret(SLACK_CLIENT_SECRET)
          .code(code)
          .redirectUri(SLACK_REDIRECT_URI)
        );
        LOG.warn(oAuthRes);
        if (oAuthRes.isOk()) {
          if (oAuthRes.getTokenType() == null) {
            String userId;
            try {
              userId = ConversationState.getCurrent().getIdentity().getUserId();
            } catch (final Exception e) {
              return Response.status(HTTPStatus.UNAUTHORIZED).build();
            }
            if (StringUtils.isBlank(userId) || IdentityConstants.ANONIM.equals(userId)) {
              return Response.status(HTTPStatus.UNAUTHORIZED).build();
            }
            Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
            final String userToken = oAuthRes.getAuthedUser().getAccessToken();
            final UsersIdentityResponse slackUserIdentity = this.methods.usersIdentity(r -> r.token(userToken));
            LOG.warn("Slack Identity: {} - Meeds Identity: {}", slackUserIdentity, identity);
            final String email = slackUserIdentity.getUser().getEmail();

            final SlackAccountDAO slackAccountDAO = (SlackAccountDAO) container.getComponentInstanceOfType(SlackAccountDAO.class);
            SlackAccountEntity slackAccountEntity = new SlackAccountEntity();
            slackAccountEntity.setIdentityId(Long.parseLong(identity.getId()));
            slackAccountEntity.setSlackId(oAuthRes.getAuthedUser().getId());
            slackAccountEntity.setSlackTeamId(oAuthRes.getTeam().getId());
            slackAccountEntity.setEmail(email);
            slackAccountEntity.setToken(userToken);
            LOG.warn(slackAccountEntity);
            slackAccountEntity = slackAccountDAO.create(slackAccountEntity);
            return Response.ok("Authentication completed").build();
          } else {
            SLACK_BOT_TOKEN = oAuthRes.getAccessToken();
            System.setProperty("gamification.connectors.slack.botToken", SLACK_BOT_TOKEN);
            this.initSlackClient();
            LOG.warn("Please save this token to gamification.connectors.slack.botToken in exo.properties file: " + SLACK_BOT_TOKEN);
            return Response.ok(SLACK_BOT_TOKEN).build();
          }
        } else {
          return Response.status(
            Response.Status.FORBIDDEN
          ).entity("Authentication failed: " + oAuthRes.getError()).build();
        }
      } else {
        return Response.status(
            Response.Status.FORBIDDEN
          ).entity(error).build();
      }
    } catch (final Exception e) {
      e.printStackTrace();
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }
}
