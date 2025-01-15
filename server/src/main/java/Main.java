import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;

import java.time.Duration;
import java.util.*;

public class Main {

    public static void main(String[] args) {

       List<WsContext> allUsers = new ArrayList<>();
               Javalin app = Javalin.create(javalinConfig -> {
            // Modifying the WebSocketServletFactory to set the socket timeout to 120 seconds
            javalinConfig.jetty.modifyWebSocketServletFactory(jettyWebSocketServletFactory ->
                    jettyWebSocketServletFactory.setIdleTimeout(Duration.ofSeconds(120))
            );
        });

        app.ws("/", wsConfig -> {

            wsConfig.onConnect((connectContext) -> {
                System.out.println("Connected: " + connectContext.sessionId());
                allUsers.add(connectContext);
                User user = new User(connectContext);
                System.out.println(user);
                System.out.println(Arrays.toString(allUsers.toArray()));
            });

            wsConfig.onMessage((messageContext) -> {
                System.out.println("Message: " + messageContext.sessionId());
                String jsonMessage = messageContext.message();

                // parsed json
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode messageNode = objectMapper.readTree(jsonMessage);
                String recipientId = messageNode.get("recipientId").asText();
                String content = messageNode.get("content").asText();
                System.out.println(content + " " + recipientId);
                if (recipientId.isEmpty()){
                    // 121 NOTES
                    // EACH USER HAS A connectContext -> INFORMATION ABOUT THE WEBSOCKET THAT IS CREATED
                    // - KEEP A REFERENCE TO EACH WEBSOCKET -> A CONNECT TO EACH USER FE
                    // - STORING EACH USERS CONTEXT & THEN SENDING A MESSAGE TO EACH OF THEM IF A GLOBAL MESSAGE
                    // VIEW TH AJAVLIN DOCS ABOUT CONTEXT & METHODS THAT ARE ON THEM.  
                    // - https://javalin.io/documentation#wscontext  
                    messageContext.send((Map.of("content", "Hello, World")));
                } else {
                    User recipient = new User(messageContext);
                    recipient.receiveMessage(Map.of("content", content));
                    messageContext.send((Map.of("content", "Hello from the server")));
                }
            });

            wsConfig.onClose((closeContext) -> {
                System.out.println("Closed: " + closeContext.sessionId());
            });

            wsConfig.onError((errorContext) -> {
                System.out.println("Error: " + errorContext.sessionId());
            });



        });

        app.start(5001);
    }

}