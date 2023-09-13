package sk.tuke.gamestudio.server.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.MyUserDetails;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.server.dto.RatingDto;
import sk.tuke.gamestudio.service.CommentService;
import sk.tuke.gamestudio.service.RatingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/rating")
public class RatingServiceRest {
    @Autowired
    private RatingService ratingService;

    @GetMapping("/{game}")
    public int getAverageRating(@PathVariable @NotBlank String game) {
        return ratingService.getAverageRating(game);
    }

    @GetMapping("/{game}/{player}")
    public int getRating(@PathVariable @NotBlank String game, @PathVariable @NotBlank String player) {
        return ratingService.getRating(game, player);
    }

    @PostMapping
    public ResponseEntity<Void> setRating(@RequestBody @Valid RatingDto ratingDto) {
        /*if (SecurityContextHolder.getContext().getAuthentication()
                instanceof AnonymousAuthenticationToken)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);*/
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Rating rating = ratingDto.toRating();
        rating.setPlayer(userDetails.getUser());
        rating.setGame("JigsawSudoku");
        ratingService.setRating(rating);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}