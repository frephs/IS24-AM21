package polimi.ingsw.am21.codex.model.Cards.Commons;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Builder.CardBuilder;
import polimi.ingsw.am21.codex.model.Cards.Builder.CardType;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveType;
import polimi.ingsw.am21.codex.model.Cards.Objectives.PointConditionType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;

public final class CardsLoader {

  private final JSONArray cards;
  private boolean loaded = false;
  private final List<PlayableCard> starterCardsList = new ArrayList<>();
  private final List<PlayableCard> resourceCardsList = new ArrayList<>();
  private final List<PlayableCard> goldCardsList = new ArrayList<>();
  private final List<ObjectiveCard> objectiveCardsList = new ArrayList<>();

  private List<Card> cardList = null;

  public CardsLoader() {
    this("/polimi/ingsw/am21/codex/model/Cards/Commons/cards.json");
    this.loadCards();
  }

  /**
   * @param index the geometry card value (number between 0-6)
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
   * Instantiates a new CardsLoader by parsing a JSON file at the given path.
   * @param path Resource path to the JSON file
   */
  public CardsLoader(String path) {
    try {
      InputStream is = getClass().getResourceAsStream(path);
      if (is == null) {
        throw new RuntimeException("Failed loading cards json");
      }
      String content = new BufferedReader(
        new InputStreamReader(is, StandardCharsets.UTF_8)
      )
        .lines()
        .collect(Collectors.joining("\n"));
      this.cards = new JSONArray(content);
    } catch (Exception e) {
      throw new RuntimeException("Failed loading cards json", e);
    }
  }

  /**
   * Instantiates a new CardsLoader by parsing a JSON array.
   * @param cards JSON array containing the cards
   */
  public CardsLoader(JSONArray cards) {
    this.cards = cards;
  }

  /**
   * @param starterCardsList   starter card list where the cards will be added
   * @param resourceCardsList  resource card list where the cards will be added
   * @param goldCardsList      gold cards list where the cards will be added
   * @param objectiveCardsList objective cards list where the cards will be
   *                           added
   */
  public void loadCards(
    List<PlayableCard> starterCardsList,
    List<PlayableCard> resourceCardsList,
    List<PlayableCard> goldCardsList,
    List<ObjectiveCard> objectiveCardsList
  ) {
    this.starterCardsList.clear();
    this.resourceCardsList.clear();
    this.goldCardsList.clear();
    this.objectiveCardsList.clear();
    this.loadCards();
    loaded = true;
    starterCardsList.addAll(this.starterCardsList);
    resourceCardsList.addAll(this.resourceCardsList);
    goldCardsList.addAll(this.goldCardsList);
    objectiveCardsList.addAll(this.objectiveCardsList);
  }

  /**
   * @param ids the ids of the cards to be returned
   * @return the list of cards with the given ids
   */
  public List<Card> getCardsFromIds(List<Integer> ids) {
    List<Card> cards = new ArrayList<>();
    for (int id : ids) {
      cards.add(getCardFromId(id));
    }
    return cards;
  }

  /**
   * @param id the id of the card to be returned
   * @return the card with the given id
   */
  public Card getCardFromId(int id) {
    if (cardList == null) {
      this.cardList = new ArrayList<>();
      cardList.addAll(resourceCardsList);
      cardList.addAll(goldCardsList);
      cardList.addAll(starterCardsList);
      cardList.addAll(objectiveCardsList);
    }
    return cardList
      .stream()
      .filter(c -> c.getId() == id)
      .findFirst()
      .orElseThrow();
  }

  public List<PlayableCard> getStarterCards() {
    return new ArrayList<>(this.starterCardsList);
  }

  public List<PlayableCard> getGoldCards() {
    return new ArrayList<>(this.goldCardsList);
  }

  public List<PlayableCard> getResourceCards() {
    return new ArrayList<>(this.resourceCardsList);
  }

  public List<ObjectiveCard> getObjectiveCards() {
    return new ArrayList<>(this.objectiveCardsList);
  }

