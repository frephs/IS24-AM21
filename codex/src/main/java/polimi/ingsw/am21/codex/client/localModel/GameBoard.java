package polimi.ingsw.am21.codex.client.localModel;

import java.util.List;
import java.util.Map;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;

public class GameBoard {

  CardsLoader cardsLoader = new CardsLoader();

  public CardPair<Card> goldCards;
  public CardPair<Card> resourceCards;
  public Card secretObjective;
  public List<Card> hand;

  public Map<String, Player> players;
}
