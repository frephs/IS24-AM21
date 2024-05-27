package polimi.ingsw.am21.codex.model.Cards.Commons;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;

class CardsLoaderTest {

  @Test
  public void testLoadCards() {
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

    assertEquals(resourceDeck.cardsLeft(), 40);
    assertEquals(goldDeck.cardsLeft(), 40);
    assertEquals(starterDeck.cardsLeft(), 6);
    assertEquals(objectiveDeck.cardsLeft(), 16);
  }
}
