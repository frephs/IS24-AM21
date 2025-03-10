package polimi.ingsw.am21.codex.model.Cards.Commons;

import java.util.Collections;
import java.util.List;

public class Deck<T> {

  /**
   * The list of cards in the deck
   */
  private List<T> cards;

  /**
   * @param cards list of cards
   */
  public Deck(List<T> cards) {
    this.cards = cards;
  }

  /**
   * Shuffles the deck
   */
  public void shuffle() {
    Collections.shuffle(this.cards);
  }

  /**
   * Draws a card from the deck
   *
   * @return the card drawn
   */
  public T draw() throws EmptyDeckException {
    if (this.cardsLeft() == 0) throw new EmptyDeckException();
    return this.cards.removeLast();
  }

  /**
   * Draws a card from the deck
   *
   * @param n number of cards to draw
   * @return the list of drawn cards
   */
  public List<T> draw(int n) throws EmptyDeckException {
    if (this.cardsLeft() < n) throw new EmptyDeckException();
    List<T> drawn =
      this.cards.subList(this.cards.size() - n, this.cards.size());
    this.cards = this.cards.subList(0, this.cards.size() - n);
    return drawn;
  }

  /**
   * Returns the number of cards left in the deck
   */
  public int cardsLeft() {
    return this.cards.size();
  }

  /**
   * Inserts a card in the bottom of the deck
   *
   * @param card the card to insert
   */
  public void insert(T card) {
    this.cards.addFirst(card);
  }

  /**
   * Lets you peek the top card of the deck, without removing it
   * @return The top card of the deck
   */
  public T peek() {
    return this.cards.getLast();
  }
}
