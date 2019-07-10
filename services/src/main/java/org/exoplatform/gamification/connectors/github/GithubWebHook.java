package org.exoplatform.gamification.connectors.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.exoplatform.services.listener.ListenerService;

@Path("/gamification/connectors/")
@Produces(MediaType.APPLICATION_JSON)

public class GithubWebHook implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(GithubWebHook.class);
    private static  ListenerService listenerService;
    private static OrganizationService organizationService;

    public GithubWebHook(ListenerService listenerService, OrganizationService organizationService) {
        this.listenerService = listenerService;
        this.organizationService = organizationService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("githubhook")

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
                        senderId = getUserByEmail(infoNode.get("pusher").get("email").textValue());
                        receiverId = senderId;
                        object = infoNode.get("head_commit").get("url").textValue();
                        createGamificationHistory(ruleTitle, senderId, receiverId, object);
                    }

                    break;
                case "pull_request":
                    {
                        ruleTitle = "creatPullRequest";
                        senderId = getUserByEmail(getEmailFromGithub(infoNode.get("sender").get("login").textValue()));
                        receiverId = senderId;
                        object = infoNode.get("pull_request").get("url").textValue();
                        if(senderId!=null){
                            createGamificationHistory(ruleTitle, senderId, receiverId, object);
                        }
                    }
                    break;
                case "pull_request_review_comment":
                    {
                        ruleTitle = "commentPullRequest";
                        senderId = getUserByEmail(getEmailFromGithub(infoNode.get("comment").get("user").get("login").textValue()));
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
                        senderId = getUserByEmail(getEmailFromGithub(infoNode.get("review").get("user").get("login").textValue()));
                        receiverId = senderId;
                        object = infoNode.get("review").get("html_url").toString();
                        if(senderId!=null){
                            createGamificationHistory(ruleTitle, senderId, receiverId, object);
                        }
                    }
                    break;

            }

            LOG.info("Github action gamified");
            return Response.ok("Github action gamified", MediaType.APPLICATION_JSON).build();
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
        } catch (Exception e) {
            LOG.error("Cannot broadcast gamification event",e);
        }
    }

    private String getUserByEmail(String mail){
        if (mail==null) return null;
        try {
            Query query = new Query();
            query.setEmail(mail);
            ListAccess<User> users = organizationService.getUserHandler().findUsersByQuery(query);
            if(users.getSize()>0){
               return users.load(0,1)[0].getUserName();
            } else return null;
        } catch (Exception e) {
            LOG.error("Cannot get user from email",e);
            return null;
        }
    }


    private String getEmailFromGithub(String userName) {
        HttpURLConnection httpURLConnection = null;
        int status = 500;
        try {
            URL url = new URL("https://api.github.com/users/"+userName+"/events/public");
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Content-length", "0");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setAllowUserInteraction(false);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();

            status = httpURLConnection.getResponseCode();
            if (status == 200 || status == 201) {
                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {

                    if(line.contains("email")) {
                        sb.append(line + "\n");
                        break;
                    }
                }
                br.close();
                // Output to client
                 String result = sb.toString();
                return(result.split("email")[1].split(",")[0].replace("\"", "").replace(":", ""));

            }
            return null;
        } catch (Exception e) {
            LOG.error("Cannot get email of user {} from Github", userName, e);
            return null;
        }

    }
}
