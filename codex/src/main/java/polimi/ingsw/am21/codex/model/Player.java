package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.*;
import java.util.List;

public class Player {
    private final String nickname;
    private PlayerBoard board;
    private int points;
    private TokenColors token;

    /**
     * @param nickname the player's chose nickname, its uni
     * @param hand the hand of cards first drawn by the player
     * @param starterCard the starterCard first drawn by the player which is positioned on the playerboard in (0,0)
     */
    Player(String nickname, List<PlayableCard> hand, PlayableCard starterCard ){
        this.nickname = nickname;
        this.board = new PlayerBoard(hand, starterCard);
        this.points = 0;
    }

    /**
     * @return player's nickname
     */
    public String getNickname(){
        return this.nickname;
    }

    /**
     * @return player's token
     */
    public TokenColors getToken(){
        return this.token;
    }

    /**
     * @param token chosen by the client controller between the available ones
     */
    void setToken(TokenColors token){
        this.token = token;
    }

    /**
     * @param objectiveCard chosen by the client controller between the two given
     */
    void setObjectiveCard(ObjectiveCard objectiveCard){
        board.setObjectiveCard(objectiveCard);
    }

    /**
     * @param card drawn from the GameBoard
     */
    private void drawCard(PlayableCard card){
        board.drawCard(card);
    }

    /**
     * @param card chosen from the player's hand, which will be evaluated
     * @param side of the card chosen to be placed on the PlayerBoard
     * @param position of the PlayerBoard in which the card will be placed by the PlayerBoard
     */
    private void placeCard(PlayableCard card, CardSidesTypes side, Position position){
        board.placeCard(card, side, position);
        this.points += card.evaluate(board);
    }

    /**
     * @param objectiveCard to be evaluated at the end of the game
     */
    private void evaluate(ObjectiveCard objectiveCard){
        this.points += objectiveCard.evaluate(board);
    }


}



