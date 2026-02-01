package dhbw.studienarbeit.meldesystem.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;


@java.lang.annotation.Target({java.lang.annotation.ElementType.PARAMETER,
        java.lang.annotation.ElementType.TYPE})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@org.springframework.security.core.annotation.AuthenticationPrincipal
public @interface CurrentUser {
}
