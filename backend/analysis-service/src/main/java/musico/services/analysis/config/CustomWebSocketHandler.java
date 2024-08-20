package musico.services.analysis.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;


import java.io.IOException;
import java.util.Map;

import static org.springframework.web.socket.CloseStatus.SERVER_ERROR;

@RequiredArgsConstructor
@Slf4j
public class CustomWebSocketHandler extends AbstractWebSocketHandler {

    private final Map<String, WebSocketSession> sessions;


    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.info("Adding Decorator");
        if (message instanceof TextMessage msg) {
            String payload = msg.getPayload();
            message = new TextMessage(payload + "\u0000");
            log.info(message.getPayload().toString());
        }
        super.handleMessage(session, message);
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        var principal = session.getPrincipal();

        if (principal == null || principal.getName() == null) {
            session.close(SERVER_ERROR.withReason("User must be authenticated"));
            return;
        }

        sessions.put(principal.getName(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        var principal = session.getPrincipal();
        sessions.remove(principal.getName());
    }

}