package org.exoplatform.gamification.connectors.github.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.exoplatform.gamification.connectors.github.services.GithubHooksManagement;
import org.exoplatform.gamification.connectors.github.utils.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/gamification/connectors/github/")
@Produces(MediaType.APPLICATION_JSON)

public class GithubWebHookRest implements ResourceContainer {

  private final Log             LOG = ExoLogger.getLogger(GithubWebHookRest.class);

  private GithubHooksManagement githubHooksManagement;

  public GithubWebHookRest(GithubHooksManagement githubHooksManagement) {
    this.githubHooksManagement = githubHooksManagement;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("webhooks")
  public Response gitHubEvent(@HeaderParam("x-github-event") String event,
                              @HeaderParam("x-hub-signature") String signature,
                              String obj) {

    if (Utils.verifySignature(obj, signature, githubHooksManagement.getSecret())) {

      try {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(obj);
        String ruleTitle = "";
        String senderId = "";
        String receiverId = "";
        String object = "";

        switch (event) {
        case "push": {
          ruleTitle = "pushCode";
          senderId = githubHooksManagement.getUserByGithubId(infoNode.get("pusher").get("name").textValue());
          receiverId = senderId;
          object = infoNode.get("head_commit").get("url").textValue();
          if (senderId != null) {
            githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
          }
        }

          break;
        case "pull_request": {
          if (infoNode.get("action").textValue().equals("opened")) {
            ruleTitle = "creatPullRequest";
            senderId = githubHooksManagement.getUserByGithubId(infoNode.get("sender").get("login").textValue());
            receiverId = senderId;
            object = infoNode.get("pull_request").get("html_url").textValue();
            if (senderId != null) {
              githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
            }
          }

        }
          break;
        case "pull_request_review_comment": {
          ruleTitle = "commentPullRequest";
          senderId = githubHooksManagement.getUserByGithubId(infoNode.get("comment").get("user").get("login").textValue());
          receiverId = senderId;
          object = infoNode.get("comment").get("_links").get("html").get("href").textValue();
          if (senderId != null) {
            githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
          }
        }
          break;
        case "pull_request_review": {
          ruleTitle = "reviewPullRequest";
          senderId = githubHooksManagement.getUserByGithubId(infoNode.get("review").get("user").get("login").textValue());
          receiverId = senderId;
          object = infoNode.get("review").get("html_url").textValue();
          if (senderId != null && !infoNode.get("review").get("state").textValue().equals("commented")) {
            githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
          }
          if (infoNode.get("review").get("state").textValue().equals("approved")) {
            receiverId =
                       githubHooksManagement.getUserByGithubId(infoNode.get("pull_request").get("user").get("login").textValue());
            if (senderId != null && receiverId != null) {
              ruleTitle = "pullRequestValidated";
              githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
            }
          }
        }
          break;

        }

        return Response.ok().build();
      } catch (Exception e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }

    } else {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

  }

}
