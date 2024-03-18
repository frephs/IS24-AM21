package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.*;

public class Player {
    private final String nickname;
    private PlayerBoard board;
    private int points;
    private TokenColors token;

    Player(String nickname, TokenColors token, PlayableCard[] hand, PlayableCard starterCard ){
        this.nickname = nickname;
        this.board = new PlayerBoard(hand, starterCard);
        this.points = 0;
    }

    public String getNickname(){
        return this.nickname;
    }

    public TokenColors getToken(){
        return this.token;
    }

    void setToken(TokenColors token){
        this.token = token;
    }

    void setObjectiveCard(ObjectiveCard card){
        board.setObjectiveCard(card);
    }

    private void drawCard(PlayableCard card){
        board.drawCard(card);
    }

    private void placeCard(PlayableCard card, CardSidesTypes side, Position position){
        board.placeCard(card, side, position);
        this.points += board.evaluate(card);
    }

    private void evaluate(ObjectiveCard objectiveCard){
        this.points += board.evaluate(objectiveCard);
    }


}



