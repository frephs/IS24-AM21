package polimi.ingsw.am21.codex.model.GameBoard;

import java.util.*;
import org.json.JSONArray;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Commons.Deck;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;

public class GameBoard {

  private final Deck<PlayableCard> goldDeck;
  private CardPair<PlayableCard> goldCards;
  private final Deck<PlayableCard> starterDeck;
  private final Deck<ObjectiveCard> objectiveDeck;
  private CardPair<ObjectiveCard> objectiveCards;
  private final Deck<PlayableCard> resourceDeck;
  private CardPair<PlayableCard> resourceCards;

  /**
   * static method to create a GameBoard from a JSON array
   * Initializes the decks using a JSONArray
   *
   * @param cards the full list
   */
  public static GameBoard fromJSON(JSONArray cards) {
    List<PlayableCard> goldCardsList = new ArrayList<>();
    List<PlayableCard> starterCardsList = new ArrayList<>();
    List<ObjectiveCard> objectiveCardsList = new ArrayList<>();
    List<PlayableCard> resourceCardsList = new ArrayList<>();

    CardsLoader loader = new CardsLoader(cards);
    loader.loadCards(
      starterCardsList,
      resourceCardsList,
      goldCardsList,
      objectiveCardsList
    );
    return new GameBoard(
      starterCardsList,
      resourceCardsList,
      goldCardsList,
      objectiveCardsList
    );
  }

  public GameBoard(CardsLoader loader) {
    this(
      loader.loadStarterCards(),
      loader.loadResourceCards(),
      loader.loadGoldCards(),
      loader.loadObjectiveCards()
    );
  }

  /**
   * Constructor with the decks
   *
   * @param goldDeck       The deck containing the gold cards
   * @param resourceDeck   The deck containing the resource cards
   * @param starterDeck    The deck containing the starter cards
   * @param objectiveDeck  The deck containing the objective cards
   * @param resourceCards  The pair of resource cards
   * @param objectiveCards The pair of common objective cards
   */
  public GameBoard(
    Deck<PlayableCard> starterDeck,
    Deck<PlayableCard> resourceDeck,
    Deck<PlayableCard> goldDeck,
    Deck<ObjectiveCard> objectiveDeck,
    CardPair<PlayableCard> resourceCards,
    CardPair<PlayableCard> goldCards,
    CardPair<ObjectiveCard> objectiveCards
  ) {
    this.goldDeck = goldDeck;
    this.goldCards = goldCards;
    this.starterDeck = starterDeck;
    this.resourceDeck = resourceDeck;
    this.resourceCards = resourceCards;
    this.objectiveDeck = objectiveDeck;
    this.objectiveCards = objectiveCards;
  }

  /**
   * Constructor
   * Initializes the decks and draws the first cards
   */
  public GameBoard(
    List<PlayableCard> starterCardsList,
    List<PlayableCard> resourceCardsList,
    List<PlayableCard> goldCardsList,
    List<ObjectiveCard> objectiveCardsList
  ) {
    this.starterDeck = new Deck<>(starterCardsList);
    this.goldDeck = new Deck<>(goldCardsList);
    this.resourceDeck = new Deck<>(resourceCardsList);
    this.objectiveDeck = new Deck<>(objectiveCardsList);

    // shuffle all the cards
    this.starterDeck.shuffle();
    this.resourceDeck.shuffle();
    this.goldDeck.shuffle();
    this.objectiveDeck.shuffle();

    try {
      this.resourceCards = new CardPair<>(
        this.drawResourceCardFromDeck(),
        this.drawResourceCardFromDeck()
      );
      this.goldCards = new CardPair<>(
        this.drawGoldCardFromDeck(),
        this.drawGoldCardFromDeck()
      );
      this.objectiveCards = new CardPair<>(
        this.drawObjectiveCardFromDeck(),
        this.drawObjectiveCardFromDeck()
      );
    } catch (EmptyDeckException ignored) {
      // This will never happen as we just seeded the decks
    }
  }

  /**
   * Draws a gold card from the deck
   *
   * @return a gold card drawn from the gold cards deck
   * @throws EmptyDeckException there are no objective cards left in the deck
   */
  public PlayableCard drawGoldCardFromDeck() throws EmptyDeckException {
    return this.goldDeck.draw();
  }

