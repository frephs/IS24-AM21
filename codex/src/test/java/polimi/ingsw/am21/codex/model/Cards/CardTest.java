package polimi.ingsw.am21.codex.model.Cards;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CardTest {

  @Test
  void getId() {
    Card card = new ConcreteCard(1);
    assertEquals(card.getId(), 1);
  }
}
