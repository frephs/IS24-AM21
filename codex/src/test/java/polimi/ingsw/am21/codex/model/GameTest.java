package polimi.ingsw.am21.codex.model;

import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.ObjectiveCard;
import polimi.ingsw.am21.codex.model.GameBoard.Lobby;
import polimi.ingsw.am21.codex.model.GameBoard.LobbyFullException;
import polimi.ingsw.am21.codex.model.GameBoard.PlayerNotFoundException;
import polimi.ingsw.am21.codex.model.GameBoard.TokenAlreadyTakenException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    Game game;
    JSONArray mockJSONArray;

    GameTest() {
        this.mockJSONArray = new JSONArray();
        game = new Game(2, mockJSONArray);
    }

    @BeforeEach
    void initTest() {

    }

    @Test
    void testLobby() {
        Lobby lobby = game.getLobby();
        assertNotNull(lobby);
        assertEquals(lobby.getRemainingPlayerSlots(), 2);
        assertEquals(lobby.getPlayersCount(), 0);
        UUID firstPlayer = UUID.randomUUID();
        try {
            lobby.addPlayer(firstPlayer);
        } catch (Exception e) {
            fail("Exception thrown while adding first player");
        }
        assertEquals(lobby.getRemainingPlayerSlots(), 1);
        assertEquals(lobby.getPlayersCount(), 1);
        lobby.setNickname(firstPlayer, "FirstPlayer");
        lobby.setToken(firstPlayer, TokenColor.BLUE);

        UUID secondPlayer = UUID.randomUUID();
        try {
            lobby.addPlayer(secondPlayer);
        } catch (Exception e) {
            fail("Exception thrown while adding second player");
        }
        assertEquals(lobby.getRemainingPlayerSlots(), 0);
        assertEquals(lobby.getPlayersCount(), 2);
        lobby.setNickname(secondPlayer, "SecondPlayer");
//        lobby.setToken(secondPlayer, TokenColor.RED);
        assertThrows(TokenAlreadyTakenException.class, () -> lobby.setToken(secondPlayer, TokenColor.BLUE));
        assertThrows(PlayerNotFoundException.class, () -> lobby.setToken(UUID.randomUUID(), TokenColor.RED));
        assertThrows(LobbyFullException.class, () -> lobby.addPlayer(UUID.randomUUID()));

        ObjectiveCard objectiveCard = lobby.getPlayerObjectiveCards(firstPlayer).getFirst();

        lobby.finalizePlayer(firstPlayer, objectiveCard);
        assertThrows(PlayerNotFoundException.class, () -> lobby.finalizePlayer(firstPlayer, objectiveCard));
    }

}