  /**
   * Draws a card from a player's deck pair.
   *
   * @param drawingSource Where we are drawing the card rom
   * @param deckType      The type of deck to draw from.
   * @return The drawn card.
   * @throws EmptyDeckException If the deck being drawn from is empty.
   */
  public PlayableCard drawCard(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) throws EmptyDeckException {
    if (drawingSource == DrawingCardSource.Deck) {
      if (deckType == DrawingDeckType.GOLD) {
        return this.goldDeck.draw();
      } else {
        return this.resourceDeck.draw();
      }
    } else {
      CardPair<PlayableCard> drawingPair;
      Deck<PlayableCard> drawingDeck;
      if (deckType == DrawingDeckType.GOLD) {
        drawingPair = this.goldCards;
        drawingDeck = this.goldDeck;
      } else {
        drawingPair = this.resourceCards;
        drawingDeck = this.resourceDeck;
      }
      if (drawingSource == DrawingCardSource.CardPairFirstCard) {
        return drawingPair.replaceFirst(drawingDeck.draw());
      } else {
        return drawingPair.replaceSecond(drawingDeck.draw());
      }
    }
  }

  /**
   * @return the 2 gold cards
   */
  public CardPair<PlayableCard> getGoldCards() {
    return this.goldCards;
  }

  /**
   * @return the number of gold cards left in the deck
   */
  public int goldCardsLeft() {
    return this.goldDeck.cardsLeft();
  }

  /**
   * Draws a starter card from the deck
   *
   * @return the starterDeck
   * @throws EmptyDeckException there are no objective cards left in the deck
   */
  public PlayableCard drawStarterCardFromDeck() throws EmptyDeckException {
    return this.starterDeck.draw();
  }

  /**
   * @return the number of starter cards left in the deck
   */
  public int starterCardsLeft() {
    return this.starterDeck.cardsLeft();
  }

  /**
   * Draws an objective card from the deck
   *
   * @return the objectiveDeck
   * @throws EmptyDeckException there are no objective cards left in the deck
   */
  public ObjectiveCard drawObjectiveCardFromDeck() throws EmptyDeckException {
    return this.objectiveDeck.draw();
  }

  /**
   * @return the 2 objective cards
   */
  public CardPair<ObjectiveCard> getObjectiveCards() {
    return this.objectiveCards;
  }

  /**
   * Inserts an objective card in the bottom of the deck
   *
   * @param card the card to insert
   */
  public void insertObjectiveCard(ObjectiveCard card) {
    this.objectiveDeck.insert(card);
  }

  /**
   * Inserts a starter card in the bottom of the deck
   *
   * @param card the card to insert
   */
  public void insertStarterCard(PlayableCard card) {
    this.starterDeck.insert(card);
  }

  /**
   * @return the number of objective cards left in the deck
   */
  public int objectiveCardsLeft() {
    return this.objectiveDeck.cardsLeft();
  }

  /**
   * Draws a resource card from the deck
   *
   * @return a resource card drawn from the resource cards deck
   * @throws EmptyDeckException there are no resource cards left in the deck
   */
  public PlayableCard drawResourceCardFromDeck() throws EmptyDeckException {
    return this.resourceDeck.draw();
  }

  /**
   * Draws a resource card from the deck
   *
   * @param n number of cards to extract
   * @return a resource card drawn from the resource cards deck
   * @throws EmptyDeckException there are no resource cards left in the deck
   */
  public List<PlayableCard> drawResourceCardFromDeck(int n)
    throws EmptyDeckException {
    return this.resourceDeck.draw(n);
  }

  /**
   * Draws the first or the second resource card from the game board and
   * replaces it with a new one from the deck
   *
   * @param first if true, the first card is drawn, otherwise the second
   * @return the resource card drawn
   */
  public PlayableCard drawResourceCardFromPair(Boolean first)
    throws EmptyDeckException {
    if (first) {
      return this.resourceCards.replaceFirst(this.drawResourceCardFromDeck());
    } else {
      return this.resourceCards.replaceSecond(this.drawResourceCardFromDeck());
    }
  }

  /**
   * @return the number of resource cards left in the deck
   */
  public int resourceCardsLeft() {
    return this.resourceDeck.cardsLeft();
  }

  /**
   * @return a card pair with objective cards from the deck
   * @throws EmptyDeckException if the objective cards deck is empty
   */
  public CardPair<ObjectiveCard> drawObjectiveCardPair()
    throws EmptyDeckException {
    return new CardPair<>(
      this.drawObjectiveCardFromDeck(),
      this.drawObjectiveCardFromDeck()
    );
  }
}
