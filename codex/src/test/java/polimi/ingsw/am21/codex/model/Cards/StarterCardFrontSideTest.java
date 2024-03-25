package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class StarterCardFrontSideTest {

    @Test
    void evaluate() {
        PlayerBoard pb = new PlayerBoard(new ArrayList<>(), new PlayableCard(), new ObjectiveCard());

        StarterCardFrontSide a = new StarterCardFrontSide();
        // .evaluate() should always return 0
        assertEquals(a.evaluate(pb), 0);
    }
}