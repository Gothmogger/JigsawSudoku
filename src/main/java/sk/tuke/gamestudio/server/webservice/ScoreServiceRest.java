package sk.tuke.gamestudio.server.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.MyUserDetails;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.server.dto.ScoreDto;
import sk.tuke.gamestudio.service.ScoreService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/score")
public class ScoreServiceRest {
    @Autowired
    private ScoreService scoreService;

    @GetMapping("/{game}")
    public List<ScoreDto> getTopScores(@PathVariable @NotBlank String game) {
        return scoreService.getTopScores(game).stream().map(ScoreDto::fromScore).collect(Collectors.toList());
    }
/*
    @PostMapping
    public ResponseEntity<Void> addScore(@RequestBody @Valid ScoreDto scoreDto) { // Valid nema implicit NotNull, ale casto fieldy v Score napr maju dajake constraints a v takom pripade null object casto neprejde. a tak som spozoroval, ze ked nic neje v requeste tak 400 to neprejde, musi tam byt aspon {} a to bere jak null ale ked nic ta neprejde
        if (SecurityContextHolder.getContext().getAuthentication()
                instanceof AnonymousAuthenticationToken)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Score score = scoreDto.toScore();
        score.setPlayer(userDetails.getUser());
        score.setGame("JigsawSudoku");
        scoreService.addScore(score);

        return new ResponseEntity<>(HttpStatus.OK);
    }*/
}