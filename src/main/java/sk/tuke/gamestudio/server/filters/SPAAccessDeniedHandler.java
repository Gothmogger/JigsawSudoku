package sk.tuke.gamestudio.server.filters;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import java.io.IOException;

public class SPAAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest req,
                       HttpServletResponse res,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // if session expired and user sends get request, which does not check CSRF, we respond with unauthorized
        // and send him a token, which will also send a session cookie.
        /*CsrfToken csrfToken = (CsrfToken) req.getAttribute(CsrfToken.class.getName());
        res.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());*/
        //csrfToken.getToken();

        /*
        if (accessDeniedException instanceof CsrfException)
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        else // bad roles
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);*/
    }
}