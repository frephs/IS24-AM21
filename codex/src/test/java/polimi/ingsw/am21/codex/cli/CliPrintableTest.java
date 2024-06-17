package polimi.ingsw.am21.codex.cli;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Commons.Deck;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;

class CliPrintableTest {

  @Test
  void printCards() {
    CardsLoader cardsLoader = new CardsLoader();

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

    Cli.Options options = new Cli.Options(true);

    try {
      System.out.println(resourceDeck.draw().cardToAscii(options));
      System.out.println(goldDeck.draw().cardToAscii(options));
      System.out.println(starterDeck.draw().cardToAscii(options));
      System.out.println(objectiveDeck.draw().cardToString(options));
    } catch (EmptyDeckException e) {
      throw new RuntimeException(e);
    }
  }
}
