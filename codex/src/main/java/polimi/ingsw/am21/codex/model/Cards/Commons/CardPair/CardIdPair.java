package polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;

import polimi.ingsw.am21.codex.model.Cards.Card;

public class CardIdPair extends CardPairBase<Integer> {

  public CardIdPair(Integer firstCard, Integer secondCard) {
    super(firstCard, secondCard);
  }

  /**
   * Creates a CardIdPair from a CardPair
   */
  public static <T extends Card> CardIdPair fromCardPair(CardPair<T> cardPair) {
    return new CardIdPair(
      cardPair.getFirst().getId(),
      cardPair.getSecond().getId()
    );
  }
}
