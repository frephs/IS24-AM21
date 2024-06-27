package polimi.ingsw.am21.codex.model.Cards.Commons;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.GameBoard.GameBoard;

class CardsLoaderTest {

  @Test
  public void testLoadCards() {
    CardsLoader cardsLoader = new CardsLoader();
    Deck<PlayableCard> resourceDeck = new Deck<PlayableCard>(
      cardsLoader.getResourceCards()
    );
    Deck<PlayableCard> goldDeck = new Deck<PlayableCard>(
      cardsLoader.getGoldCards()
    );
    Deck<PlayableCard> starterDeck = new Deck<PlayableCard>(
      cardsLoader.getStarterCards()
    );
    Deck<ObjectiveCard> objectiveDeck = new Deck<ObjectiveCard>(
      cardsLoader.getObjectiveCards()
    );

    assertEquals(resourceDeck.cardsLeft(), 40);
    assertEquals(goldDeck.cardsLeft(), 40);
    assertEquals(starterDeck.cardsLeft(), 6);
    assertEquals(objectiveDeck.cardsLeft(), 16);

    GameBoard gm = new GameBoard(new CardsLoader());
    assertEquals(gm.goldCardsLeft(), 38);
    assertEquals(gm.resourceCardsLeft(), 38);
    assertEquals(gm.objectiveCardsLeft(), 14);
    assertEquals(gm.starterCardsLeft(), 6);
  }
}
