package polimi.ingsw.am21.codex.model.Lobby;

import java.util.*;
import java.util.stream.Collectors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.*;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.PlayerNotFoundGameException;

/**
 * Represents the lobby where players join before starting the game.
 */
public class Lobby {

  /**
   * The map of players in the lobby, with their socket ID as the key and
   * PlayerBuilder as the value used to progressively create the player;
   */
  private final Map<UUID, Player.PlayerBuilder> lobbyPlayers;

  private final List<Player> finalizedPlayers;

  /**
   * The remaining slots for players in the lobby. ( how many player can
   * still join the game )
   */
  private int remainingPlayerSlots;

  /**
   * Constructs a lobby with the specified maximum number of players.
   *
   * @param maxPlayers the maximum number of players allowed in the lobby
   */
  public Lobby(int maxPlayers) {
    this.remainingPlayerSlots = maxPlayers;
    this.lobbyPlayers = new HashMap<>();
    this.finalizedPlayers = new ArrayList<>();
  }

  /**
   * Constructs a lobby with a default maximum number of players (4).
   */
  public Lobby() {
    this(4);
  }

  /**
   * Gets the number of remaining player slots in the lobby.
   *
   * @return the number of remaining player slots
   */
  public int getRemainingPlayerSlots() {
    return remainingPlayerSlots;
  }

  /**
   * Gets the current count of players in the lobby.
   *
   * @return the count of players in the lobby
   */
  public int getPlayersCount() {
    return lobbyPlayers.size();
  }

  /**
   * Adds a player to the lobby.
   *
   * @param connectionID

 the socket ID of the player to add
   * @throws LobbyFullException.LobbyFullInternalException if the lobby is full and cannot accept more
   *                            players
   */
  public void addPlayer(
    UUID connectionID,
    CardPair<ObjectiveCard> objectiveCards,
    PlayableCard starterCard
  ) throws LobbyFullException.LobbyFullInternalException {
    if (remainingPlayerSlots <= 0) {
      throw new LobbyFullException.LobbyFullInternalException();
    }
    Player.PlayerBuilder playerBuilder = new Player.PlayerBuilder(
      starterCard,
      objectiveCards
    );
    lobbyPlayers.put(connectionID, playerBuilder);

    remainingPlayerSlots--;
  }

  /**
   * Removes a player from the lobby.
   *
   * @param connectionID

 the socket ID of the player to remove
   * @return the extracted objective card associated and the starter card of
   * the player
   * @throws PlayerNotFoundGameException if the player is not found in the lobby
   */
  public Pair<CardPair<ObjectiveCard>, PlayableCard> removePlayer(
    UUID connectionID
  ) throws PlayerNotFoundGameException {
    if (!lobbyPlayers.containsKey(connectionID)) {
      throw new PlayerNotFoundGameException(connectionID);
    }
    PlayableCard starterCard = lobbyPlayers.get(connectionID).getStarterCard();
    CardPair<ObjectiveCard> objectiveCards =
      this.lobbyPlayers.get(connectionID).getObjectiveCards();
    lobbyPlayers.remove(connectionID);
    remainingPlayerSlots++;
    return new Pair<>(objectiveCards, starterCard);
  }

  /**
   * Sets the nickname for a player in the lobby.
   *
   * @param connectionID

 the socket ID of the player
   * @param nickname the nickname to set
   * @throws PlayerNotFoundGameException       if the socket ID is not found in
   *                                       the lobby
   * @throws NicknameAlreadyTakenException if the nickname is already taken
   *                                       by another player
   */
  public void setNickname(UUID connectionID, String nickname)
    throws PlayerNotFoundGameException, NicknameAlreadyTakenException {
    if (!lobbyPlayers.containsKey(connectionID)) {
      throw new PlayerNotFoundGameException(connectionID);
    }
    if (
      lobbyPlayers
        .keySet()
        .stream()
        .anyMatch(
          id ->
            !id.equals(connectionID) &&
            lobbyPlayers
              .get(id)
              .getNickname()
              .map(nick -> nick.equals(nickname))
              .orElse(false)
        ) ||
      finalizedPlayers
        .stream()
        .anyMatch(player -> player.getNickname().equals(nickname))
    ) {
      throw new NicknameAlreadyTakenException(nickname);
    }
    lobbyPlayers.get(connectionID).setNickname(nickname);
  }

  /**
   * Sets the token color for a player in the lobby.
   *
   * @param connectionID

   the socket ID of the player
   * @param tokenColor the token color to set
   * @throws PlayerNotFoundGameException    if the socket ID is not found in the
   *                                    lobby
   * @throws TokenAlreadyTakenException if the token color is already taken
   *                                    by another player
   */
  public void setToken(UUID connectionID, TokenColor tokenColor)
    throws PlayerNotFoundGameException, TokenAlreadyTakenException {
    if (!lobbyPlayers.containsKey(connectionID)) {
      throw new PlayerNotFoundGameException(connectionID);
    }
    if (
      lobbyPlayers
        .keySet()
        .stream()
        .anyMatch(
          id ->
            !id.equals(connectionID) &&
            lobbyPlayers
              .get(id)
              .getTokenColor()
              .map(t -> t.equals(tokenColor))
              .orElse(false)
        ) ||
      finalizedPlayers
        .stream()
        .anyMatch(player -> player.getToken().equals(tokenColor))
    ) {
      throw new TokenAlreadyTakenException(tokenColor);
    }
    lobbyPlayers.get(connectionID).setTokenColor(tokenColor);
  }

