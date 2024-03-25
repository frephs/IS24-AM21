package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceCardFrontSideTest {

    @Test
    void evaluate() {
        PlayerBoard pb = new PlayerBoard(new ArrayList<>(), new PlayableCard(), new ObjectiveCard());

        List<Integer> testInts = List.of(0, 123, Integer.MAX_VALUE);

        testInts.forEach(e -> {
            ResourceCardFrontSide a = new ResourceCardFrontSide(e);

            assertEquals(a.evaluate(pb), e);
        });
    }
}