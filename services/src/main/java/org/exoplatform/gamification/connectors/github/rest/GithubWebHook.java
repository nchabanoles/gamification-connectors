package org.exoplatform.gamification.connectors.github.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.exoplatform.gamification.connectors.github.dao.GitHubAccountDAO;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.listener.ListenerService;

@Path("/gamification/connectors/github/")
@Produces(MediaType.APPLICATION_JSON)

public class GithubWebHook implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(GithubWebHook.class);
    private static  ListenerService listenerService;
    private static GitHubAccountDAO gitHubAccountDAO;

    public GithubWebHook(ListenerService listenerService, GitHubAccountDAO gitHubAccountDAO) {
        this.listenerService = listenerService;
        this.gitHubAccountDAO = gitHubAccountDAO;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("webhook")

    public Response gitHubEvent(@HeaderParam("x-github-event") String event, String obj) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode infoNode = objectMapper.readTree(obj);
            String ruleTitle = "";
            String senderId = "";
            String receiverId = "";
            String object = "";

           switch(event) {
                case "push":
                    {
                        ruleTitle = "pushCode";
                        senderId = getUserByGithubId(infoNode.get("pusher").get("name").textValue());
                        receiverId = senderId;
                        object = infoNode.get("head_commit").get("url").textValue();
                        if(senderId!=null) {
                            createGamificationHistory(ruleTitle, senderId, receiverId, object);
                        }
                    }

                    break;
                case "pull_request":
                    {
                        if (infoNode.get("action").textValue().equals("opened")) {
                            ruleTitle = "creatPullRequest";
                            senderId = getUserByGithubId(infoNode.get("sender").get("login").textValue());
                            receiverId = senderId;
                            object = infoNode.get("pull_request").get("html_url").textValue();
                            if(senderId!=null){
                                createGamificationHistory(ruleTitle, senderId, receiverId, object);
                            }
                        }

                    }
                    break;
                case "pull_request_review_comment":
                    {
                        ruleTitle = "commentPullRequest";
                        senderId = getUserByGithubId(infoNode.get("comment").get("user").get("login").textValue());
                        receiverId = senderId;
                        object = infoNode.get("comment").get("_links").get("html").get("href").textValue();
                        if(senderId!=null){
                            createGamificationHistory(ruleTitle, senderId, receiverId, object);
                        }
                    }
                    break;
                case "pull_request_review":
                    {
                        ruleTitle = "reviewPullRequest";
                        senderId = getUserByGithubId(infoNode.get("review").get("user").get("login").textValue());
                        receiverId = senderId;
                        object = infoNode.get("review").get("html_url").textValue();
                        if(senderId!=null && !infoNode.get("review").get("state").textValue().equals("commented")){
                            createGamificationHistory(ruleTitle, senderId, receiverId, object);
                        }
                        if(infoNode.get("review").get("state").textValue().equals("approved")){
                            receiverId = getUserByGithubId(infoNode.get("pull_request").get("user").get("login").textValue());
                            if(senderId!=null && receiverId!=null){
                                ruleTitle = "pullRequestValidated";
                                createGamificationHistory(ruleTitle, senderId, receiverId, object);
                            }
                        }
                    }
                    break;

            }


            return Response.ok().build();
        }
           catch (Exception e){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("error")
                        .build();
            }

    }

    private void createGamificationHistory(String ruleTitle, String senderId, String receiverId, String object){
        try {
            Map<String, String> gam = new HashMap<>();
            gam.put("ruleTitle", ruleTitle);
            gam.put("senderId", senderId);
            gam.put("receiverId", receiverId);
            gam.put("object", object);
            listenerService.broadcast("exo.gamification.generic.action", gam, "");
            LOG.info("Github action {} gamified for user {} to {}", ruleTitle, senderId, receiverId);
        } catch (Exception e) {
            LOG.error("Cannot broadcast gamification event",e);
        }
    }


    private String getUserByGithubId(String id){
        if (id==null) return null;
        try {
            return gitHubAccountDAO.getAccountByGithubId(id).getUserName();
        } catch (Exception e) {
            LOG.error("Cannot get user with GithubId {}",id,e);
            return null;
        }
    }


}
