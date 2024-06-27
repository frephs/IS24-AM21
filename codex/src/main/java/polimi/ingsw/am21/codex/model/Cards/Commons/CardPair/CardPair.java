package polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;

import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;

public class CardPair<T extends Card> extends CardPairBase<T> {

  public CardPair(T firstCard, T secondCard) {
    super(firstCard, secondCard);
  }

  /**
   * Creates a CardPair from a CardIdPair
   */
  public static <T extends Card> CardPair<T> fromCardIndexPair(
    CardsLoader cardsLoader,
    CardIdPair cardIdPair
  ) {
    //noinspection unchecked
    return new CardPair<>(
      (T) cardsLoader.getCardFromId(cardIdPair.getFirst()),
      (T) cardsLoader.getCardFromId(cardIdPair.getSecond())
    );
  }
}
