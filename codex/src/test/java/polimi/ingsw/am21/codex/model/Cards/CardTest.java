package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void getId() {
        Card card = new ConcreteCard(1);
        assertEquals(card.getId(), 1);
    }
}