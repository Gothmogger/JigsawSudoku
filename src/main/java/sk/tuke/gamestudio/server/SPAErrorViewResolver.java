package sk.tuke.gamestudio.server;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

public class SPAErrorViewResolver implements ErrorViewResolver {
        @Override
        public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
            // Use the request or status to optionally return a ModelAndView
            if (status == HttpStatus.NOT_FOUND) {
                ModelAndView view = new ModelAndView("/index.html");
                view.setStatus(HttpStatus.OK);
                return view;
            }
            return null;
        }
}