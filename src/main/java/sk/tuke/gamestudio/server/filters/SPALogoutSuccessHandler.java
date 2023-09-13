package sk.tuke.gamestudio.server.filters;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.DeferredCsrfToken;

import java.io.IOException;

public class SPALogoutSuccessHandler implements LogoutSuccessHandler {
    CsrfTokenRequestAttributeHandler handler;
    CsrfTokenRepository repo;

    public SPALogoutSuccessHandler(CsrfTokenRequestAttributeHandler handler, CsrfTokenRepository repo) {
        this.handler = handler;
        this.repo = repo;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);

        //HttpSession session = request.getSession(true); // Force it to create session even after logout. no need tho, since repo will do it
        /*DeferredCsrfToken deferredCsrfToken = repo.loadDeferredToken(request, response);
        handler.handle(request, response, deferredCsrfToken::get); // create new token for new session(which it will create), as is done in CsrfAuthenticationStrategy
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        //csrfToken.getToken();
        response.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());*/
    }
}
