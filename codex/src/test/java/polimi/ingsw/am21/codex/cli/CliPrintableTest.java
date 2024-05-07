package polimi.ingsw.am21.codex.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Commons.Deck;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameBoard.GameBoard;

class CliPrintableTest {

  @Test
  void printCards() {
    CardsLoader cardsLoader = new CardsLoader();
    List<Deck<Card>> decks = new LinkedList<>();

    Deck<PlayableCard> resourceDeck = new Deck<PlayableCard>(
      cardsLoader.loadResourceCards()
    );

    Deck<PlayableCard> goldDeck = new Deck<PlayableCard>(
      cardsLoader.loadGoldCards()
    );

    Deck<PlayableCard> starterDeck = new Deck<PlayableCard>(
      cardsLoader.loadStarterCards()
    );

    Deck<ObjectiveCard> objectiveDeck = new Deck<ObjectiveCard>(
      cardsLoader.loadObjectiveCards()
    );

    try {
      System.out.println(resourceDeck.draw().cardToAscii(new HashMap<>()));
      System.out.println(goldDeck.draw().cardToAscii(new HashMap<>()));
      System.out.println(starterDeck.draw().cardToAscii(new HashMap<>()));
    } catch (EmptyDeckException e) {
      throw new RuntimeException(e);
    }
  }
}
