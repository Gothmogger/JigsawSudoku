package sk.tuke.gamestudio.server.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/*
    There are filers in the filter chain which act if URI is /login or /logout. If they do act, the request continues not
    down the filter chain. Since the filter which decides whether to allow access based on requestMatchers is last, this
    means requestMatchers does not work for /login and /logout. This filter ought to be applied before logout filter.
    If it determines than the user is logged in, it does not let him access /login or /register. If it determines he is
    not logged in, it does not let him access /logout. The anonymous filter is somewhere among the last, and such it is
    not known if the user is anonymous or not. Generally Authentication object is never null, it is either anonymous or
    authenticated. But since it will not be set to anonymous until too late, here we find that if the authentication
    object is null, the authentication is anonymous.

    Ideally you would use a controller and not filters for /login and /logout but this is the way Spring developers went.
    If you use controllers, you do not need this filter. (That is, you are not using the default fromLogin() implementation
    provided by Spring, which also adds these filters) But you will need to set it up such that when visiting a website
    unauthenticated, you get redirected to the login. This can easily be done with a filter like this, though you need
    to store the secured website that was to be accessed, so that you after successful login redirect back. This can
    be stored in the session or use the referrer header. Spring does this by default when you use formLogin and so it
    may be wise to just use their implementation and use this filter. The code for doing this would be ugly and exposing
    the low levels that probably ought not be exposed, when you can just use the furnished implementation.

    Otherwise, this filter has to be used. And if it is needed to return the message "Wrong credentials" through AJAX,
    and not the way Spring does it by redirecting to /login?error, then AuthenticationFailureHandler has to be supplied.
    That is if you are using loginForm.

    Just a thought lol. When you set something up incorrectly, you get all weird errors that often do not mean much and
    internet is of no help. But when you fix your errors it starts to work again. Just saying, think and then write
    with this framework.
 */

public class LoginAndLogoutAndRegisterAnonymousFilter extends GenericFilterBean {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                if (((HttpServletRequest) request).getRequestURI().equals("/api/login") || ((HttpServletRequest) request).getRequestURI().equals("/api/register")) {
                    ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
        } else if (((HttpServletRequest) request).getRequestURI().equals("/api/logout")) {
            ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        chain.doFilter(request, response);
    }
}