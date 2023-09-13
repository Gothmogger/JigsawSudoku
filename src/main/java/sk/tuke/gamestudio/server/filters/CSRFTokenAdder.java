package sk.tuke.gamestudio.server.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

public class CSRFTokenAdder extends GenericFilterBean {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        chain.doFilter(request, new ResponseWrapper(response, request));
    }

    public static class ResponseWrapper extends HttpServletResponseWrapper {
        private HttpServletRequest request;
        public ResponseWrapper(HttpServletResponse response, HttpServletRequest request) {
            super(response);

            this.request = request;
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            String cookieHeader = super.getHeader("Set-Cookie");
            if (cookieHeader != null && cookieHeader.contains("JSESSIONID")) {
                CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                super.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());
            }
        }
    }
}
