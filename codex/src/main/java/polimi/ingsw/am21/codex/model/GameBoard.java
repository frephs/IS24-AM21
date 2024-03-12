package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.SidedCard;

public class GameBoard {
    private CommonBoard;


    /**
     * @requires cardNumber in {0, 1}
     * @ensures a card is returned from either a deck or the common board for the game to give it to the player.
     * */

    public SidedCard drawCard(DrawingSource source, DeckType deck, int cardNumber){
        SidedCard card= new Card();

        switch(source){
            case DECK:
                switch (deck) {
                    case GOLD_DECK:
                        card = goldDeck.draw();
                        break;
                    case RESOURCES_DECK:
                        card = recourceDeck.draw();
                        break;
                }
                break;

            case COMMON_BOARD:
                switch (deck){
                    case GOLD_DECK:
                        card = commonBoard.goldCards.pop(cardNumber);
                        commonBoard.goldCards.insert(gameboard.goldDeck.draw());
                        break;
                    case RESOURCES_DECK:
                        card = commonBoard.resourceCards.pop(cardNumber);
                        commonBoards.goldCards.insert(resourceDeck.draw());
                        break;
                }
                break;
        }
        return card;
    }
}

