package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.*;





public class Player {
    private final String nickname;
    private int points;
    private PlayerBoard board;
    private Token token;

    private void setPoints(int points){
        this.points = points;
    }

    public void getPoints(){
        return this.points;
    }

    Player(String nickname, Token token, SidedCard cards ){
        this.nickname = nickname;
        this.token = token;
        this.board= new PlayerBoard(cards);
    }

    // these are to be used from client, they fit more in the controller me thinks
    private DrawingSource chooseDrawingSource(int source){
        return DrawingSource.values()[source];
    }

    private DeckType chooseDrawingDeck(int deck){
        return DeckType.values()[deck];
    }


    //IDEA game init: queste potremmo implementarle come interfacce che vanno realizzate dal controllore
    private TokenColors choseToken(Tokens remainingTokens, int choice){
        return remainingTokens
    }

    private ObjectiveCard chooseObjectiveCards(ObjectiveCard cards[], int choice){
        return cards[choice];
    }



    // interfacce limitate per il player in modo tale da trovare tutto qui.

    private void drawCard(SidedCard card){
        board.drawCard(card);
    }

    private void placeCard(PlayedCard card, CardSides side, Corner corner){
        board.placeCard(card, side, corner);
    }




}



