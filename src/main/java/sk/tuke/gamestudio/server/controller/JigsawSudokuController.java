package sk.tuke.gamestudio.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.SudokuSolver.Tile;
import sk.tuke.gamestudio.core.Field;
import sk.tuke.gamestudio.core.GameState;
import sk.tuke.gamestudio.entity.MyUserDetails;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.server.dto.TileDto;
import sk.tuke.gamestudio.server.dto.validators.ValidList;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import sk.tuke.gamestudio.service.ScoreService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class JigsawSudokuController {
    @Autowired
    private ScoreService scoreService;

    @Autowired
    private Field field;

    private Map<Integer, String> colorMap;

    //
/*
    @GetMapping(value = {"/{path:^(?!api$).*$}/**"}) // Exclude paths starting with "/api"
    public String index(@PathVariable String path) {
        return "/index.html";
    }
*/
/*
    @GetMapping("/api/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }*/

    @RequestMapping(value = "/api/game/new", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getNewBoard() {
        field.initialize(9);
        return getBoard();
    }

    @RequestMapping(value = "/api/game", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getBoard() {
        if (field.unInitialized())
            field.initialize(9);
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("tiles", Arrays.stream(field.getTiles())
                .map(row -> Arrays.stream(row)
                        .map(TileDto::from)
                        .toArray(TileDto[]::new)
                )
                .toArray(TileDto[][]::new));
        map.put("gameState", field.getGameState().toString());
        map.put("maxPossibleScore", field.getMaxPossibleScore());
        map.put("score", field.getScore());

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/hint", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getHints() {
        if (field.unInitialized()) {
            /*HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/");
            return new ResponseEntity<>(headers, HttpStatus.FOUND);*/
            return ResponseEntity.badRequest().build();
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (field.getGameState() == GameState.Playing) {
            HashSet<TileDto> hints = new HashSet<>();
            for (int i = 0; i < 3; i++) {
                Tile hint = field.getHint();
                if (hint == null)
                    break;
                hints.add(TileDto.from(hint));
            }
            map.put("hints", hints);
        }
        map.put("score", field.getScore());
        map.put("maxPossibleScore", field.getMaxPossibleScore());
        map.put("gameState", field.getGameState().toString());

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/check", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> check(@RequestBody @Valid ValidList<TileDto> tiles) { // int defaultuje od 0, coz prejde cez validaciu
        if (field.unInitialized()) {
            return ResponseEntity.badRequest().build();
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (field.getGameState() == GameState.Playing) {
            HashSet<TileDto> correctTiles = new HashSet<>();
            HashSet<TileDto> incorrectTiles = new HashSet<>();
            for (TileDto dto : tiles) {
                Tile tile = TileDto.to(dto, field);
                if (dto.getDisplayedValue() == 0 || tile.isClue())
                    continue;
                if (tile.getValue() == dto.getDisplayedValue()) {
                    correctTiles.add(dto);
                } else {
                    incorrectTiles.add(dto);
                }
                field.setTileDisplayValue(dto.getRow(), dto.getColumn(), dto.getDisplayedValue());
            }
            map.put("correct", correctTiles);
            map.put("incorrect", incorrectTiles);

            if (field.getGameState() == GameState.Solved) {
                saveScore();
            }
        }
        map.put("score", field.getScore());
        map.put("maxPossibleScore", field.getMaxPossibleScore());
        map.put("gameState", field.getGameState().toString());

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    private void saveScore() {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
            MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Score score = new Score("JigsawSudoku", userDetails.getUser(), field.getScore());
            scoreService.addScore(score);
        }
    }
}
