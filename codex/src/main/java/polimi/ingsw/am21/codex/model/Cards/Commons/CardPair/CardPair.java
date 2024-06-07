package polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;

import polimi.ingsw.am21.codex.model.Cards.Card;

public class CardPair<T extends Card> extends CardPairBase<T> {

  public CardPair(T firstCard, T secondCard) {
    super(firstCard, secondCard);
  }
}
