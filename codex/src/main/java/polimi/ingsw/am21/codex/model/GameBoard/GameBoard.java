package polimi.ingsw.am21.codex.model.GameBoard;

import org.json.JSONArray;
import org.json.JSONObject;
import polimi.ingsw.am21.codex.model.Cards.*;

import java.util.*;
import java.util.stream.Collectors;


public class GameBoard {
  final private Deck<PlayableCard> goldDeck;
  private CardPair<PlayableCard> goldCards;
  final private Deck<PlayableCard> starterDeck;
  final private Deck<ObjectiveCard> objectiveDeck;
  private CardPair<ObjectiveCard> objectiveCards;
  final private Deck<PlayableCard> resourceDeck;
  private CardPair<PlayableCard> resourceCards;

  /**
   * @param index the index of the ge
   * @return AdjacentPosition the parsed adjacent position
   */
  private static AdjacentPosition cardGeometryPositionFromJSONIndex(int index) {
    if (index == 0) return CornerPosition.TOP_LEFT;
    if (index == 1) return EdgePosition.TOP;
    if (index == 2) return CornerPosition.TOP_RIGHT;
    if (index == 3) return EdgePosition.CENTER;
    if (index == 4) return CornerPosition.BOTTOM_LEFT;
    if (index == 5) return EdgePosition.BOTTOM;
    if (index == 6) return CornerPosition.BOTTOM_RIGHT;
    throw new RuntimeException("Invalid AdjacentPosition value");
  }

  /**
   * Constructor
   * Initializes the decks using a JSONArray
   */
  public static GameBoard fromJSON(JSONArray cards) {
    List<PlayableCard> goldDeck = new ArrayList<>();
    List<PlayableCard> starterDeck = new ArrayList<>();
    List<ObjectiveCard> objectiveDeck = new ArrayList<>();
    List<PlayableCard> resourceDeck = new ArrayList<>();

    for (int i = 0; i < cards.length(); i++) {
      JSONObject card = cards.getJSONObject(i);
      int id = card.getInt("id");
      String typeStr = card.getString("type");

      CardType type = CardType.fromString(typeStr);
      CardBuilder builder = new CardBuilder(id, type);


      builder.setPoints(card.getInt("points"));
      builder.setObjectiveType(ObjectiveType.fromString(card.getString(
        "objectiveType")));


      JSONArray geometryObjectivesArray = card.getJSONArray(
        "objectiveGeometry");
      Map<AdjacentPosition, ResourceType> objectiveGeometry =
        new HashMap<AdjacentPosition, ResourceType>();

      for (int j = 0; j < geometryObjectivesArray.length(); j++) {
        if (!geometryObjectivesArray.isNull(i)) {
          AdjacentPosition position =
            GameBoard.cardGeometryPositionFromJSONIndex(j);
          ResourceType resource =
            ResourceType.fromString(geometryObjectivesArray.getString(j));
          objectiveGeometry.put(position, resource);
        }
      }

      builder.setObjectiveGeometry(objectiveGeometry);

      Set<String> objectiveResourcesSet = card.getJSONObject(
        "objectiveResources")
        .keySet();
      Map<ResourceType, Integer> objectiveResources = new HashMap<>();
      for (String resourceTypeStr : objectiveResourcesSet) {
        ResourceType resourceType = ResourceType.fromString(resourceTypeStr);
        objectiveResources.put(resourceType, card.getJSONObject(
          "objectiveResources")
          .getInt(resourceTypeStr));
      }
      builder.setObjectiveResources(objectiveResources);

      Map<ObjectType, Integer> objectiveObjects = new HashMap<>();
      Set<String> objectiveObjectsSet = card.getJSONObject("objectiveObjects")
        .keySet();
      for (String objectiveTypeStr : objectiveObjectsSet) {
        ObjectType objectiveType = ObjectType.fromString(objectiveTypeStr);
        objectiveObjects.put(objectiveType, card.getJSONObject(
          "objectiveObjects")
          .getInt(objectiveTypeStr));
      }
      builder.setObjectiveObjects(objectiveObjects);

      if (card.has("backPermanentResources")) {

        List<String> backPermanentResources = new ArrayList<>();
        JSONArray backPermanentResourcesStr = card.getJSONArray(
          "backPermanentResources");
        for (int r = 0; r < backPermanentResourcesStr.length(); r++) {
          backPermanentResources.add(backPermanentResourcesStr.getString(r));
        }
        builder.setBackPermanentResources(backPermanentResources.stream()
          .map(ResourceType::fromString)
          .collect(Collectors.toList()));
      }


      if (card.has("placementCondition")) {
        List<String> placementCondition = new ArrayList<>();
        JSONArray placementConditionStr = card.getJSONArray(
          "placementCondition");
        for (int p = 0; p < placementConditionStr.length(); p++) {
          placementCondition.add(placementConditionStr.getString(p));
        }
        builder.setPlacementCondition(placementCondition.stream()
          .map(ResourceType::fromString)
          .collect(Collectors.toList()));
      }

      if (card.has("pointCondition")) {
        String pointConditionStr = card.getString("pointCondition");
        builder.setPointCondition(PointConditionType.fromString(pointConditionStr));
      }

      if (card.has("pointConditionObject")) {
        ObjectType.fromString(card.getString("pointConditionObject"));
        builder.setPointConditionObject(ObjectType.fromString(card.getString(
          "pointConditionObject")));
      }
    }

    return new GameBoard(goldDeck, resourceDeck, objectiveDeck, starterDeck);

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
  public GameBoard(Deck<PlayableCard> goldDeck,
                   Deck<PlayableCard> resourceDeck,
                   Deck<PlayableCard> starterDeck,
                   Deck<ObjectiveCard> objectiveDeck,
                   CardPair<PlayableCard> resourceCards,
                   CardPair<ObjectiveCard> objectiveCards,
                   CardPair<PlayableCard> goldCards) {
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
  public GameBoard(List<PlayableCard> goldCardsList,
                   List<PlayableCard> starterCardsList,
                   List<ObjectiveCard> objectiveCardsList,
                   List<PlayableCard> resourceCardsList) {
    this.goldDeck = new Deck<>(goldCardsList);
    this.starterDeck = new Deck<>(starterCardsList);
    this.objectiveDeck = new Deck<>(objectiveCardsList);
    this.resourceDeck = new Deck<>(resourceCardsList);
    try {
      this.goldCards = new CardPair<>(this.drawGoldCardFromDeck(),
        this.drawGoldCardFromDeck());
      this.objectiveCards = new CardPair<>(this.drawObjectiveCardFromDeck(),
        this.drawObjectiveCardFromDeck());
      this.resourceCards = new CardPair<>(this.drawResourceCardFromDeck(),
        this.drawResourceCardFromDeck());
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
   * Draws the first or the second gold card from the game board and replaces
   * it with a new one from the deck
   *
   * @param first if true, the first gold card is drawn, otherwise the second
   * @return the gold card drawn
   * @throws EmptyDeckException there are no gold cards left in the deck
   */
  public PlayableCard drawGoldCardFromPair(Boolean first)
  throws EmptyDeckException {
    if (first) {
      return this.goldCards.replaceFirst(this.drawGoldCardFromDeck());
    } else {
      return this.goldCards.replaceSecond(this.drawGoldCardFromDeck());
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
   * Inserts a gold card in the bottom of the deck
   *
   * @param card the card to insert
   */
  public void insertObjectiveCard(ObjectiveCard card) {
    this.objectiveDeck.insert(card);
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
}