package polimi.ingsw.am21.codex.client.localModel;

import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

/**
 * The class that represents the local player.
 * It's included in the LocalGameBoard class
 * It's used to store the information about the player in the game.
 * It includes the player's nickname, token, points, hand, objective card, resources, objects, available spots, forbidden spots, played cards, objective cards, connection status.
 * @see LocalGameBoard
 * @see GameController.UserGameContext.ConnectionStatus
 *
 * */
public class LocalPlayer {

  /**
   * The unique identifier of the player in the server, associated to their connection.
   * */
  private final UUID connectionID;

  /**
   * The unique identifier of a player in the  game
   * */
  private String nickname;

  /**
   * The chosen token color of the player
   * */
  private TokenColor token;

  /**
   * The points a player has scored in the game to that point.
   * */
  private int points;

  /**
   * The list of playable cards in the player's hand
   * */
  private List<Card> hand;

  /**
   * The player's connection status
   * */
  private GameController.UserGameContext.ConnectionStatus connectionStatus;

  /**
   * The player's chosen secret objective card
   * */
  private Card objectiveCard;

  // this is only present if the player is the current player ( these are the 2 objective cards )
  private CardPair<ObjectiveCard> objectiveCards; //TODO look into this

  /**
   * The game resources the player currently has on the player board
   * */
  private final Map<ResourceType, Integer> resources = new EnumMap<>(
    ResourceType.class
  );

  /**
   * The game objects the player currently has on the player board
   * */
  private final Map<ObjectType, Integer> objects = new EnumMap<>(
    ObjectType.class
  );

  /**
   * The set of positions where the player can place a card
   * */
  private Set<Position> availableSpots = new HashSet<>();

  /**
   * The set of positions where the player can't place a card because of a neighboring card having a disabled angle
   * */
  private Set<Position> forbiddenSpots = new HashSet<>();

  /**
   * The map of the played cards in the game.
   * The key is the position where the card is placed, the value is the pair of the card and the side of the card that is played.
   * @see Position
   * @see PlayableCard
   * @see CardSideType
   * */
  private final Map<
    Position,
    Pair<PlayableCard, CardSideType>
  > playedCardsByPosition = new HashMap<>();
    /**
     * The list of the played cards in the game.
     * The key of the pairs in the listis the position where the card is placed, the value is the pair of the card and the side of the card that is played.
     * @see Position
     * @see PlayableCard
     * @see CardSideType
     */
  private final List<
    Pair<Position, Pair<PlayableCard, CardSideType>>
  > playedCardsByOrder = new ArrayList<>();

  /**
   * Class constructor that initializes the player with the given their connection ID.
   */
  public LocalPlayer(UUID connectionID) {
    this.connectionID = connectionID;

    Arrays.stream(ResourceType.values()).forEach(
      resourceType -> resources.put(resourceType, 0)
    );

    Arrays.stream(ObjectType.values()).forEach(
      objectType -> objects.put(objectType, 0)
    );
  }

  public UUID getConnectionID() {
    return connectionID;
  }

  /**
   * Adds a card to the player's playerboard, to be used when initializing the LocalPlayer
   * @param card The card to add
   * @param side The side that should be shown after the card is placed
   * @param position The model position in which the card has been placed
   */
  public void addPlayedCard(
    PlayableCard card,
    CardSideType side,
    Position position
  ) {
    this.playedCardsByPosition.put(position, new Pair<>(card, side));
    this.playedCardsByOrder.add(new Pair<>(position, new Pair<>(card, side)));
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public TokenColor getToken() {
    return token;
  }

  public void setToken(TokenColor token) {
    this.token = token;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public List<Card> getHand() {
    return hand;
  }

  public void setHand(List<Card> hand) {
    this.hand = hand;
  }

  public Card getObjectiveCard() {
    return objectiveCard;
  }

  public void setObjectiveCard(Card objectiveCard) {
    this.objectiveCard = objectiveCard;
  }

  public Map<ResourceType, Integer> getResources() {
    return resources;
  }

  public Map<ObjectType, Integer> getObjects() {
    return objects;
  }

  public Optional<Set<Position>> getAvailableSpots() {
    return Optional.ofNullable(availableSpots);
  }

  public void setAvailableSpots(Set<Position> availableSpots) {
    this.availableSpots = availableSpots;
  }

  public Optional<Set<Position>> getForbiddenSpots() {
    return Optional.ofNullable(forbiddenSpots);
  }

  public void setForbiddenSpots(Set<Position> forbiddenSpots) {
    this.forbiddenSpots = forbiddenSpots;
  }

  public Map<
    Position,
    Pair<PlayableCard, CardSideType>
  > getPlayedCardsByPosition() {
    return playedCardsByPosition;
  }

  public List<
    Pair<Position, Pair<PlayableCard, CardSideType>>
  > getPlayedCardsByOrder() {
    return playedCardsByOrder;
  }

  public CardPair<ObjectiveCard> getObjectiveCards() {
    return objectiveCards;
  }

  public void setObjectiveCards(CardPair<ObjectiveCard> objectiveCards) {
    this.objectiveCards = objectiveCards;
  }

  public GameController.UserGameContext.ConnectionStatus getConnectionStatus() {
    return connectionStatus;
  }

  public void setConnectionStatus(
    GameController.UserGameContext.ConnectionStatus connectionStatus
  ) {
    this.connectionStatus = connectionStatus;
  }
}
