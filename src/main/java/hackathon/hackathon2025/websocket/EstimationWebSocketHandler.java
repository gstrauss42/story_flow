package hackathon.hackathon2025.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class EstimationWebSocketHandler extends TextWebSocketHandler {
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages
        String payload = message.getPayload();
        // Process and send updates
        session.sendMessage(new TextMessage("Processing: " + payload));
    }
}