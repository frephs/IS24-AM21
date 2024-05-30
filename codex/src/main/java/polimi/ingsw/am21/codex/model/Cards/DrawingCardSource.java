package polimi.ingsw.am21.codex.model.Cards;

import java.io.Serializable;

public enum DrawingCardSource implements Serializable {
  CardPairFirstCard,
  CardPairSecondCard,
  Deck;

  @Override
  public String toString() {
    return switch (this) {
      case CardPairFirstCard -> "the first card from the pair of";
      case CardPairSecondCard -> "the second card from the pair of";
      case Deck -> "from the deck of";
    };
  }
}
