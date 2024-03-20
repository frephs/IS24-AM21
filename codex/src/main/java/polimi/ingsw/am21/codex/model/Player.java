package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.*;
import java.util.List;
import polimi.ingsw.am21.codex.model.GameBoard.*;

public class Player {
    private final String nickname;
    private final PlayerBoard board;
    private final TokenColors token;
    private int points;

    Player(PlayerBuilder builder){
        this.nickname = builder.nickname;
        this.token = builder.token;
        this.points = 0;
        this.board = new PlayerBoard(
                builder.cards,
                builder.starterCard,
                builder.objectiveCard
        );
    }

    public static class PlayerBuilder{
        private String nickname;
        private TokenColors token;
        private List<PlayableCard> cards;
        private PlayableCard starterCard;
        private ObjectiveCard objectiveCard;

        /**
         * @param nickname the player's chose nickname, its uni
         */
        public PlayerBuilder nickname(String nickname){
            this.nickname = nickname;
            return this;
        }

        /**
         * @param token chosen by the client controller (physical player)
         */
        public PlayerBuilder TokenColors(TokenColors token){
            this.token = token;
            return this;
        }

        /**
         * @param cards list drawn from the GameBoard
         */
        public PlayerBuilder hand(List<PlayableCard> cards){
            this.cards = cards;
            return this;
        }

        /**
         * @param starterCard drawn from the GameBoard
         */
        public PlayerBuilder starterCard(PlayableCard starterCard){
            this.starterCard = starterCard;
            return this;
        }

        /**
         * @param objectiveCard chosen by the client controller (physical player)
         */
        public PlayerBuilder objectiveCard(ObjectiveCard objectiveCard){
            this.objectiveCard = objectiveCard;
            return this;
        }

        /**
         * @return a functioning player
         */
        public Player build(){
            return new Player(this);
        }
    }



    /**
     * @return player's nickname
     */
    public String getNickname(){
        return this.nickname;
    }

    /**
     * @return player's board
     */
    public PlayerBoard getBoard(){
        return this.board;
    }

    /**
     * @return player's token
     */
    public TokenColors getToken(){
        return this.token;
    }

    /**
     * @return player's points
     */
    public int getPoints() {
        return points;
    }


    /**
     * @param card drawn from the GameBoard which is added to the players hand
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



