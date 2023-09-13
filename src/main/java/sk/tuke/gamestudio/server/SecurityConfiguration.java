package sk.tuke.gamestudio.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.session.DisableEncodeUrlFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import sk.tuke.gamestudio.server.filters.*;
import sk.tuke.gamestudio.server.webservice.MyUserDetailsService;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/*
SPA Session politics - Session ID gets changed when logging in, or registering (which logs user in automatically), so
session.isNew() will return false. When logout happens, session gets invalidated, but new one does not get created.
Session is sort of managed by Tomcat or jakarta or whatever. It gets created when you call request.getSession(). The
JSESSIONID cookie gets added to the response by tomcat I presume, but creation of session is handled by Spring or you.
CSRF Token creation is deferred - a differed token is set to request attribute, only upon accessing it from request attribute
and calling some of its methods it is loaded from session. To save performance. So only when CSRFFilter determines that a
path needs to be protected with CSRF that it loads it.
All the above is default Spring behaviour.
I could create a new session after logout. But why. I will just let the frontend send requests. When it gets told that
CSRF Token is wrong, I will add it to header to get it from there. This will mean on the frontend that a session has
expired. I will then repeat the request on the frontend. When unauthorized gets returned, that means session has
expired. Spring will or will not create new session, depending on whether it's get or post (get will not because CSRF
token was not loaded and therefore session was not accessed and so no need to even create one). The next request will
fail, since CSRF token will be wrong, so resend.
Alternative would be to just always create session, even after logout and send the token immediately, already as response
to the logout POST. But, expiration would be harder to handle. The first request after expiration would still fail... Because
no token was sent on expiration, since you can not... So I chose to stick to Spring behaviour.
Also, there is Cookie CSRF Repository. This would send cookies, which do not depend on session. But I feel like it is a
waste to send the cookie forth, while obviously also needing to send the token in a header in the request. Though it
would mean less need for a session, which indeed caught me off guard, POSTS to log in etc. will not need a session at
all.
requestHandler.setCsrfRequestAttributeName("_csrf"); - if u set this to null, then u can find in the source code that
to determine this name, it loads the token from session. So it will be loaded. And if it is not in session, then it will
be created. But this is complicated lol so need to refer to source code and migration docs. For example, with cookie
repo, setting it to null will in fact, just make sure that the token is grabbed from the cookie. Only in the generate
function it is sent as cookie in response tho. In docs its recommended not to set it to null, but to add a filter after
auth filter. That would however load it from cookie or generate, but after login it would not since the filter does not
continue the chain. So a different solution, ideally add it before filter and add the code after filterChain call so that
you get it on its way back.
But there is also CSRFAuthenticationStrategy, which deletes token and replaces with new (which is also deferred), so after
logging in need to send token in response too. Here I do not wait for failed request but send the token immediately,
because there is a session... After logout there isn't.
Login and logout are POST. So, CSRFFilter will create a new RepositoryDeferredCsrfToken and add it as request parameter.
It is accessed, because POST has to be secured against CSRF, so it is accessed and loaded either from cookie or from
session and checked. On login csrf handler it is deleted from session or if cookie repo than a set cookie xsrf = ; is
set. and then this request param is replaced with new RepositoryDeferredCsrfToken so that you can access it again and
call getToken for example in filter, this will load it and since it doesn't exist also generate and save. So another
set-cookie will be added, this time with correct token. But in logout handler, it is only removed from session or cookie,
but new RepositoryDeferredCsrfToken is not put into request handler, so the old with the old token value is there. so
when you access it you will get the old value. So you would have to add the code that is in login handler to logout
success handler, so that after logout u get the right token.
*/

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Autowired
    private Environment environment;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CsrfTokenRepository csrfRepo, CsrfTokenRequestAttributeHandler csrfRequestHandler) throws Exception {
        http.addFilterBefore(
                new LoginAndLogoutAndRegisterAnonymousFilter(),  LogoutFilter.class); // maybe doesnt make much sense for SPA
        http.addFilterAfter(new CSRFTokenAdder(), DisableEncodeUrlFilter.class);
        /*XorCsrfTokenRequestAttributeHandler delegate = new XorCsrfTokenRequestAttributeHandler();
        delegate.setCsrfRequestAttributeName("_csrf");
        CsrfTokenRequestHandler requestHandler = delegate::handle;*/ //use this if you plan to use cookieRep and want a workaround BREACH
        /*CsrfTokenRequestAttributeHandler requestHandler = csrfRequestHandler();
        // set the name of the attribute the CsrfToken will be populated on
        requestHandler.setCsrfRequestAttributeName("_csrf");*/
        //CsrfTokenRepository repo = new HttpSessionCsrfTokenRepository();//CookieCsrfTokenRepository.withHttpOnlyFalse();
        http.csrf(csrf -> csrf.csrfTokenRepository(csrfRepo).csrfTokenRequestHandler(csrfRequestHandler));
        /*exposing CSRF token with each set-Cookie, this will send a token, but only check if the one in cookie is equal
        to the one in header or form field, so the security stems form the fact that only ur JS can add it there...
        this other boilerplate code is taken from spring migration strategy, since now they are securing it against BREACH attack
        and you need this...
        They are also loading CSRF token lazily, not on every request... but you need to send it to the frontend with every
        request with SPA, so they tell u to add a filter after BasicAuthenitcationFilter that does this:
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		// Render the token value to a cookie by causing the deferred token to be loaded
		csrfToken.getToken();
		*/
        // X-XSRF-TOKEN when cookies, X-CSRF-TOKEN when sessino

        /*
        Routing an SPA web on the backend... No resources about this online, so this is what I came up with:
        1) If user navigates to /login or /register - if he is logged in, redirect to /. - this is actually handled on the frontend
        2) To a resource he is not authorized to see - Instead of sending 401, will send a redirect to /login, which will
           make him go through step 3. Do not follow this redirect when using fetch on the frontend, its only when user writes
           something like /protected/... directly to search bar (before visiting the website for example)
           Actually scrap that. There will be no protected urls other than starting with /api.. so this is handled on frontend
        3) To resource that does not exist - handler should give him index.html, which the js will correctly rout on the frontend
           This is the most important part of SPA routing
        4) To a public resource that exists, eg. index.html - he will get the resource
        5) To /api/** he will just get a json as a response - the same as in SSR - if this is undesirable, and you want to
           show index.html when writing /api/comment to search bar, then: a) facebook has its api calls to a different domain,
           so you can use a different ip address(different server) for api calls or a different server on the same ip but
           different port. b) add a header something like apiCallFromFetch: yes to tell whether to treat it like api call
           or a website get to display not found c) use post instead of get, I think YT does it
         */

        http.authorizeHttpRequests((authz) -> authz
                        //.requestMatchers(HttpMethod.GET,"/api/comment/*", "/api/rating/**", "/api/score/*").permitAll()
                        //.requestMatchers("/api/game", "/api/game/new", "/api/hint", "/api/check").permitAll()
                        //.requestMatchers("/api/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/comment", "/api/rating").authenticated()
                        .anyRequest().permitAll()
                ).formLogin(login -> login.loginPage("/api/login").usernameParameter("userName").successHandler(new SPAAuthenticationSuccessHandler()).failureHandler(new SPAAuthenticationFailureHandler()))
                .logout(logout -> logout.logoutUrl("/api/logout").logoutSuccessHandler(new SPALogoutSuccessHandler(csrfRequestHandler, csrfRepo)));

        http.exceptionHandling(handling -> {handling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
        handling.accessDeniedHandler(new SPAAccessDeniedHandler());
        });
        /*
        AuthenticationEntryPoint implementation is to handle 401, UNAUTHORIZED access (when roles dont match its also 401, but authrity 403 apparently)
        AccessDeniedHandler implementation is there for 403, FORBIDDEN access, for CSRF
         */

        /*
        Can also be added as meta tag to the served html file, which is more efficient
            <meta http-equiv="Content-Security-Policy"
          content="...policy definition here..." />
         */
        /*
        To fix vite or webpack not working with csp, create .env file and put in : INLINE_RUNTIME_CHUNK=false IMAGE_INLINE_SIZE_LIMIT=0 This would work with create-react-app, hopefully it also works with vite
         */
        http.headers(headers ->
                headers.contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self' 'unsafe-inline';")));
        //http.sessionManagement(session -> session.maximumSessions(1).maxSessionsPreventsLogin(true)); user wont be able to login forexample if he just closed browser, setting it to false would apparently invalidate previous session
        if (environment.acceptsProfiles(Profiles.of("dev")))
            http.cors(withDefaults());
        //http.csrf(csrf ->csrf.disable());
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        // configuration for frontend run dev testing. It still gets initialized with prod profile, but does not get used
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN"));
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET","POST"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("X-CSRF-TOKEN");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CsrfTokenRepository csrfRepo() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public CsrfTokenRequestAttributeHandler csrfRequestHandler() {
        CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName("_csrf");
        return handler;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new MyUserDetailsService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); /* new BCryptPasswordEncoder(); */
    }
}