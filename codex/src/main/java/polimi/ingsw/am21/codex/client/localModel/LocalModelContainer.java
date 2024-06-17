package polimi.ingsw.am21.codex.client.localModel;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.ClientContext;
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
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameAlreadyExistsException;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class LocalModelContainer
  implements GameEventListener, GameErrorListener {

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
  private LocalGameBoard localGameBoard;

  private final View view;

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

  public LocalModelContainer(View view) {
    this.view = view;

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

  public LocalGameBoard getLocalGameBoard() {
    return localGameBoard;
  }

  public LocalLobby getLocalLobby() {
    return lobby;
  }

  @Override
  public void unknownResponse() {
    // TODO use this
    view.postNotification(Notification.UNKNOWN_RESPONSE);
  }

  @Override
  public void gameAlreadyExists(String gameId) {
    view.postNotification(
      NotificationType.ERROR,
      "Game '" + gameId + "' already exists"
    );
    listGames();
  }

  @Override
  public void gameAlreadyStarted() {
    view.postNotification(NotificationType.ERROR, "Game has already started");
  }

  @Override
  public void gameNotStarted() {
    view.postNotification(NotificationType.ERROR, "Game not started");
  }

  public void createGames(
    Set<String> gameIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) {
    // TODO make the server return or send gameEntries?
    if (gameIds.isEmpty()) {
      view.postNotification(NotificationType.WARNING, "No games available");
    } else {
      gameIds.forEach(
        gameId ->
          menu
            .getGames()
            .put(
              gameId,
              new GameEntry(
                gameId,
                currentPlayers.get(gameId),
                maxPlayers.get(gameId)
              )
            )
      );
    }
    listGames();
  }

  public void listGames() {
    //TODO make listGames use GameEntries
    if (state.get().equals(ClientContext.MENU)) {
      view.drawAvailableGames(menu.getGames().values().stream().toList());
    }
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    menu
      .getGames()
      .put(gameId, new GameEntry(gameId, currentPlayers, maxPlayers));
    listGames();
  }

  @Override
  public void gameDeleted(String gameId) {
    // TODO delete game on gameOver
    menu.getGames().remove(gameId);

    if (lobby != null && lobby.getGameId().equals(gameId)) {
      lobby = null;
      localGameBoard = null;
    }

    view.postNotification(
      NotificationType.ERROR,
      "Game " + gameId + " deleted. "
    );

    listGames();
  }

  @Override
  public void lobbyFull(String gameId) {
    view.postNotification(
      NotificationType.ERROR,
      "The game lobby of " + gameId + " is full. "
    );
  }

  @Override
  public void gameNotFound(String gameId) {
    menu.getGames().remove(gameId);
    view.postNotification(
      NotificationType.ERROR,
      "Game " + gameId + " not found. "
    );
  }

  @Override
  public void notInLobby() {
    view.postNotification(NotificationType.ERROR, "You are not in any lobby. ");
  }

  public void showAvailableTokens() {
    getView()
      .postNotification(
        NotificationType.RESPONSE,
        "Available tokens: " +
        this.lobby.getAvailableTokens()
          .stream()
          .map(color -> CliUtils.colorize(color, ColorStyle.NORMAL))
          .collect(Collectors.joining(" "))
      );
  }

  public void listLobbyPlayers() {
    view.drawLobby(lobby.getPlayers());
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
    menu
      .getGames()
      .computeIfPresent(gameId, (gameID, gameEntry) -> {
        gameEntry.setCurrentPlayers(gameEntry.getCurrentPlayers() + 1);
        return gameEntry;
      });

    // Do not draw the lobby in this method, let it be drawn by lobbyInfo
    // This way we prevent an outdated lobby from being drawn

    if (lobby != null && lobby.getGameId().equals(gameId)) {
      addToLobby(socketId);
      view.postNotification(
        NotificationType.UPDATE,
        "Player " + socketId + " joined your game " + gameId
      );
      lobby.getPlayers().put(socketId, new LocalPlayer(socketId));
    } else if (socketId.equals(this.socketId)) {
      state.set(ClientContext.LOBBY);
      getView()
        .postNotification(
          NotificationType.RESPONSE,
          "You joined the lobby of the game: " + gameId
        );

      lobby = new LocalLobby(gameId);
      if (!menu.getGames().containsKey(gameId)) this.listGames();
      localGameBoard = new LocalGameBoard(
        gameId,
        menu.getGames().get(gameId).getMaxPlayers()
      );

      addToLobby(socketId);
      this.getClientContextContainer().set(ClientContext.GAME);
      view.postNotification(
        NotificationType.RESPONSE,
        "You joined the lobby of the game: " + gameId
      );

      getView().drawAvailableTokenColors(lobby.getAvailableTokens());
    } else {
      view.postNotification(
        NotificationType.UPDATE,
        "Player " + socketId + " joined game " + gameId
      );
    }
  }

  /**
   *
   */
  private void addToLobby(UUID socketId) {
    lobby.getPlayers().put(socketId, new LocalPlayer(socketId));
  }

  public void playerLeftLobby() {
    playerLeftLobby(lobby.getGameId(), this.socketId);
  }

  @Override
  public void playerLeftLobby(String gameId, UUID socketID) {
    menu
      .getGames()
      .computeIfPresent(gameId, (gameID, gameEntry) -> {
        gameEntry.setCurrentPlayers(gameEntry.getCurrentPlayers() - 1);
        return gameEntry;
      });

    if (socketID.equals(this.socketId)) {
      lobby = null;
      localGameBoard = null;

      view.postNotification(
        NotificationType.RESPONSE,
        "You left the lobby of the game " + gameId + ". "
      );
    } else {
      lobby.getPlayers().remove(socketID);
      view.postNotification(
        NotificationType.UPDATE,
        "Player" + socketID + " left the game lobby " + gameId + ". "
      );

      lobby.getPlayers().remove(socketID);
      if (state.get().equals(ClientContext.LOBBY)) {
        view.drawLobby(lobby.getPlayers());
      }
    }
  }

  /**
   * Internal utility to set the token, without displaying anything in the view
   */
  private void setPlayerToken(UUID socketId, TokenColor token) {
    Set<TokenColor> availableTokens = lobby.getAvailableTokens();
    availableTokens.remove(token);

    lobby.getPlayers().get(socketId).setToken(token);
  }

  @Override
  public void playerSetToken(
    String gameId,
    UUID socketId,
    String nickname,
    TokenColor token
  ) {
    if (lobby == null || !lobby.getGameId().equals(gameId)) return;

    setPlayerToken(socketId, token);

    if (socketId.equals(this.socketId)) {
      getView()
        .postNotification(
          NotificationType.UPDATE,
          new String[] { "You chose the ", " token. " },
          token,
          2
        );
      getView().drawNicknameChoice();
    } else {
      getView()
        .postNotification(
          NotificationType.UPDATE,
          new String[] {
            Optional.ofNullable(nickname).orElse(socketId.toString()),
            " chose the ",
            " token. ",
          },
          token,
          2
        );
    }

    if (lobby.getPlayers().get(this.socketId).getToken() == null) {
      view.drawAvailableTokenColors(lobby.getAvailableTokens());
    }

    view.drawLobby(lobby.getPlayers());
  }

  @Override
  public void tokenTaken(TokenColor token) {
    lobby.getAvailableTokens().remove(token);
    view.postNotification(
      NotificationType.ERROR,
      new String[] { "The ", " token is already taken" },
      token,
      2
    );
    // TODO only do this if you're in the lobby
    view.drawAvailableTokenColors(lobby.getAvailableTokens());
  }

  /**
   * Internal utility to set the nickname, without displaying anything in the view
   */
  private void setPlayerNickname(UUID socketId, String nickname) {
    lobby.getPlayers().get(socketId).setNickname(nickname);
  }

  @Override
  public void playerSetNickname(String gameId, UUID socketId, String nickname) {
    if (lobby == null || !lobby.getGameId().equals(gameId)) return;

    setPlayerNickname(socketId, nickname);

    if (this.socketId.equals(socketId)) {
      view.postNotification(
        NotificationType.UPDATE,
        "You chose the nickname \"" + nickname + "\""
      );
    } else {
      view.postNotification(
        NotificationType.UPDATE,
        "Player " + socketId + " chose the nickname \"" + nickname + "\""
      );
    }
    view.drawLobby(lobby.getPlayers());
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID socketID,
    String nickname
  ) {
    view.postNotification(
      NotificationType.UPDATE,
      "Player " +
      Optional.ofNullable(nickname).orElse(socketID.toString()) +
      " chose an objective card."
    );
  }

  public CardPair<Card> getAvailableObjectives() {
    return lobby.getAvailableObjectives();
  }

  public void playerChoseObjectiveCard(Boolean isFirst) {
    this.localGameBoard.setSecretObjective(
        isFirst
          ? this.lobby.getAvailableObjectives().getFirst()
          : this.lobby.getAvailableObjectives().getSecond()
      );
    this.view.postNotification(
        NotificationType.RESPONSE,
        "Secret objective chosen. "
      );
  }

  @Override
  public void nicknameTaken(String nickname) {
    view.postNotification(
      NotificationType.ERROR,
      "The nickname " + nickname + " is already taken."
    );
  }

  public void listObjectiveCards(Pair<Integer, Integer> cardIdPair) {
    lobby.setAvailableObjectives(
      cardsLoader.getCardFromId(cardIdPair.getKey()),
      cardsLoader.getCardFromId(cardIdPair.getValue())
    );
    getView().drawObjectiveCardChoice(lobby.getAvailableObjectives());
  }

  public void playerGetStarterCardSides(int cardId) {
    lobby.setStarterCardId(cardId);
    getView()
      .drawStarterCardSides(
        cardsLoader.getCardFromId(lobby.getStarterCardId())
      );
  }

  @Override
  public void notInGame() {
    view.postNotification(NotificationType.ERROR, "You are not in any game. ");
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
    localGameBoard.getPlayers().add(player);

    view.postNotification(
      NotificationType.UPDATE,
      "Player " + nickname + " joined game " + gameId + ". "
    );
  }

  @Override
  public void gameStarted(String gameId, GameInfo gameInfo) {
    if (this.localGameBoard.getGameId().equals(gameId)) {
      localGameBoard.getPlayers().clear();

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
          localGameBoard.setGoldCards(
            CardPair.fromCardIndexPair(cardsLoader, gameInfo.getGoldCards())
          );
          localGameBoard.setObjectiveCards(
            CardPair.fromCardIndexPair(
              cardsLoader,
              gameInfo.getObjectiveCards()
            )
          );
          localGameBoard.setResourceCards(
            CardPair.fromCardIndexPair(cardsLoader, gameInfo.getResourceCards())
          );
          localGameBoard.getPlayers().add(localPlayer);
        });

      localGameBoard.setCurrentPlayerIndex(gameInfo.getCurrentUserIndex());
      for (int i = 0; i < gameInfo.getUsers().size(); ++i) {
        UUID userSocketID = gameInfo.getUsers().get(i).getSocketID();
        if (userSocketID.equals(socketId)) {
          localGameBoard.setPlayerIndex(i);
          break;
        }
      }
      view.postNotification(NotificationType.UPDATE, "The Game has started. ");
      clientContextContainer.set(ClientContext.GAME);
      if (localGameBoard.getCurrentPlayer().getSocketID().equals(socketId)) {
        view.postNotification(NotificationType.UPDATE, "It's your turn. ");
        view.drawGame(localGameBoard.getPlayers());
      } else {
        view.postNotification(
          NotificationType.UPDATE,
          "It's " +
          localGameBoard.getCurrentPlayer().getNickname() +
          "'s turn. "
        );
      }
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
    LocalPlayer localPlayer = localGameBoard.getCurrentPlayer();

    localPlayer.addPlayedCards(card, side, position);

    List<Card> nextHand = new ArrayList<>();
    for (int i = 0; i < localPlayer.getHand().size(); i++) {
      if (i != playerHandCardNumber) {
        nextHand.add(localPlayer.getHand().get(i));
      }
    }
    localPlayer.setHand(nextHand);

    view.postNotification(
      NotificationType.UPDATE,
      "Card " + cardId + " placed"
    );

    view.drawCardPlacement(
      card,
      side,
      position,
      availablePositions,
      forbiddenPositions
    );

    diffMessage(
      newPlayerScore - localGameBoard.getCurrentPlayer().getPoints(),
      "point"
    );

    Arrays.stream(ResourceType.values()).forEach(
      resourceType ->
        diffMessage(
          updatedResources.get(resourceType) -
          localPlayer.getResources().get(resourceType),
          resourceType
        )
    );

    Arrays.stream(ObjectType.values()).forEach(
      objectType ->
        diffMessage(
          updatedObjects.get(objectType) -
          localPlayer.getObjects().get(objectType),
          objectType
        )
    );

    localPlayer.setPoints(newPlayerScore);

    localPlayer.getResources().putAll(updatedResources);
    localPlayer.getObjects().putAll(updatedObjects);

    localPlayer.setAvailableSpots(availablePositions);
    localPlayer.setForbiddenSpots(forbiddenPositions);

    // TODO this actually makes drawCardPlacement redundant
    view.drawPlayerBoard(localPlayer);
    view.drawLeaderBoard(localGameBoard.getPlayers());
    view.drawHand(localPlayer.getHand());
  }

  void diffMessage(int diff, String attributeName) {
    if (diff != 0) {
      view.postNotification(
        NotificationType.UPDATE,
        localGameBoard.getCurrentPlayer().getNickname() +
        (diff > 0 ? "gained" : "lost" + diff) +
        attributeName +
        ((Math.abs(diff) != 1) ? "s" : "") +
        ". "
      );
    }
  }

  void diffMessage(int diff, Colorable colorable) {
    if (diff != 0) {
      view.postNotification(
        NotificationType.UPDATE,
        new String[] {
          localGameBoard.getCurrentPlayer().getNickname(),
          (diff > 0 ? " gained " : " lost " + diff),
          ((Math.abs(diff) != 1) ? "s" : ""),
          ". ",
        },
        colorable,
        2
      );
    }
  }

  @Override
  public void invalidCardPlacement(String reason) {
    view.postNotification(
      NotificationType.ERROR,
      "Invalid card placement: " + reason
    );
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
    Set<Position> forbiddenSpots
  ) {
    if (drawnCardId != null) {
      Card drawnCard = cardsLoader.getCardFromId(drawnCardId);
      localGameBoard.getCurrentPlayer().getHand().add(drawnCard);
    }

    view.drawPlayerBoard(localGameBoard.getCurrentPlayer());

    switch (source) {
      case CardPairFirstCard, CardPairSecondCard -> {
        Card newPairCard = cardsLoader.getCardFromId(newPairCardId);
        CardPair<Card> cardPairToUpdate =
          switch (deck) {
            case GOLD -> localGameBoard.getGoldCards();
            case RESOURCE -> localGameBoard.getResourceCards();
          };

        switch (source) {
          case CardPairFirstCard -> cardPairToUpdate.replaceFirst(newPairCard);
          case CardPairSecondCard -> cardPairToUpdate.replaceSecond(
            newPairCard
          );
        }

        view.drawCardDrawn(deck, newPairCard);
      }
      case Deck -> view.drawCardDrawn(deck);
    }

    view.postNotification(
      NotificationType.UPDATE,
      localGameBoard.getCurrentPlayer().getNickname() +
      " has drawn a card from the " +
      source.toString().toLowerCase() +
      " " +
      deck.toString().toLowerCase() +
      ". "
    );

    changeTurn(
      gameId,
      playerNickname,
      playerIndex,
      isLastRound,
      availableSpots,
      forbiddenSpots
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
    Set<Position> forbiddenSpots
  ) {
    if (isLastRound) {
      view.postNotification(NotificationType.WARNING, "Last round of the game");
    }

    localGameBoard.setCurrentPlayerIndex(playerIndex);
    localGameBoard.getCurrentPlayer().setAvailableSpots(availableSpots);
    localGameBoard.getCurrentPlayer().setForbiddenSpots(forbiddenSpots);
    if (
      localGameBoard.getCurrentPlayer().getSocketID().equals(this.getSocketID())
    ) {
      view.postNotification(NotificationType.UPDATE, "It's your turn. ");
      view.drawPlayerBoard(localGameBoard.getCurrentPlayer());
    } else {
      view.postNotification(
        NotificationType.UPDATE,
        "It's " + localGameBoard.getCurrentPlayer().getNickname() + "'s turn. "
      );
    }
  }

  @Override
  public void gameOver() {
    state.set(ClientContext.GAME_OVER);
    view.drawGameOver(localGameBoard.getPlayers());
  }

  @Override
  public void playerScoresUpdate(Map<String, Integer> newScores) {
    newScores.forEach((nickname, newScore) ->
      localGameBoard
        .getPlayers()
        .stream()
        .filter(player -> player.getNickname().equals(nickname))
        .forEach(player -> {
          int diff = newScore - player.getPoints();
          player.setPoints(newScore);
          diffMessage(diff, "points");
        }));
    view.drawLeaderBoard(localGameBoard.getPlayers());
  }

  @Override
  public void remainingRounds(String gameID, int remainingRounds) {
    if (Objects.equals(localGameBoard.getGameId(), gameID)) {
      if (remainingRounds == 2 || remainingRounds == 1) view.postNotification(
        NotificationType.UPDATE,
        remainingRounds == 2
          ? "The next round will be the last one. "
          : "The last round has started."
      );

      this.localGameBoard.setRemainingRounds(remainingRounds);
    }
  }

  public void gameStatusUpdate(GameState state) {
    view.postNotification(NotificationType.UPDATE, "Game state: " + state);
    //TODO switch on the game STATE
  }

  @Override
  public void winningPlayer(String nickname) {
    view.drawWinner(nickname);
    //TODO Back to lobby
    lobby = null;
    localGameBoard = null;
    listGames();
  }

  @Override
  public void playerConnectionChanged(
    UUID socketID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) {
    view.postNotification(
      NotificationType.UPDATE,
      "Player " +
      Optional.ofNullable(nickname).orElse(socketID.toString()) +
      " is now " +
      status
    );
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
    this.clientContextContainer.set(ClientContext.LOBBY);
  }

  @Override
  public void chatMessage(String gameID, ChatMessage message) {
    if (Objects.equals(gameID, localGameBoard.getGameId())) {
      if (!message.getSender().equals(localGameBoard.getPlayerNickname())) {
        localGameBoard.getChat().postMessage(message);
        view.drawChatMessage(message);
      }
    }
  }

  @Override
  public void playerNotActive() {
    view.postNotification(NotificationType.ERROR, "It's not your turn. ");
  }

  @Override
  public void invalidNextTurnCall() {
    view.postNotification(NotificationType.ERROR, "Invalid next turn call. ");
  }

  @Override
  public void invalidGetObjectiveCardsCall() {
    view.postNotification(
      NotificationType.ERROR,
      "Invalid get objective cards call. "
    );
  }

  @Override
  public void gameNotReady() {
    view.postNotification(NotificationType.ERROR, "Game not ready. ");
  }

  @Override
  public void emptyDeck() {
    view.postNotification(NotificationType.ERROR, "Deck is empty. ");
  }

  @Override
  public void playerNotFound() {
    view.postNotification(NotificationType.ERROR, "Player not found. ");
  }

  @Override
  public void incompleteLobbyPlayer(String msg) {
    view.postNotification(NotificationType.ERROR, msg);
  }

  @Override
  public void illegalCardSideChoice() {
    view.postNotification(NotificationType.ERROR, "Illegal card side choice. ");
  }

  @Override
  public void invalidTokenColor() {
    view.postNotification(NotificationType.ERROR, "Invalid token color");
  }

  @Override
  public void alreadyPlacedCard() {
    view.postNotification(NotificationType.ERROR, "You already placed a card");
  }

  public void handleInvalidActionException(InvalidActionException e) {
    switch (e.getCode()) {
      case PLAYER_NOT_ACTIVE -> this.playerNotActive();
      case NOT_IN_GAME -> this.notInGame();
      case GAME_ALREADY_EXISTS -> this.gameAlreadyExists(
          ((GameAlreadyExistsException) e).getGameID()
        );
      case GAME_ALREADY_STARTED -> this.gameAlreadyStarted();
      case INVALID_NEXT_TURN_CALL -> this.invalidNextTurnCall();
      case INVALID_GET_OBJECTIVE_CARDS_CALL -> this.invalidGetObjectiveCardsCall();
      case GAME_NOT_READY -> this.gameNotReady();
      case GAME_NOT_FOUND -> this.gameNotFound(
          ((GameNotFoundException) e).getGameID()
        );
      case PLAYER_NOT_FOUND -> this.playerNotFound();
      case INCOMPLETE_LOBBY_PLAYER -> this.incompleteLobbyPlayer(
          e.getNotes().get(0)
        );
      case EMPTY_DECK -> this.emptyDeck();
      case ALREADY_PLACED_CARD -> this.alreadyPlacedCard();
      case ILLEGAL_PLACING_POSITION -> this.invalidCardPlacement(
          ((IllegalPlacingPositionException) e).getReason()
        );
      case ILLEGAL_CARD_SIDE_CHOICE -> this.invalidCardPlacement(
          e.getMessage()
        );
      case LOBBY_FULL -> this.lobbyFull(((LobbyFullException) e).getGameID());
      case NICKNAME_ALREADY_TAKEN -> this.nicknameTaken(
          ((NicknameAlreadyTakenException) e).getNickname()
        );
      case INVALID_TOKEN_COLOR -> this.invalidTokenColor();
      case TOKEN_ALREADY_TAKEN -> this.tokenTaken(
          TokenColor.fromString(
            ((TokenAlreadyTakenException) e).getTokenColor()
          )
        );
      case GAME_OVER -> this.gameOver();
    }
  }

  public View getView() {
    return this.view;
  }

  public Optional<String> getGameId() {
    if (lobby != null) {
      return Optional.ofNullable(lobby.getGameId());
    } else {
      return Optional.empty();
    }
  }

  public ClientContext getState() {
    return state.get();
  }
}