  /**
   * Loads the cards from the JSON array. Will not re-run after subsequent calls.
   */
  public void loadCards() {
    if (loaded) return;
    for (int i = 0; i < cards.length(); i++) {
      JSONObject card = cards.getJSONObject(i);
      int id = card.getInt("id");
      String typeStr = card.getString("type");

      CardType type = CardType.fromString(typeStr);
      CardBuilder builder = new CardBuilder(id, type);

      if (card.has("points")) builder.setPoints(card.getInt("points"));
      if (card.has("objectiveType")) builder.setObjectiveType(
        ObjectiveType.fromString(card.getString("objectiveType"))
      );

      if (card.has("objectiveGeometry")) {
        JSONArray geometryObjectivesArray = card.getJSONArray(
          "objectiveGeometry"
        );
        Map<AdjacentPosition, ResourceType> objectiveGeometry = new HashMap<>();

        for (int j = 0; j < geometryObjectivesArray.length(); j++) {
          if (!geometryObjectivesArray.isNull(j)) {
            AdjacentPosition position =
              CardsLoader.cardGeometryPositionFromJSONIndex(j);
            ResourceType resource = ResourceType.fromString(
              geometryObjectivesArray.getString(j)
            );
            objectiveGeometry.put(position, resource);
          }
        }

        builder.setObjectiveGeometry(objectiveGeometry);
      }

      if (card.has("objectiveResources")) {
        Set<String> objectiveResourcesSet = card
          .getJSONObject("objectiveResources")
          .keySet();
        Map<ResourceType, Integer> objectiveResources = new HashMap<>();
        for (String resourceTypeStr : objectiveResourcesSet) {
          ResourceType resourceType = ResourceType.fromString(resourceTypeStr);
          objectiveResources.put(
            resourceType,
            card.getJSONObject("objectiveResources").getInt(resourceTypeStr)
          );
        }
        builder.setObjectiveResources(objectiveResources);
      }

      if (card.has("objectiveObjects")) {
        Map<ObjectType, Integer> objectiveObjects = new HashMap<>();
        Set<String> objectiveObjectsSet = card
          .getJSONObject("objectiveObjects")
          .keySet();
        for (String objectiveTypeStr : objectiveObjectsSet) {
          ObjectType objectiveType = ObjectType.fromString(objectiveTypeStr);
          objectiveObjects.put(
            objectiveType,
            card.getJSONObject("objectiveObjects").getInt(objectiveTypeStr)
          );
        }
        builder.setObjectiveObjects(objectiveObjects);
      }

      if (card.has("backPermanentResources")) {
        List<String> backPermanentResources = new ArrayList<>();
        JSONArray backPermanentResourcesStr = card.getJSONArray(
          "backPermanentResources"
        );
        for (int r = 0; r < backPermanentResourcesStr.length(); r++) {
          backPermanentResources.add(backPermanentResourcesStr.getString(r));
        }
        builder.setBackPermanentResources(
          backPermanentResources
            .stream()
            .map(ResourceType::fromString)
            .collect(Collectors.toList())
        );
      }

      if (card.has("placementCondition")) {
        List<String> placementCondition = new ArrayList<>();
        JSONArray placementConditionStr = card.getJSONArray(
          "placementCondition"
        );
        for (int p = 0; p < placementConditionStr.length(); p++) {
          placementCondition.add(placementConditionStr.getString(p));
        }
        builder.setPlacementCondition(
          placementCondition
            .stream()
            .map(ResourceType::fromString)
            .collect(Collectors.toList())
        );
      }

      if (card.has("pointCondition")) {
        String pointConditionStr = card.getString("pointCondition");
        builder.setPointCondition(
          PointConditionType.fromString(pointConditionStr)
        );
      }

      if (card.has("pointConditionObject")) {
        ObjectType.fromString(card.getString("pointConditionObject"));
        builder.setPointConditionObject(
          ObjectType.fromString(card.getString("pointConditionObject"))
        );
      }

      if (card.has("corners")) {
        CardSideType[] sides = CardSideType.values();
        CornerPosition[] cornerPositions = CornerPosition.values();
        JSONObject cardSidesCorners = card.getJSONObject("corners");
        for (CardSideType side : sides) {
          Map<CornerPosition, Optional<CornerContentType>> corners =
            new HashMap<>();
          String sideStr = side.toString().toLowerCase();
          if (cardSidesCorners.has(sideStr)) {
            JSONObject jsonCorners = cardSidesCorners.getJSONObject(sideStr);
            for (CornerPosition cornerPosition : cornerPositions) {
              String cornerPositionStr = cornerPosition.toString();
              if (jsonCorners.has(cornerPositionStr)) {
                String cornerInfo = jsonCorners.getString(cornerPositionStr);
                if (ResourceType.isResourceType(cornerInfo)) {
                  corners.put(
                    cornerPosition,
                    Optional.of(ResourceType.fromString((cornerInfo)))
                  );
                } else if (ObjectType.isObjectType(cornerInfo)) {
                  corners.put(
                    cornerPosition,
                    Optional.of(ObjectType.fromString(cornerInfo))
                  );
                } else if (cornerInfo.equals("EMPTY")) {
                  corners.put(cornerPosition, Optional.empty());
                } else {
                  throw new RuntimeException(
                    "Invalid corner content type: " + cornerInfo
                  );
                }
              }
            }
          }
          builder.setCorners(side, corners);
        }
      }

      if (type == CardType.OBJECTIVE) {
        ObjectiveCard objectiveCard = builder.buildObjectiveCard();
        this.objectiveCardsList.add(objectiveCard);
      } else {
        PlayableCard playableCard = builder.buildPlayableCard();
        if (type == CardType.GOLD) {
          this.goldCardsList.add(playableCard);
        } else if (type == CardType.RESOURCE) {
          this.resourceCardsList.add(playableCard);
        } else if (type == CardType.STARTER) {
          this.starterCardsList.add(playableCard);
        }
      }
    }
  }
}
