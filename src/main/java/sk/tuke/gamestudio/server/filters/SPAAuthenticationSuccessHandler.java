package sk.tuke.gamestudio.server.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;

import java.io.IOException;

public class SPAAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {//extends SimpleUrlAuthenticationSuccessHandler {

    public SPAAuthenticationSuccessHandler() {
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        //csrfToken.getToken();
        //clearAuthenticationAttributes(request); // we are using custom failurehandler too so no need
    }
}