package polimi.ingsw.am21.codex.model.Cards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Commons.Deck;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;

class DeckTest {

  private final int initialSize = 50;
  private Deck<Integer> deck;

  @BeforeEach
  void generateTestDeck() {
    List<Integer> numbers = new ArrayList<>();
    for (int i = 0; i < initialSize; ++i) numbers.add(i);
    this.deck = new Deck<>(numbers);
    this.deck.shuffle();
  }

  @Test
  void shuffle() {
    boolean isShuffled = false;
    int tries = 10000;

    while (!isShuffled && --tries != 0) {
      int shouldDraw = initialSize - 1;
      try {
        while (!isShuffled) isShuffled = shouldDraw != this.deck.draw();
      } catch (EmptyDeckException ignored) {}
      if (!isShuffled) this.generateTestDeck();
    }
    assertTrue(isShuffled);
  }

  @Test
  void draw() {
    assertEquals(initialSize, this.deck.cardsLeft());
    for (int i = 0; i < initialSize; i++) {
      try {
        this.deck.draw();
      } catch (EmptyDeckException e) {
        fail(
          "Deck was empty after " +
          (i + 1) +
          " draws when the deck should contain " +
          initialSize +
          " cards"
        );
      }
    }
    assertThrows(EmptyDeckException.class, () -> this.deck.draw());
  }

  @Test
  void cardsLeft() {
    assertEquals(initialSize, this.deck.cardsLeft());
  }

  @Test
  void insert() {
    int initialCardsLeft = this.deck.cardsLeft();
    this.deck.insert(initialSize);
    assertEquals(initialCardsLeft + 1, this.deck.cardsLeft());
  }
}
