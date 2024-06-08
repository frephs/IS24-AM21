package polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;

import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;

public class CardPair<T extends Card> extends CardPairBase<T> {

  public CardPair(T firstCard, T secondCard) {
    super(firstCard, secondCard);
  }

  public static <T extends Card> CardPair<T> fromCardIndexPair(
    CardsLoader cardsLoader,
    CardIndexPair cardIndexPair
  ) {
    //noinspection unchecked
    return new CardPair<T>(
      (T) cardsLoader.getCardFromId(cardIndexPair.getFirst()),
      (T) cardsLoader.getCardFromId(cardIndexPair.getSecond())
    );
  }
}
