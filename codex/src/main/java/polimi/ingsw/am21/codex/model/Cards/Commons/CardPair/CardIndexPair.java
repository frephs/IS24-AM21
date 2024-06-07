package polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;

import polimi.ingsw.am21.codex.model.Cards.Card;

public class CardIndexPair extends CardPairBase<Integer> {

  public CardIndexPair(Integer firstCard, Integer secondCard) {
    super(firstCard, secondCard);
  }

  public static CardIndexPair fromCardPair(CardPair<Card> cardPair) {
    return new CardIndexPair(
      cardPair.getFirst().getId(),
      cardPair.getSecond().getId()
    );
  }
}
