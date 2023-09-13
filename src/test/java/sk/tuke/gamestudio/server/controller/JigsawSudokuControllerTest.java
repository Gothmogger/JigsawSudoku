package sk.tuke.gamestudio.server.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.MVCConfig;
import sk.tuke.gamestudio.SudokuSolver.Tile;
import sk.tuke.gamestudio.ToJsonUtil;
import sk.tuke.gamestudio.WithMockCustomUser;
import sk.tuke.gamestudio.core.Field;
import sk.tuke.gamestudio.entity.MyUserDetails;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.server.dto.TileDto;
import sk.tuke.gamestudio.server.webservice.MyUserDetailsService;
import sk.tuke.gamestudio.service.ScoreService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@RunWith(SpringRunner.class)
//@WebMvcTest(JigsawSudokuController.class)
@ActiveProfiles(profiles = {"test"}) // nwm com nastavenie classes pe springboottest nefunguje, zatotreba pouzit profiles
@SpringBootTest(classes = {/*SecurityConfiguration.class,*/ MVCConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.MOCK) // mock abo random port
//@ContextConfiguration(classes = {/*SecurityConfiguration.class,*/ MVCConfig.class})
//@SpringJUnitWebConfig
@Transactional
public class JigsawSudokuControllerTest {

    //@Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ToJsonUtil toJsonUtil;

    @Autowired
    private UserDetailsService userService;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .apply(sharedHttpSession()) // its using my SecurityConfiguration.class by default
                .build();
    }

    @Test
    public void shouldSendFieldWhenAccessingHome() throws Exception {
        Field field = new Field(9);
        TileDto[][] tileDtos = Arrays.stream(field.getTiles())
                .map(row -> Arrays.stream(row)
                        .map(TileDto::from)
                        .toArray(TileDto[]::new)
                )
                .toArray(TileDto[][]::new);

        mvc.perform(get("/api/game").sessionAttr("scopedTarget.field", field))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.gameState", is("Playing")))
                .andExpect(jsonPath("$.tiles").value(toJsonUtil.toJsonArray(tileDtos)));
    }

    @Test
    public void shouldGet3HintsTest() throws Exception {
        mvc.perform(get("/api/game/new")); // to initialize the field

        mvc.perform(get("/api/hint"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hints", hasSize(3)))
                .andExpect(jsonPath("$.gameState", is("Playing")));
    }

    @Test
    public void shouldGet1CorrectAnd1IncorrectCheckedTilesTest() throws Exception {
        Field field = new Field(9);
        Tile correctTile = null, incorrectTile = null;
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                Tile tile = field.getTile(x, y);
                if (tile.getDisplayedValue() == 0) {
                    if (correctTile == null) {
                        correctTile = new Tile(tile.getValue(), false, tile.getTileGroup(), tile.getRow(), tile.getColumn()); // shallow copy
                        correctTile.displayValue(tile.getValue());
                    } else if (incorrectTile == null) {
                        incorrectTile = new Tile(tile.getValue(), false, tile.getTileGroup(), tile.getRow(), tile.getColumn()); // shallow copy
                        incorrectTile.displayValue(field.getTile(x < 8 ? x + 1 : x - 1, y).getValue());
                    }
                }
            }
        }

        mvc.perform(post("/api/check").with(csrf()).sessionAttr("scopedTarget.field", field)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonUtil.toJson(new TileDto[]{TileDto.from(correctTile), TileDto.from(incorrectTile)})))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.correct", hasSize(1)))
                .andExpect(jsonPath("$.incorrect", hasSize(1)))
                .andExpect(jsonPath("$.gameState").value("Playing"));
    }

    @WithMockCustomUser(name = "Samuel")
    @Test
    public void shouldStoreScoreWhenGameStateIsSolvedTest() throws Exception {
        ((MyUserDetailsService)userService).registerNewUserAccount(
                ((MyUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser());
        Field field = new Field(9);
        List<Tile> tilesToCheck = new ArrayList<>();
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                Tile tile = field.getTile(x, y);
                if (tile.getDisplayedValue() == 0) {
                    Tile tileShallowCopy = new Tile(tile.getValue(), false, tile.getTileGroup(), tile.getRow(), tile.getColumn()); // shallow copy
                    tileShallowCopy.displayValue(tile.getValue());
                    tilesToCheck.add(tileShallowCopy);
                }
            }
        }

        mvc.perform(post("/api/check").with(csrf()).sessionAttr("scopedTarget.field", field)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonUtil.toJson(tilesToCheck)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.correct", hasSize(tilesToCheck.size())))
                .andExpect(jsonPath("$.incorrect", hasSize(0)))
                .andExpect(jsonPath("$.gameState").value("Solved"));

        List<Score> topScores = scoreService.getTopScores("JigsawSudoku");

        assertEquals(topScores.size(), 1);
        assertEquals(topScores.get(0).getPlayer().getUserName(), "Samuel");
        assertEquals(topScores.get(0).getPoints(), field.getMaxPossibleScore());
    }
}
