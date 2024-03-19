package polimi.ingsw.am21.codex.model.Cards;

import java.util.Collections;
import java.util.List;

public class Deck<T extends  Card> {
    private List<T> cards;

    /**
     * Constructor
     * @param cards list of cards
     */
    Deck(List<T> cards){
        this.cards = cards;
    }

    /**
     * Shuffles the deck
     */
    public void shuffle(){
        Collections.shuffle(this.cards);
    }

    /**
     * Draws a card from the deck
     * @return the card drawn
     */
    public T draw() throws EmptyDeckException {
        if(this.cardsLeft() == 0) throw new EmptyDeckException();
        return this.cards.removeLast();
    }

    /**
     * Draws a card from the deck
     * @param N number of cards to draw
     * @return the card drawn
     */
    public List<T> draw(int N) throws EmptyDeckException {
        if(this.cardsLeft() < N) throw new EmptyDeckException();
        List<T> drawn = this.cards.subList(this.cards.size()-N, this.cards.size());
        this.cards = this.cards.subList(0, this.cards.size()-N);
        return drawn;
    }

    /**
     * Returns the number of cards left in the deck
     * @return the number of cards left in the deck
     */
    public int cardsLeft(){
        return this.cards.size();
    }

    /**
     * Inserts a card in the bottom of the deck
     * @param card the card to insert
     */
    public void insert(T card){
        this.cards.add(0, card);
    }
}
