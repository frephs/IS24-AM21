package polimi.ingsw.am21.codex.client.localModel;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.ClientGameEventHandler;
import polimi.ingsw.am21.codex.client.localModel.remote.LocalModelGameEventListener;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.controller.listeners.*;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableBackSide;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class LocalModelContainer implements GameEventListener {

  /**
   * State of the local model
   * */
  AtomicReference<ClientContext> state = new AtomicReference<>(
    ClientContext.MENU
  );

  /**
   * Contains the game entries
   * */
  private final LocalMenu menu = new LocalMenu();

  /**
   * Contains the players in the lobby
   * */
  private LocalLobby lobby;

  /**
   * Contains all the players in the game and the gameboard
   * */
  private LocalGameBoard gameBoard;

  private final CardsLoader cardsLoader = new CardsLoader();

  private UUID socketId;

  private final RemoteGameEventListener listener;

  public class ClientContextContainer {

    private ClientContext context;

    ClientContextContainer() {
      context = null;
    }

    public Optional<ClientContext> get() {
      return Optional.ofNullable(context);
    }

    public void set(ClientContext context) {
      this.context = context;
    }
  }

  private final ClientContextContainer clientContextContainer =
    new ClientContextContainer();

  public ClientContextContainer getClientContextContainer() {
    return clientContextContainer;
  }

  public Optional<String> getGameId() {
    if (lobby != null) {
      return Optional.ofNullable(lobby.getGameId());
    } else {
      return Optional.empty();
    }
  }

  public LocalModelContainer() {
    cardsLoader.loadCards();

    try {
      listener = new LocalModelGameEventListener(this);
    } catch (RemoteException e) {
      // TODO: handle this
      throw new RuntimeException("Failed creating client", e);
    }
  }

  public void setSocketId(UUID socketId) {
    this.socketId = socketId;
  }

  public RemoteGameEventListener getRemoteListener() {
    // TODO implement this in TCP or change location.
    return listener;
  }

  public UUID getSocketID() {
    return socketId;
  }

  public LocalMenu getLocalMenu() {
    return menu;
  }

  public LocalLobby getLocalLobby() {
    return lobby;
  }

  public LocalGameBoard getLocalGameBoard() {
    return gameBoard;
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    menu
      .getGames()
      .put(gameId, new GameEntry(gameId, currentPlayers, maxPlayers));
  }

  @Override
  public void gameDeleted(String gameId) {
    // TODO delete game on gameOver
    menu.getGames().remove(gameId);

    if (lobby != null && lobby.getGameId().equals(gameId)) {
      lobby = null;
      gameBoard = null;
    }
  }

  public void lobbyFull(String gameId) {
    menu
      .getGames()
      .get(gameId)
      .setCurrentPlayers(menu.getGames().get(gameId).getMaxPlayers());
  }

  public void showAvailableTokens() {
    //TODO: H: move this inside view
    //    getView()
    //      .postNotification(
    //        NotificationType.RESPONSE,
    //        "Available tokens: " +
    //        this.lobby.getAvailableTokens()
    //          .stream()
    //          .map(color -> CliUtils.colorize(color, ColorStyle.NORMAL))
    //          .collect(Collectors.joining(" "))
    //      );
  }

  /**
   * Removes a player slot from the game entry in the menu.
   * Adds the player to your lobby if you have one.
   * Creates a lobby if you join a lobby.
   * @param gameId the id of the game lobby
   * @param socketId the id of the player that joined the lobby
   * */
  @Override
  public void playerJoinedLobby(String gameId, UUID socketId) {
    // update menu
    menu
      .getGames()
      .computeIfPresent(gameId, (gameID, gameEntry) -> {
        gameEntry.setCurrentPlayers(gameEntry.getCurrentPlayers() + 1);
        return gameEntry;
      });
    //TOdo what if it's not present?

    // if it's you, create a new lobby
    if (this.socketId.equals(socketId)) {
      clientContextContainer.set(ClientContext.LOBBY);
      lobby = new LocalLobby(gameId);
      addToLobby(socketId);
    }

    // if it's not you, update lobby if you have one,
    if (lobby != null && lobby.getGameId().equals(gameId)) {
      addToLobby(socketId);
    }
  }

  /**
   * Internal utility to add a player to the current lobby
   */
  private void addToLobby(UUID socketId) {
    lobby.getPlayers().put(socketId, new LocalPlayer(socketId));
  }

  @Override
  public void playerConnectionChanged(
    UUID socketID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) {
    if (lobby != null) {
      lobby
        .getPlayers()
        .computeIfPresent(socketID, (uuid, player) -> {
          player.setConnectionStatus(status);
          return player;
        });
    }

    if (gameBoard != null) {
      gameBoard
        .getPlayers()
        .stream()
        .filter(player -> player.getSocketID().equals(socketID))
        .forEach(player -> player.setConnectionStatus(status));
    }
    //TODO check this out
  }

  @Override
  public void playerLeftLobby(String gameId, UUID socketID) {
    menu
      .getGames()
      .computeIfPresent(gameId, (gameID, gameEntry) -> {
        gameEntry.setCurrentPlayers(gameEntry.getCurrentPlayers() - 1);
        return gameEntry;
      });

    if (lobby.getGameId().equals(gameId)) {
      lobby.getPlayers().remove(socketID);
    }

    if (socketID.equals(this.socketId)) {
      lobby = null;
      gameBoard = null;
    }

    if (lobby != null && lobby.getGameId().equals(gameId)) {
      lobby.getPlayers().remove(socketID);
    }
  }

  /**
   * Internal utility to set the token color in the current lobby
   * */
  private void setPlayerToken(UUID socketId, TokenColor token) {
    lobby.getPlayers().get(socketId).setToken(token);
  }

  public void tokenTaken(TokenColor token) {
    lobby.getAvailableTokens().remove(token);
  }

  @Override
  public void playerSetToken(
    String gameId,
    UUID socketId,
    String nickname,
    TokenColor token
  ) {
    // return if you're not in a lobby
    if (lobby == null || !lobby.getGameId().equals(gameId)) return;

    Set<TokenColor> availableTokens = lobby.getAvailableTokens();
    availableTokens.remove(token);

    lobby.getPlayers().get(socketId).setToken(token);
  }

  /**
   * Internal utility to set the nickname of a player in the current lobby
   */
  private void setPlayerNickname(UUID socketId, String nickname) {
    lobby.getPlayers().get(socketId).setNickname(nickname);
  }

  @Override
  public void playerSetNickname(String gameId, UUID socketId, String nickname) {
    if (lobby == null || !lobby.getGameId().equals(gameId)) return;
    lobby.getPlayers().get(socketId).setNickname(nickname);
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID socketID,
    String nickname
  ) {}

  public CardPair<Card> getAvailableObjectives() {
    return lobby.getAvailableObjectives();
  }

  public void playerChoseObjectiveCard(Boolean isFirst) {
    this.gameBoard.setSecretObjective(
        isFirst
          ? this.lobby.getAvailableObjectives().getFirst()
          : this.lobby.getAvailableObjectives().getSecond()
      );
  }

  public void getObjectiveCards(Pair<Integer, Integer> cardIdPair) {
    lobby.setAvailableObjectives(
      cardsLoader.getCardFromId(cardIdPair.getKey()),
      cardsLoader.getCardFromId(cardIdPair.getValue())
    );
  }

  public void playerGetStarterCardSides(int cardId) {
    lobby.setStarterCard(cardsLoader.getCardFromId(cardId));
  }

  @Override
  public void playerJoinedGame(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCardID,
    CardSideType starterSide
  ) {
    if (lobby == null || !lobby.getGameId().equals(gameId)) return;

    if (lobby.getPlayers().get(socketId) == null) lobby
      .getPlayers()
      .put(socketId, new LocalPlayer(socketId));

    LocalPlayer player = lobby.getPlayers().get(socketID);

    player.setNickname(nickname);
    player.setToken(color);
    player.setConnectionStatus(
      GameController.UserGameContext.ConnectionStatus.CONNECTED
    );

    // Initialize hand
    List<Card> hand = cardsLoader.getCardsFromIds(handIDs);
    player.setHand(hand);

    // Initialize played cards
    PlayableCard starterCard = (PlayableCard) cardsLoader.getCardFromId(
      starterCardID
    );
    player
      .getPlayedCards()
      .put(new Position(), new Pair<>(starterCard, starterSide));

    // Initialize resource and object counts ...
    starterCard.setPlayedSideType(starterSide);

    // ... counting the ones in the corners ...
    starterCard
      .getPlayedSide()
      .ifPresent(side ->
        side
          .getCorners()
          .values()
          .forEach(corner ->
            corner
              .getContent()
              .ifPresent(content -> {
                if (ResourceType.has(content)) player.addResource(
                  (ResourceType) content,
                  1
                );
                else if (ObjectType.has(content)) player.addObjects(
                  (ObjectType) content,
                  1
                );
              })));

    // ... and the permanent resources in the back (if the back side is played)
    if (starterSide == CardSideType.BACK) {
      starterCard
        .getPlayedSide()
        .ifPresent(
          side ->
            ((PlayableBackSide) side).getPermanentResources()
              .forEach(resource -> player.addResource(resource, 1))
        );
    }

    // Add the player to the game board
    gameBoard.getPlayers().add(player);
  }

  @Override
  public void gameStarted(String gameId, GameInfo gameInfo) {
    if (this.gameBoard.getGameId().equals(gameId)) {
      gameBoard.getPlayers().clear();

      gameInfo
        .getUsers()
        .forEach((GameInfo.GameInfoUser player) -> {
          LocalPlayer localPlayer = lobby
            .getPlayers()
            .get(player.getSocketID());
          localPlayer.setNickname(player.getNickname());
          localPlayer.setToken(player.getTokenColor());
          localPlayer.setHand(cardsLoader.getCardsFromIds(player.getHandIDs()));
          localPlayer.setPoints(player.getPoints());
          if (player.getSecretObjectiveCard().isPresent()) {
            localPlayer.setObjectiveCard(
              cardsLoader.getCardFromId(player.getSecretObjectiveCard().get())
            );
          }
          localPlayer.setPoints(localPlayer.getPoints());
          localPlayer.setAvailableSpots(player.getAvailableSpots());
          localPlayer.setForbiddenSpots(player.getForbiddenSpots());
          localPlayer.setConnectionStatus(player.getConnectionStatus());
          gameBoard.setGoldCards(
            CardPair.fromCardIndexPair(cardsLoader, gameInfo.getGoldCards())
          );
          gameBoard.setObjectiveCards(
            CardPair.fromCardIndexPair(
              cardsLoader,
              gameInfo.getObjectiveCards()
            )
          );
          gameBoard.setResourceCards(
            CardPair.fromCardIndexPair(cardsLoader, gameInfo.getResourceCards())
          );
          gameBoard.getPlayers().add(localPlayer);
        });

      gameBoard.setCurrentPlayerIndex(gameInfo.getCurrentUserIndex());
      for (int i = 0; i < gameInfo.getUsers().size(); ++i) {
        UUID userSocketID = gameInfo.getUsers().get(i).getSocketID();
        if (userSocketID.equals(socketId)) {
          gameBoard.setPlayerIndex(i);
          break;
        }
      }

      gameBoard.setResourceDeckTopCard(
        (PlayableCard) cardsLoader.getCardFromId(
          gameInfo.getResourceDeckTopCardId()
        )
      );
      gameBoard.setGoldDeckTopCard(
        (PlayableCard) cardsLoader.getCardFromId(
          gameInfo.getGoldDeckTopCardId()
        )
      );
    }
  }

  @Override
  public void cardPlaced(
    String gameId,
    String playerNickname,
    Integer playerHandCardNumber,
    Integer cardId,
    CardSideType side,
    Position position,
    int newPlayerScore,
    Map<ResourceType, Integer> updatedResources,
    Map<ObjectType, Integer> updatedObjects,
    Set<Position> availablePositions,
    Set<Position> forbiddenPositions
  ) {
    Card card = cardsLoader.getCardFromId(cardId);
    LocalPlayer localPlayer = gameBoard.getCurrentPlayer();

    localPlayer.addPlayedCards(card, side, position);

    List<Card> nextHand = new ArrayList<>();
    for (int i = 0; i < localPlayer.getHand().size(); i++) {
      if (i != playerHandCardNumber) {
        nextHand.add(localPlayer.getHand().get(i));
      }
    }
    localPlayer.setHand(nextHand);

    localPlayer.setPoints(newPlayerScore);

    localPlayer.getResources().putAll(updatedResources);
    localPlayer.getObjects().putAll(updatedObjects);

    localPlayer.setAvailableSpots(availablePositions);
    localPlayer.setForbiddenSpots(forbiddenPositions);
    // TODO this actually makes drawCardPlacement redundant

  }

  /**
   * @param playerNickname is the playerNickname of the new player
   * */
  @Override
  public void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer drawnCardId,
    Integer newPairCardId,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
  ) {
    if (drawnCardId != null) {
      Card drawnCard = cardsLoader.getCardFromId(drawnCardId);
      gameBoard.getCurrentPlayer().getHand().add(drawnCard);
    }

    switch (source) {
      case CardPairFirstCard, CardPairSecondCard -> {
        Card newPairCard = cardsLoader.getCardFromId(newPairCardId);
        CardPair<Card> cardPairToUpdate =
          switch (deck) {
            case GOLD -> gameBoard.getGoldCards();
            case RESOURCE -> gameBoard.getResourceCards();
          };

        switch (source) {
          case CardPairFirstCard -> cardPairToUpdate.replaceFirst(newPairCard);
          case CardPairSecondCard -> cardPairToUpdate.replaceSecond(
            newPairCard
          );
        }
      }
    }

    changeTurn(
      gameId,
      playerNickname,
      playerIndex,
      isLastRound,
      availableSpots,
      forbiddenSpots,
      resourceDeckTopCardId,
      goldDeckTopCardId
    );
  }

  /**
   * @param playerNickname is the nickname of the new player
   * */
  @Override
  public void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
  ) {
    gameBoard.setCurrentPlayerIndex(playerIndex);
    gameBoard.getCurrentPlayer().setAvailableSpots(availableSpots);
    gameBoard.getCurrentPlayer().setForbiddenSpots(forbiddenSpots);
    gameBoard.setResourceDeckTopCard(
      (PlayableCard) cardsLoader.getCardFromId(resourceDeckTopCardId)
    );
    gameBoard.setGoldDeckTopCard(
      (PlayableCard) cardsLoader.getCardFromId(goldDeckTopCardId)
    );
  }

  @Override
  public void gameOver() {
    clientContextContainer.set(ClientContext.GAME_OVER);
  }

  @Override
  public void playerScoresUpdate(Map<String, Integer> newScores) {
    gameBoard
      .getPlayers()
      .forEach(player -> player.setPoints(newScores.get(player.getNickname())));
  }

  @Override
  public void remainingRounds(String gameID, int remainingRounds) {
    if (Objects.equals(gameBoard.getGameId(), gameID)) {
      this.gameBoard.setRemainingRounds(remainingRounds);
    }
  }

  @Override
  public void winningPlayer(String nickname) {
    lobby = null;
    gameBoard = null;
  }

  @Override
  public void lobbyInfo(LobbyUsersInfo usersInfo) {
    this.lobby.getPlayers().clear();
    usersInfo
      .getUsers()
      .forEach((uuid, lobbyInfoUser) -> {
        addToLobby(uuid);
        lobbyInfoUser
          .getNickname()
          .ifPresent(nickname -> setPlayerNickname(uuid, nickname));
        lobbyInfoUser
          .getTokenColor()
          .ifPresent(token -> setPlayerToken(uuid, token));
      });
    clientContextContainer.set(ClientContext.LOBBY);
    //TODO what if the player has already joined the game?
  }

  @Override
  public void chatMessage(String gameID, ChatMessage message) {
    if (Objects.equals(gameID, gameBoard.getGameId())) {
      gameBoard.getChat().postMessage(message);
    }
  }
}
