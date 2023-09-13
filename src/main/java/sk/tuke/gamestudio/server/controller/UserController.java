package sk.tuke.gamestudio.server.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.server.dto.UserDto;
import sk.tuke.gamestudio.server.webservice.MyUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
//@RequestMapping("/register")
public class UserController {
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    CsrfTokenRequestAttributeHandler csrfRequestHandler;
    @Autowired
    CsrfTokenRepository csrfRepo;

    @RequestMapping(value = "/api/register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object register(@RequestBody @Valid UserDto userDto, BindingResult result, HttpServletRequest request, HttpServletResponse response) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(result.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        User registered = ((MyUserDetailsService) userDetailsService).registerNewUserAccount(userDto.toUser());
        if (registered != null) {
            try {
                request.login(userDto.getUserName(), userDto.getPassword());
                if (request.getSession(false) != null) {
                    request.changeSessionId();
                    replaceCsrfToken(request, response);
                }
            } catch (ServletException e) {
                return ResponseEntity.internalServerError();
            }
            return ResponseEntity.ok().build();
        } else {
            result.addError(new ObjectError("userName", "The name is already in use."));
            return new ResponseEntity<>(result.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
    }

    // Taken from CsrfAuthenticationStrategy.java .. request.login does not call it like an auth filter does
    private void replaceCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        boolean containsToken = csrfRepo.loadToken(request) != null;
        if (containsToken) {
            csrfRepo.saveToken(null, request, response);
            DeferredCsrfToken deferredCsrfToken = csrfRepo.loadDeferredToken(request, response);
            csrfRequestHandler.handle(request, response, deferredCsrfToken::get);
        }
    }
}
