package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.*;
import java.util.List;

public class Player {
    private final String nickname;
    private PlayerBoard board;
    private int points;
    private TokenColors token;

    Player(String nickname, List<PlayableCard> hand, PlayableCard starterCard ){
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
        this.points += card.evaluate(board);
    }

    private void evaluate(ObjectiveCard objectiveCard){
        this.points += objectiveCard.evaluate(board);
    }


}



