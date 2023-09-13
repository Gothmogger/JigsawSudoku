package sk.tuke.gamestudio;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.core.Field;
import sk.tuke.gamestudio.service.*;

@SpringBootApplication
@Configuration
@Profile(value = {"test"})
public class MVCConfig {

    public static void main(String[] args) {SpringApplication.run(MVCConfig.class);
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION,
            proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Field field() { return new Field(); }

    @Bean
    public ScoreService scoreService() {
        return new ScoreServiceJPA();
    }

    @Bean
    public CommentService commentService() {
        return new CommentServiceJPA();
    }

    @Bean
    public RatingService ratingService() {
        return new RatingServiceJPA();
    }

    @Bean
    public ToJsonUtil toJsonUtil() { return new ToJsonUtil(); }
}
