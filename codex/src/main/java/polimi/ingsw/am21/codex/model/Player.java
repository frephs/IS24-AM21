package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;

import java.util.List;
import java.util.Optional;

public class Player {
    private final String nickname;
    private final PlayerBoard board;
    private final TokenColor token;
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
        private TokenColor token;
        private List<PlayableCard> cards;
        private PlayableCard starterCard;
        private ObjectiveCard objectiveCard;

        /**
         * @param nickname the player's chose nickname, its uni
         */
        public PlayerBuilder setNickname(String nickname){
            this.nickname = nickname;
            return this;
        }

        /**
         * @return the player nickname color
         */
        public Optional<String> getNickname(){
            return Optional.ofNullable(this.nickname);
        }


        /**
         * @param token chosen by the client controller (physical player)
         */
        public PlayerBuilder setTokenColor(TokenColor token){
            this.token = token;
            return this;
        }

        /**
         * @return the player token color
         */
        public Optional<TokenColor> getTokenColor(){
            return Optional.ofNullable(this.token);
        }

        /**
         * @param cards list drawn from the GameBoard
         */
        public PlayerBuilder setHand(List<PlayableCard> cards){
            this.cards = cards;
            return this;
        }

        /**
         * @param starterCard The starter card drawn from the GameBoard
         * @return the player starter card
         */
        public PlayerBuilder setStarterCard(PlayableCard starterCard){
            this.starterCard = starterCard;
            return this;
        }

        /**
         * @param side chosen by the client controller (physical player)
         */
        public void setStarterCardSide(CardSideType side){
            this.starterCard.setPlayedSideType(side);
        }

        /**
         * @param objectiveCard chosen by the client controller (physical player)
         */
        public PlayerBuilder setObjectiveCard(ObjectiveCard objectiveCard){
            this.objectiveCard = objectiveCard;
            return this;
        }

        /**
         * @return a functioning player
         */
        public Player build(){
                return new Player(this);
        }

        /**
         * @return the player's starter card
         */
        public Optional<PlayableCard> getStarterCard() {
            return Optional.ofNullable(starterCard);
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
    public TokenColor getToken(){
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
    public void drawCard(PlayableCard card){
        board.drawCard(card);
    }

    /**
     * @param card chosen from the player's hand, which will be evaluated
     * @param side of the card chosen to be placed on the PlayerBoard
     * @param position of the PlayerBoard in which the card will be placed by the PlayerBoard
     */
    public void placeCard(PlayableCard card, CardSideType side, Position position){
        board.placeCard(card, side, position);
        this.points += card.getEvaluator().apply(board);
    }

    /**
     * @param objectiveCard to be evaluated at the end of the game
     */
    public void evaluate(ObjectiveCard objectiveCard){
        this.points += objectiveCard.getEvaluator().apply(board);
    }

    /**
     * Evaluates the player secret objective, called by the Game class when Game overs
     * */
    public void evaluateSecretObjective(){
        this.evaluate(
            this.board.getObjectiveCard()
        );
    }
}



