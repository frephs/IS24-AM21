package polimi.ingsw.am21.codex.model.GameBoard;

import org.json.JSONArray;
import org.json.JSONObject;
import polimi.ingsw.am21.codex.model.Cards.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
     * Constructor
     * Initializes the decks using a JSONArray
     */
    public static GameBoard fromJSON(JSONArray cards) {
        List<PlayableCard> goldDeck = new ArrayList<PlayableCard>();
        List<PlayableCard> starterDeck = new ArrayList<PlayableCard>();
        List<ObjectiveCard> objectiveDeck = new ArrayList<ObjectiveCard>();
        List<PlayableCard> resourceDeck = new ArrayList<PlayableCard>();

        for (int i = 0; i < cards.length(); i++) {
            JSONObject card = cards.getJSONObject(i);
            int id = card.getInt("id");
            String typeStr = card.getString("type");

            CardType type = CardType.fromString(typeStr);
            Card.CardBuilder builder = new Card.CardBuilder(id, type);


            builder.setPoints(card.getInt("points"));
            builder.setCost(card.getInt("cost"));
            builder.setObjectiveType(ObjectiveType.fromString(card.getString("objectiveType")));

            List<List<ResourceType>> geometryObjectives = new ArrayList<List<ResourceType>>();

            JSONArray geometryObjectivesArray = card.getJSONArray("objectiveGeometry");
            for (int j = 0; j < geometryObjectivesArray.length(); j++) {
                List<ResourceType> geometryObjective = new ArrayList<ResourceType>();
                JSONArray geometryObjectiveArray = geometryObjectivesArray.getJSONArray(j);
                for (int k = 0; k < geometryObjectiveArray.length(); k++) {
                    geometryObjective.add(ResourceType.fromString(geometryObjectiveArray.getString(k)));
                }
                geometryObjectives.add(geometryObjective);
            }

            builder.setObjectiveGeometry(geometryObjectives);

            Set<String> objectiveResources = card.getJSONObject("objectiveResources").keySet();
            for (String resourceTypeStr : objectiveResources) {
                ResourceType resourceType = ResourceType.fromString(resourceTypeStr);
                builder.addResourceType(resourceType, card.getJSONObject("objectiveResources").getInt(resourceTypeStr));
            }

            Set<String> objectiveObjects = card.getJSONObject("objectiveObjects").keySet();
            for (String objectiveTypeStr : objectiveObjects) {
                ObjectiveType objectiveType = ObjectiveType.fromString(objectiveTypeStr);
                builder.addObjectiveType(objectiveType, card.getJSONObject("objectiveObjects").getInt(objectiveTypeStr));
            }

            if (card.has("backPermanentResources")) {

                List<String> backPermanentResources = new ArrayList<String>();
                JSONArray backPermanentResourcesStr = card.getJSONArray("backPermanentResources");
                for (int r = 0; r < backPermanentResourcesStr.length(); r++) {
                    backPermanentResources.add(backPermanentResourcesStr.getString(r));
                }
                builder.setBackPermanentResources(backPermanentResources.stream().map(ResourceType::fromString).collect(Collectors.toList()));
            }


            if (card.has("placementCondition")) {
                List<String> placementCondition = new ArrayList<String>();
                JSONArray placementConditionStr = card.getJSONArray("placementCondition");
                for (int p = 0; p < placementConditionStr.length(); p++) {
                    placementCondition.add(placementConditionStr.getString(p));
                }
                builder.setPlacementCondition(placementCondition.stream().map(ResourceType::fromString).collect(Collectors.toList()));
            }

            if(card.has("pointCondition")){
                String pointConditionStr = card.getString("pointCondition");
                builder.setPointCondition(PointConditionType.fromString(pointConditionStr))
            }

            if(card.has("pointConditionObject")){
                ObjectType.from(card.getString("pointConditionObject"));
                builder.setPointConditionObject(ObjectType.from(card.getString("pointConditionObject")));
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
    public GameBoard(Deck<PlayableCard> goldDeck, Deck<PlayableCard> resourceDeck, Deck<PlayableCard> starterDeck, Deck<ObjectiveCard> objectiveDeck, CardPair<PlayableCard> resourceCards, CardPair<ObjectiveCard> objectiveCards, CardPair<PlayableCard> goldCards) {
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
    public GameBoard(List<PlayableCard> goldCardsList, List<PlayableCard> starterCardsList, List<ObjectiveCard> objectiveCardsList, List<PlayableCard> resourceCardsList) {
        this.goldDeck = new Deck<PlayableCard>(goldCardsList);
        this.starterDeck = new Deck<PlayableCard>(starterCardsList);
        this.objectiveDeck = new Deck<ObjectiveCard>(objectiveCardsList);
        this.resourceDeck = new Deck<PlayableCard>(resourceCardsList);
        try {
            this.goldCards = new CardPair<PlayableCard>(this.drawGoldCardFromDeck(), this.drawGoldCardFromDeck());
            this.objectiveCards = new CardPair<ObjectiveCard>(this.drawObjectiveCardFromDeck(), this.drawObjectiveCardFromDeck());
            this.resourceCards = new CardPair<PlayableCard>(this.drawResourceCardFromDeck(), this.drawResourceCardFromDeck());
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
     * Draws the first or the second gold card from the game board and replaces it with a new one from the deck
     *
     * @param first if true, the first gold card is drawn, otherwise the second
     * @return the gold card drawn
     * @throws EmptyDeckException there are no gold cards left in the deck
     */
    public PlayableCard drawGoldCardFromPair(Boolean first) throws EmptyDeckException {
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
    public List<PlayableCard> drawResourceCardFromDeck(int n) throws EmptyDeckException {
        return this.resourceDeck.draw(n);
    }

    /**
     * Draws the first or the second resource card from the game board and replaces it with a new one from the deck
     *
     * @param first if true, the first card is drawn, otherwise the second
     * @return the resource card drawn
     */
    public PlayableCard drawResourceCardFromPair(Boolean first) throws EmptyDeckException {
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