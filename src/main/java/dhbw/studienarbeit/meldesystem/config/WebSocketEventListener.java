package dhbw.studienarbeit.meldesystem.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebSocketEventListener {

    @org.springframework.context.event.EventListener
    public void handleWebSocketConnectListener(
            org.springframework.web.socket.messaging.SessionConnectedEvent event) {
        log.info("WebSocket connection established");
    }

    @org.springframework.context.event.EventListener
    public void handleWebSocketDisconnectListener(
            org.springframework.web.socket.messaging.SessionDisconnectEvent event) {
        log.info("WebSocket connection closed");
    }

}