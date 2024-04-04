package polimi.ingsw.am21.codex.model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    Game game;

    @BeforeEach
    void init(){
        game = new Game(2, new JSONArray());
    }

    @Test
    void testGetPlayers() {
        assertEquals(2, game.getPlayers().size());
    }

    @Test
    void testGetPlayer() {
        assertEquals("Player1", game.getPlayer(0).getName());
    }

}