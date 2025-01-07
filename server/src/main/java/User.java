import io.javalin.websocket.WsContext;

import java.util.Map;

public class User {

    private int userId;
    private static int nextIdIncrement = 0;
    private final WsContext socket;

    public User(WsContext socket) {
        userId  = nextIdIncrement++;
        this.socket = socket;
    }

    public int getId() {
        return userId;
    }

    public void receiveMessage(Map<String, String> message) {
        socket.send(message);
    }
}
