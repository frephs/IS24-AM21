package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.PlayerBoard;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void getId() {
        Card card = new ConcreteCard(1);
        assertEquals(card.getId(), 1);
    }
}

class ConcreteCard extends Card {
    public ConcreteCard(int id) {
        super(id);
    }

    public int evaluate(PlayerBoard playerboard) {
        return 0;
    }
}