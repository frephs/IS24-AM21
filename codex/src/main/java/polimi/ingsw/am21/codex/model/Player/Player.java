package polimi.ingsw.am21.codex.model.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;
import polimi.ingsw.am21.codex.model.exceptions.AlreadyPlacedCardGameException;

public class Player {

  private final String nickname;
  private final PlayerBoard board;
  private final TokenColor token;
  private int points;
  private final UUID connectionID;
  private Boolean cardPlaced = false;

  Player(PlayerBuilder builder, UUID connectionID)
    throws IllegalCardSideChoiceException, IllegalPlacingPositionException {
    this.nickname = builder.nickname;
    this.token = builder.token;
    this.points = 0;
    this.board = new PlayerBoard(
      builder.cards,
      builder.starterCard,
      builder.getObjectiveCard().orElse(null)
    );
    this.connectionID = connectionID;
  }

  public UUID getConnectionID() {
    return connectionID;
  }

  public static class PlayerBuilder {

    private String nickname;
    private TokenColor token;
    private List<PlayableCard> cards;
    private PlayableCard starterCard;
    private Boolean selectedFirstObjectiveCard;
    private final CardPair<ObjectiveCard> secretObjectives;

    public PlayerBuilder(
      PlayableCard card,
      CardPair<ObjectiveCard> secretObjectives
    ) {
      this.starterCard = card;
      this.secretObjectives = secretObjectives;
    }

    /**
     * @param nickname the player's chose nickname, its uni
     */
    public PlayerBuilder setNickname(String nickname) {
      this.nickname = nickname;
      return this;
    }

    /**
     * @return the player nickname color
     */
    public Optional<String> getNickname() {
      return Optional.ofNullable(this.nickname);
    }

    /**
     * @param token chosen by the client controller (physical player)
     */
    public PlayerBuilder setTokenColor(TokenColor token) {
      this.token = token;
      return this;
    }

    /**
     * @return the player token color
     */
    public Optional<TokenColor> getTokenColor() {
      return Optional.ofNullable(this.token);
    }

    /**
     * @return the player token color
     */
    public Optional<List<PlayableCard>> getHand() {
      return Optional.ofNullable(this.cards);
    }

    /**
     * @param cards list drawn from the GameBoard
     */
    public PlayerBuilder setHand(List<PlayableCard> cards) {
      this.cards = cards;
      return this;
    }

    /**
     * @param starterCard The starter card drawn from the GameBoard
     * @return the player starter card
     */
    public PlayerBuilder setStarterCard(PlayableCard starterCard) {
      this.starterCard = starterCard;
      return this;
    }

    /**
     * @param side chosen by the client controller (physical player)
     */
    public void setStarterCardSide(CardSideType side) {
      this.starterCard.setPlayedSideType(side);
    }

    public CardPair<ObjectiveCard> getObjectiveCards() {
      return secretObjectives;
    }

    public Boolean hasSelectedObjectiveCard() {
      return selectedFirstObjectiveCard != null;
    }

    /**
     * @return the player objective card
     */
    public Optional<ObjectiveCard> getObjectiveCard() {
      return Optional.ofNullable(this.selectedFirstObjectiveCard).map(
        selectedFirstObjectiveCard ->
          selectedFirstObjectiveCard
            ? secretObjectives.getFirst()
            : secretObjectives.getSecond()
      );
    }

    /**
     * @param first true if the player selects the first card in the pair
     */
    public PlayerBuilder setObjectiveCard(Boolean first) {
      ObjectiveCard selectedObjectiveCard;
      this.selectedFirstObjectiveCard = first;
      return this;
    }

    /**
     * @return a functioning player
     */
    public Player build(UUID connectionID)
      throws IncompletePlayerBuilderException, IllegalCardSideChoiceException, IllegalPlacingPositionException {
      IncompletePlayerBuilderException.checkPlayerBuilder(this);
      return new Player(this, connectionID);
    }

    /**
     * @return the player's starter card
     */
    public PlayableCard getStarterCard() {
      return starterCard;
    }
  }

  /**
   * @return player's nickname
   */
  public String getNickname() {
    return this.nickname;
  }

  /**
   * @return player's board
   */
  public PlayerBoard getBoard() {
    return this.board;
  }

  /**
   * @return player's token
   */
  public TokenColor getToken() {
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
  public void drawCard(PlayableCard card) {
    board.drawCard(card);
  }

  /**
   * Asks the PlayerBoard to position the card and then evaluates it
   * @param cardIndex of the card chosen from the player's hand,
   * @param side of the card chosen to be placed on the PlayerBoard
   * @param position of the PlayerBoard in which the card will be placed by the PlayerBoard
   * @throws IndexOutOfBoundsException if the index provided exceeds the player
   * @throws IllegalCardSideChoiceException if the side chosen is not placeable because of the side placing condition
   * @throws IllegalPlacingPositionException if the position provided is either unreachable, occupied or forbidden
   */
  public PlayableCard placeCard(
    int cardIndex,
    CardSideType side,
    Position position
  ) throws InvalidActionException, AlreadyPlacedCardGameException {
    if (cardPlaced) throw new AlreadyPlacedCardGameException();
    PlayableCard playedCard;
    if (cardIndex < 0 || cardIndex >= board.getHand().size()) {
      throw new IllegalPlacingPositionException(
        "You tried to place a card which either doesn't exist or is not in your hand"
      );
    }
    playedCard = board.getHand().get(cardIndex);

    board.placeCard(playedCard, side, position);
    this.points += playedCard.getEvaluator().apply(board);
    cardPlaced = true;
    return playedCard;
  }

  /**
   * Uses the PlayerBoard to evaluate the points of the objective card passed as argument
   * adds the point to the player score.
   * @param objectiveCard to be evaluated at the end of the game
   */
  public void evaluate(ObjectiveCard objectiveCard) {
    this.points += objectiveCard.getEvaluator().apply(board);
  }

  /**
   * Evaluates the player secret objective, called by the Game class when Game overs
   * */
  public void evaluateSecretObjective() {
    this.evaluate(this.board.getObjectiveCard());
  }

  public Boolean getCardPlaced() {
    return cardPlaced;
  }

  public void resetCardPlaced() {
    cardPlaced = false;
  }
}