  /**
   * Updates the PlayerBuilder with connectionID

 with the selectedObjectiveCard
   *
   * @param connectionID

           the socket ID of the player
   * @param firstObjectiveCard sets the selectedObjectiveCard card to the
   *                           first or second from the CardPair
   * @throws PlayerNotFoundGameException if the socket ID is not found in the lobby
   */
  public void setObjectiveCard(UUID connectionID, Boolean firstObjectiveCard)
    throws PlayerNotFoundGameException {
    if (!lobbyPlayers.containsKey(connectionID)) {
      throw new PlayerNotFoundGameException(connectionID);
    }
    lobbyPlayers.get(connectionID).setObjectiveCard(firstObjectiveCard);
  }

  /**
   * Gets the objective card associated with the player in the lobby.
   *
   * @param connectionID

 the socket ID of the player
   * @return the objective card associated with the player
   * @throws PlayerNotFoundGameException if the player is not found in the lobby
   */
  public Boolean hasSelectedFirstObjectiveCard(UUID connectionID)
    throws PlayerNotFoundGameException {
    if (!lobbyPlayers.containsKey(connectionID)) {
      throw new PlayerNotFoundGameException(connectionID);
    }
    return lobbyPlayers.get(connectionID).hasSelectedObjectiveCard();
  }

  /**
   * Finalizes a player in the lobby, constructing the Player object and
   * removing the PlayerBuilder from the map
   *
   * @param connectionID

 the socket ID of the player to finalize
   * @param cardSide the objective card associated with the player
   * @return the finalized Player object
   * @throws PlayerNotFoundGameException if the socked ID is not found in the lobby
   */
  public Player finalizePlayer(
    UUID connectionID,
    CardSideType cardSide,
    List<PlayableCard> hand
  )
    throws PlayerNotFoundGameException, IncompletePlayerBuilderException, IllegalCardSideChoiceException, IllegalPlacingPositionException {
    if (!lobbyPlayers.containsKey(connectionID)) {
      throw new PlayerNotFoundGameException(connectionID);
    }
    Player.PlayerBuilder playerBuilder = lobbyPlayers.get(connectionID);

    playerBuilder.setStarterCardSide(cardSide);
    playerBuilder.setHand(hand);
    Player player = playerBuilder.build(connectionID);
    lobbyPlayers.remove(connectionID);
    finalizedPlayers.add(player);
    return player;
  }

  /**
   * Retrieves the objective cards associated with a player in the lobby.
   *
   * @param connectionID

 the socket ID of the player
   * @return the CardPair containing the objective cards of the player
   */
  public Optional<CardPair<ObjectiveCard>> getPlayerObjectiveCards(
    UUID connectionID
  ) {
    if (!lobbyPlayers.containsKey(connectionID)) {
      return Optional.empty();
    }
    return Optional.of(lobbyPlayers.get(connectionID).getObjectiveCards());
  }

  /**
   * Checks if a player with the given socket ID is already in the lobby.
   *
   * @param connectionID the socket ID of the player to check
   * @return true if the player is already in the lobby, otherwise false
   */
  public boolean containsConnectionID(UUID connectionID) {
    return lobbyPlayers.containsKey(connectionID);
  }

  /**
   * Gets the nickname of the player in the lobby.
   *
   * @param connectionID

 the socket ID of the player
   * @return the nickname of the player
   * @throws PlayerNotFoundGameException if the player is not found in the lobby
   */
  public Optional<String> getPlayerNickname(UUID connectionID)
    throws PlayerNotFoundGameException {
    if (!lobbyPlayers.containsKey(connectionID)) {
      throw new PlayerNotFoundGameException(connectionID);
    }
    return lobbyPlayers.get(connectionID).getNickname();
  }

  /**
   * Gets the token color of the player in the lobby.
   *
   * @param connectionID

 the socket ID of the player
   * @return the token color of the player
   * @throws PlayerNotFoundGameException if the player is not found in the lobby
   */
  public Optional<TokenColor> getPlayerTokenColor(UUID connectionID)
    throws PlayerNotFoundGameException {
    if (!lobbyPlayers.containsKey(connectionID)) {
      throw new PlayerNotFoundGameException(connectionID);
    }
    return lobbyPlayers.get(connectionID).getTokenColor();
  }

  /**
   * Gets a list of all the available token colors in the lobby
   */
  public Set<TokenColor> getAvailableColors() {
    return Arrays.stream(TokenColor.values())
      .filter(
        color ->
          lobbyPlayers
            .values()
            .stream()
            .noneMatch(
              player ->
                player
                  .getTokenColor()
                  .map(playerColor -> playerColor == color)
                  .orElse(false)
            )
      )
      .collect(Collectors.toSet());
  }

  /**
   * Gets the information of all the players in the lobby.
   *
   * @return a map of the players' socket ID to their nickname and token color
   */
  public Map<UUID, Pair<String, TokenColor>> getPlayersInfo() {
    Map<UUID, Pair<String, TokenColor>> playersInfo = new HashMap<>();
    lobbyPlayers.forEach(
      (connectionID, playerBuilder) ->
        playersInfo.put(
          connectionID,
          new Pair<>(
            playerBuilder.getNickname().orElse(null),
            playerBuilder.getTokenColor().orElse(null)
          )
        )
    );
    return playersInfo;
  }

  /**
   * @param connectionID

 the socked ID of the player
   * @return the starter card of the player
   * @throws PlayerNotFoundGameException if the player is not found in the lobby
   */
  public PlayableCard getStarterCard(UUID connectionID)
    throws PlayerNotFoundGameException {
    if (!lobbyPlayers.containsKey(connectionID)) {
      throw new PlayerNotFoundGameException(connectionID);
    }
    return lobbyPlayers.get(connectionID).getStarterCard();
  }
}
