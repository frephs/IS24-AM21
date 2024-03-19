package polimi.ingsw.am21.codex.model.GameBoard;

import polimi.ingsw.am21.codex.model.Cards.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Deck;
import polimi.ingsw.am21.codex.model.Cards.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.GoldCard;
import polimi.ingsw.am21.codex.model.Cards.ResourceCard;
import polimi.ingsw.am21.codex.model.Cards.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.StarterCard;

import java.util.List;


public class GameBoard {
    private Deck<GoldCard> goldDeck;
    private CardPair<GoldCard> goldCards;
    private Deck<StarterCard> starterDeck;
    private Deck<ObjectiveCard> objectiveDeck;
    private CardPair<ObjectiveCard> objectiveCards;
    private Deck<ResourceCard> resourceDeck;
    private CardPair<ResourceCard> resourceCards;

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
    public GameBoard(Deck<GoldCard> goldDeck, Deck<ResourceCard> resourceDeck, Deck<StarterCard> starterDeck, Deck<ObjectiveCard> objectiveDeck, CardPair<ResourceCard> resourceCards, CardPair<ObjectiveCard> objectiveCards, CardPair<GoldCard> goldCards) {
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
    public GameBoard(List<GoldCard> goldCardsList, List<StarterCard> starterCardsList, List<ObjectiveCard> objectiveCardsList, List<ResourceCard> resourceCardsList) {
        this.goldDeck = new Deck<GoldCard>(goldCardsList);
        this.starterDeck = new Deck<StarterCard>(starterCardsList);
        this.objectiveDeck = new Deck<ObjectiveCard>(objectiveCardsList);
        this.resourceDeck = new Deck<ResourceCard>(resourceCardsList);
        try {
            this.goldCards = new CardPair<GoldCard>(this.drawGoldCardFromDeck(), this.drawGoldCardFromDeck());
            this.objectiveCards = new CardPair<ObjectiveCard>(this.drawObjectiveCardFromDeck(), this.drawObjectiveCardFromDeck());
            this.resourceCards = new CardPair<ResourceCard>(this.drawResourceCardFromDeck(), this.drawResourceCardFromDeck());
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
    public GoldCard drawGoldCardFromDeck() throws EmptyDeckException {
        return this.goldDeck.draw();
    }

    /**
     * Draws the first or the second gold card from the game board and replaces it with a new one from the deck
     *
     * @param first if true, the first gold card is drawn, otherwise the second
     * @return the gold card drawn
     * @throws EmptyDeckException there are no gold cards left in the deck
     */
    public GoldCard drawGoldCardFromPair(Boolean first) throws EmptyDeckException {
        if (first) {
            return this.goldCards.replaceFirst(this.drawGoldCardFromDeck());
        } else {
            return this.goldCards.replaceSecond(this.drawGoldCardFromDeck());
        }
    }

    /**
     * @return the 2 gold cards
     */
    public CardPair<GoldCard> getGoldCards() {
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
    public StarterCard drawStarterCardFromDeck() throws EmptyDeckException {
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

    // TBF this is probably not needed since you cannot draw multiple cards at once
    // /**
    // * Draws N resource cards from the deck
    //  *
    //  * @param N number of cards to draw
    // * @return N resource cards drawn from the resource cards deck
    // */
    // public List<ResourceCard> drawResourceCardsFromDeck(int N) {
    //     return this.resourceDeck.draw(N);
    // }

    /**
     * Draws a resource card from the deck
     *
     * @return a resource card drawn from the resource cards deck
     * @throws EmptyDeckException there are no resource cards left in the deck
     */
    public ResourceCard drawResourceCardFromDeck() throws EmptyDeckException {
        return this.resourceDeck.draw();
    }

    /**
     * Draws the first or the second resource card from the game board and replaces it with a new one from the deck
     *
     * @param first if true, the first card is drawn, otherwise the second
     * @return the resource card drawn
     */
    public ResourceCard drawResourceCardFromPair(Boolean first) throws EmptyDeckException {
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