package dhbw.studienarbeit.meldesystem.config;


import dhbw.studienarbeit.meldesystem.security.CustomUserDetailsService;
import dhbw.studienarbeit.meldesystem.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String jwt = extractJwtFromHeaders(accessor);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                try {
                    Long userId = tokenProvider.getUserIdFromToken(jwt);
                    UserDetails userDetails = ((CustomUserDetailsService) userDetailsService)
                            .loadUserById(userId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    accessor.setUser(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("WebSocket authenticated for user: {}", userId);
                } catch (Exception e) {
                    log.error("WebSocket authentication failed", e);
                }
            }
        }

        return message;
    }

    private String extractJwtFromHeaders(StompHeaderAccessor accessor) {
        // Try Authorization header
        String bearerToken = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // Try X-Authorization header (für manche WebSocket Clients)
        bearerToken = accessor.getFirstNativeHeader("X-Authorization");
        if (StringUtils.hasText(bearerToken)) {
            return bearerToken;
        }

        return null;
    }
}
