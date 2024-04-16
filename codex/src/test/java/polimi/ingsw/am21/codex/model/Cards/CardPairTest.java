package polimi.ingsw.am21.codex.model.Cards;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.GameBoard.CardPair;

class CardPairTest {

  CardPair<Card> pair;

  @BeforeEach
  void setup() {
    this.pair = new CardPair<>(new ConcreteCard(123), new ConcreteCard(456));
  }

  @Test
  void getFirst() {
    assertEquals(pair.getFirst().getId(), 123);
  }

  @Test
  void getSecond() {
    assertEquals(pair.getSecond().getId(), 456);
  }

  @Test
  void replaceFirst() {
    pair.replaceFirst(new ConcreteCard(789));
    assertEquals(pair.getFirst().getId(), 789);
  }

  @Test
  void replaceSecond() {
    pair.replaceSecond(new ConcreteCard(789));
    assertEquals(pair.getSecond().getId(), 789);
  }

  @Test
  void swap() {
    pair.swap();
    assertEquals(pair.getFirst().getId(), 456);
    assertEquals(pair.getSecond().getId(), 123);
  }
}
