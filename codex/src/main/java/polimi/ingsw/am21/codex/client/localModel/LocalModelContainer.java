package polimi.ingsw.am21.codex.client.localModel;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.localModel.remote.LocalModelGameEventListener;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.GameErrorListener;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class LocalModelContainer
  implements GameEventListener, GameErrorListener {

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

  @Override
  public void unknownResponse() {
    // TODO use this
    view.postNotification(Notification.UNKNOWN_RESPONSE);
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
      listGames();
    }
  }

  public void listGames() {
    //TODO make listGames use GameEntries
    view.drawAvailableGames(menu.getGames().values().stream().toList());
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

    if (socketID == this.socketId) {
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
      view.drawLobby(lobby.getPlayers());
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
    view.drawLobby(lobby.getPlayers());
  }

  @Override
  public void tokenTaken(TokenColor token) {
    lobby.getAvailableTokens().remove(token);
    view.postNotification(
      NotificationType.ERROR,
      new String[] { "The", " token is already taken" },
      token,
      2
    );
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
    view.drawObjectiveCardChoice(lobby.getAvailableObjectives());
  }

  public void playerGetStarterCardSides(int cardId) {
    view.drawStarterCardSides(cardsLoader.getCardFromId(cardId));
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
    List<Card> hand = cardsLoader.getCardsFromIds(handIDs);

    Card starterCard = cardsLoader.getCardFromId(starterCardID);

    if (lobby.getPlayers().get(socketId) == null) lobby
      .getPlayers()
      .put(socketId, new LocalPlayer(socketId));

    LocalPlayer player = lobby.getPlayers().get(socketID);

    player.setHand(hand);
    player.setNickname(nickname);
    player.setToken(color);
    player
      .getPlayedCards()
      .put(new Position(), new Pair<>(starterCard, starterSide));

    localGameBoard.getPlayers().add(lobby.getPlayers().get(socketID));

    view.postNotification(
      NotificationType.UPDATE,
      "Player " + nickname + " joined game " + gameId + ". "
    );

    view.drawLeaderBoard(localGameBoard.getPlayers());
    view.drawPlayerBoards(localGameBoard.getPlayers());
  }

  @Override
  public void gameStarted(String gameId, List<String> players) {
    Map<String, Integer> nicknameToIndex = new HashMap<>();

    if (this.localGameBoard.getGameId().equals(gameId)) {
      localGameBoard
        .getPlayers()
        .sort(
          Comparator.comparingInt(
            player -> players.indexOf(player.getNickname())
          )
        );
    } else {
      view.postNotification(
        NotificationType.UPDATE,
        "Game " + gameId + " started without you"
      );
    }
  }

  @Override
  public void cardPlaced(
    String gameId,
    String playerId,
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
    localGameBoard.getCurrentPlayer().addPlayedCards(card, side, position);

    view.postNotification(NotificationType.UPDATE, "Card" + cardId + " placed");
    view.drawCardPlacement(card, side, position);

    diffMessage(
      newPlayerScore - localGameBoard.getCurrentPlayer().getPoints(),
      "point"
    );

    Arrays.stream(ResourceType.values()).forEach(
      resourceType ->
        diffMessage(
          updatedResources.get(resourceType) -
          localGameBoard.getCurrentPlayer().getResources().get(resourceType),
          resourceType
        )
    );

    Arrays.stream(ObjectType.values()).forEach(
      objectType ->
        diffMessage(
          updatedObjects.get(objectType) -
          localGameBoard.getCurrentPlayer().getObjects().get(objectType),
          objectType
        )
    );

    localGameBoard.getCurrentPlayer().getResources().putAll(updatedResources);
    localGameBoard.getCurrentPlayer().getObjects().putAll(updatedObjects);

    localGameBoard.getCurrentPlayer().setAvailableSpots(availablePositions);
    localGameBoard.getCurrentPlayer().setForbiddenSpots(forbiddenPositions);

    view.drawPlayerBoard(localGameBoard.getCurrentPlayer());
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
          (diff > 0 ? "gained" : "lost" + diff),
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
   * @param playerId is the playerId of the new player
   * */
  @Override
  public void changeTurn(
    String gameId,
    String playerId,
    Boolean isLastRound,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer drawnCardId,
    Integer newPairCardId
  ) {
    Card drawnCard = cardsLoader.getCardFromId(drawnCardId);

    localGameBoard.getCurrentPlayer().getHand().add(drawnCard);
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
      "has drawn a card from the " +
      source.toString().toLowerCase() +
      " " +
      deck.toString().toLowerCase() +
      ". "
    );

    changeTurn(gameId, playerId, isLastRound);
  }

  /**
   * @param playerId is the playerId of the new player
   * */
  @Override
  public void changeTurn(String gameId, String playerId, Boolean isLastRound) {
    if (isLastRound) {
      view.postNotification(NotificationType.WARNING, "Last round of the game");
    }

    view.postNotification(
      NotificationType.UPDATE,
      "It's" + localGameBoard.getCurrentPlayer().getNickname() + "'s turn. "
    );

    localGameBoard.setCurrentPlayer(playerId);
  }

  @Override
  public void gameOver() {
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
  public void remainingTurns(int remainingTurns) {
    view.postNotification(
      NotificationType.UPDATE,
      remainingTurns + "turns left."
    );
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

  public View getView() {
    return this.view;
  }

  public String getGameId() {
    if (lobby != null) {
      return lobby.getGameId();
    } else {
      throw new RuntimeException("You are not in a lobby yet");
    }
  }
}
