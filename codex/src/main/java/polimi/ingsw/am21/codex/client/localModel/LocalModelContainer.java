package polimi.ingsw.am21.codex.client.localModel;

import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.*;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

/**
 * The LocalModelContainer class is the container of the model of the game that is stored locally on the client side to enable client interaction with the server.
 * <br><br>
 * Since the server model has some controller functionality included, we opted to create a mere container local model for the view to draw things properly and keep track of game events client side.
 * <br><br>
 * The status of the game is updated by the server: the client is notified and
 * the local gameboard updated through the client game event handler, which updates the local model and the view.
 * <br><br>
 * It contains the local menu, lobby and game board.
 * @implNote The LocalModelContainer implements GameEventLister to be able to process game events from the server and update the local model for each of them.
 * @see LocalMenu
 * @see LocalLobby
 * @see LocalGameBoard
 * @see polimi.ingsw.am21.codex.client.ClientGameEventHandler
 *
 */
public class LocalModelContainer implements GameEventListener {

  /**
   * Contains the game entries.
   * @see LocalMenu
   * */
  private final LocalMenu menu = new LocalMenu();

  /**
   * Contains the players in the lobby
   * It is an optional since the player may not have joined a lobby yet.
   * @see LocalLobby
   * */
  private Optional<LocalLobby> lobby = Optional.empty();

  /**
   * Contains all the players in the game and the game board.
   * It is an optional since the player may not have joined a game yet.
   * @see LocalGameBoard
   * */
  private Optional<LocalGameBoard> gameBoard = Optional.empty();

  /**
   * Since no cards are sent to the server for security and performance reasons,
   * the client loads the cards from the local file using their unique identifier (their id)
   * A cards loader is used to load the cards from the file from their ID
   * @see CardsLoader
   * */
  private final CardsLoader cardsLoader = new CardsLoader();

  /**
   * The unique identifier of the player's connection.
   */
  private UUID connectionID;

  /**
   * A boolean that keeps track of whether the current player has place their card
   * for their turn
   */
  private boolean currentPlayerHasPlacedCard = false; //TODO maybe refactor this to local gameboard

  /**
   * A class used to store the client context in the localModel,
   * It is queried by the view in case View Updates are received.
   * It is used as follows by:
   * <ul>
   * <li> the GUI to filter scene changing events to only those who matter to the current client context since view updates can be not related to the player's current game or lobby, the client context is need  </li>
   * <li> the CLI to filter available commands by those available in the current context
   * </ul>
   * @see ClientContext
   * */
  public static class ClientContextContainer {

    private ClientContext context;

    /**
     * ClientContext constructor, the default context is obviously the game menu.
     * @see LocalMenu
     * */
    ClientContextContainer() {
      context = ClientContext.MENU;
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
    return lobby.map(LocalLobby::getGameId);
  }

  public LocalModelContainer() {
    cardsLoader.loadCards();
  }

  public void setConnectionID(UUID connectionID) {
    this.connectionID = connectionID;
  }

  public UUID getConnectionID() {
    return connectionID;
  }

  public LocalMenu getLocalMenu() {
    return menu;
  }

  public Optional<LocalLobby> getLocalLobby() {
    return lobby;
  }

  public Optional<LocalGameBoard> getLocalGameBoard() {
    return gameBoard;
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    menu
      .getGames()
      .put(gameId, new GameEntry(gameId, currentPlayers, maxPlayers));
  }

  @Override
  public void refreshLobbies(
    Set<String> lobbyIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) {
    menu.getGames().clear();
    lobbyIds.forEach(
      lobbyId ->
        menu
          .getGames()
          .put(
            lobbyId,
            new GameEntry(
              lobbyId,
              currentPlayers.get(lobbyId),
              maxPlayers.get(lobbyId)
            )
          )
    );
  }

  @Override
  public void gameDeleted(String gameId) {
    // TODO delete game on gameOver
    menu.getGames().remove(gameId);

    if (lobby.map(LocalLobby::getGameId).orElse("").equals(gameId)) {
      lobby = Optional.empty();
      gameBoard = Optional.empty();
    }
  }

  /**
   * Interface method used to process
   * @param gameId The identifier of the lobby that has been filled.
   */
  public void lobbyFull(String gameId) {
    menu
      .getGames()
      .get(gameId)
      .setCurrentPlayers(menu.getGames().get(gameId).getMaxPlayers());
  }

  /**
   * Removes a player slot from the game entry in the menu.
   * Adds the player to your lobby if you have one.
   * Creates a lobby if you join a lobby.
   * @param gameId the id of the game lobby
   * @param connectionID the id of the player that joined the lobby
   * */
  @Override
  public void playerJoinedLobby(String gameId, UUID connectionID) {
    // update menu
    menu
      .getGames()
      .computeIfPresent(gameId, (gameID, gameEntry) -> {
        gameEntry.setCurrentPlayers(gameEntry.getCurrentPlayers() + 1);
        return gameEntry;
      });
    //TOdo what if it's not present?

    // if it's you, create a new lobby
    if (this.connectionID.equals(connectionID)) {
      clientContextContainer.set(ClientContext.LOBBY);
      lobby = Optional.of(new LocalLobby(gameId));
      gameBoard = Optional.of(
        new LocalGameBoard(gameId, menu.getGames().get(gameId).getMaxPlayers())
      );
      addToLobby(connectionID);
    }

    // if it's not you, update lobby if you have one
    if (lobby.map(LocalLobby::getGameId).orElse("").equals(gameId)) {
      addToLobby(connectionID);
    }
  }

  /**
   * Internal utility to add a player to the current lobby
   */
  private void addToLobby(UUID connectionID) {
    lobby
      .orElseThrow()
      .getPlayers()
      .put(connectionID, new LocalPlayer(connectionID));
  }

  @Override
  public void playerConnectionChanged(
    UUID connectionID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) {
    lobby.ifPresent(lobby ->
      lobby
        .getPlayers()
        .computeIfPresent(connectionID, (uuid, player) -> {
          player.setConnectionStatus(status);
          return player;
        }));

    gameBoard.ifPresent(
      gameBoard ->
        gameBoard
          .getPlayers()
          .stream()
          .filter(player -> player.getConnectionID().equals(connectionID))
          .forEach(player -> player.setConnectionStatus(status))
    );
    //TODO check this out
  }

  @Override
  public void playerLeftLobby(String gameId, UUID connectionID) {
    menu
      .getGames()
      .computeIfPresent(gameId, (gameID, gameEntry) -> {
        gameEntry.setCurrentPlayers(gameEntry.getCurrentPlayers() - 1);
        return gameEntry;
      });

    lobby.ifPresent(lobby -> lobby.getPlayers().remove(connectionID));

    if (connectionID.equals(this.connectionID)) {
      lobby = Optional.empty();
      gameBoard = Optional.empty();
      clientContextContainer.set(ClientContext.MENU);
    }
  }

  /**
   * Internal utility to set the token color in the current lobby
   * */
  private void setPlayerToken(UUID connectionID, TokenColor token) {
    lobby.orElseThrow().getPlayers().get(connectionID).setToken(token);
  }

  public void tokenTaken(TokenColor token) {
    lobby.ifPresent(lobby -> lobby.getAvailableTokens().remove(token));
  }

  @Override
  public void playerSetToken(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor token
  ) {
    // return if you're not in a lobby
    if (lobby.isEmpty() || !lobby.get().getGameId().equals(gameId)) return;

    Set<TokenColor> availableTokens = lobby.get().getAvailableTokens();
    availableTokens.remove(token);

    lobby.get().getPlayers().get(connectionID).setToken(token);
  }

  /**
   * Internal utility to set the nickname of a player in the current lobby
   */
  private void setPlayerNickname(UUID connectionID, String nickname) {
    lobby.orElseThrow().getPlayers().get(connectionID).setNickname(nickname);
  }

  @Override
  public void playerSetNickname(
    String gameId,
    UUID connectionID,
    String nickname
  ) {
    if (lobby.isEmpty() || !lobby.get().getGameId().equals(gameId)) return;
    lobby.get().getPlayers().get(connectionID).setNickname(nickname);
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID connectionID,
    String nickname
  ) {}

  /**
   * Gets the objectives the local player can choose from in the lobby
   */
  public CardPair<Card> getAvailableObjectives() {
    return lobby.orElseThrow().getAvailableObjectives();
  }

  public void playerChoseObjectiveCard(Boolean isFirst) {
    this.gameBoard.orElseThrow()
      .setSecretObjective(
        isFirst
          ? this.lobby.orElseThrow().getAvailableObjectives().getFirst()
          : this.lobby.orElseThrow().getAvailableObjectives().getSecond()
      );
  }

  @Override
  public void getObjectiveCards(Pair<Integer, Integer> cardIdPair) {
    lobby
      .orElseThrow()
      .setAvailableObjectives(
        cardsLoader.getCardFromId(cardIdPair.getKey()),
        cardsLoader.getCardFromId(cardIdPair.getValue())
      );
  }

  @Override
  public void getStarterCard(Integer cardId) {
    lobby.orElseThrow().setStarterCard(cardsLoader.getCardFromId(cardId));
  }

  @Override
  public void playerJoinedGame(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCardID,
    CardSideType starterSide
  ) {
    // Players are initialized in gameStarted
  }

  @Override
  public void gameStarted(String gameId, GameInfo gameInfo) {
    if (gameBoard.isPresent() && gameBoard.get().getGameId().equals(gameId)) {
      gameBoard.get().getPlayers().clear();

      clientContextContainer.set(ClientContext.GAME);

      gameInfo
        .getUsers()
        .forEach((GameInfo.GameInfoUser player) -> {
          LocalPlayer localPlayer = lobby
            .orElseThrow()
            .getPlayers()
            .get(player.getConnectionID());

          // Initialize lobby info
          localPlayer.setNickname(player.getNickname());
          localPlayer.setToken(player.getTokenColor());
          localPlayer.setHand(cardsLoader.getCardsFromIds(player.getHandIDs()));
          if (player.getSecretObjectiveCard().isPresent()) {
            localPlayer.setObjectiveCard(
              cardsLoader.getCardFromId(player.getSecretObjectiveCard().get())
            );
          }

          localPlayer.setPoints(localPlayer.getPoints());
          localPlayer.setConnectionStatus(player.getConnectionStatus());

          // Initialize played cards & player board info
          player
            .getPlayedCards()
            .forEach((position, cardInfo) -> {
              PlayableCard card = (PlayableCard) cardsLoader.getCardFromId(
                cardInfo.getKey()
              );
              localPlayer.addPlayedCard(card, cardInfo.getValue(), position);
            });
          localPlayer.setAvailableSpots(player.getAvailableSpots());
          localPlayer.setForbiddenSpots(player.getForbiddenSpots());
          localPlayer.getResources().putAll(player.getResources());
          localPlayer.getObjects().putAll(player.getObjects());

          gameBoard.get().getPlayers().add(localPlayer);
        });

      gameBoard.get().setCurrentPlayerIndex(gameInfo.getCurrentUserIndex());
      for (int i = 0; i < gameInfo.getUsers().size(); ++i) {
        UUID userConnectionID = gameInfo.getUsers().get(i).getConnectionID();
        if (userConnectionID.equals(connectionID)) {
          gameBoard.get().setPlayerIndex(i);
          break;
        }
      }

      gameBoard
        .get()
        .setGoldCards(
          CardPair.fromCardIndexPair(cardsLoader, gameInfo.getGoldCards())
        );
      gameBoard
        .get()
        .setObjectiveCards(
          CardPair.fromCardIndexPair(cardsLoader, gameInfo.getObjectiveCards())
        );
      gameBoard
        .get()
        .setResourceCards(
          CardPair.fromCardIndexPair(cardsLoader, gameInfo.getResourceCards())
        );

      gameBoard
        .get()
        .setResourceDeckTopCard(
          gameInfo
            .getResourceDeckTopCardId()
            .map(id -> (PlayableCard) cardsLoader.getCardFromId(id))
        );
      gameBoard
        .get()
        .setGoldDeckTopCard(
          gameInfo
            .getGoldDeckTopCardId()
            .map(id -> (PlayableCard) cardsLoader.getCardFromId(id))
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
    PlayableCard card = (PlayableCard) cardsLoader.getCardFromId(cardId);
    LocalPlayer localPlayer = gameBoard.orElseThrow().getCurrentPlayer();

    localPlayer.addPlayedCard(card, side, position);

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

    currentPlayerHasPlacedCard = true;
    // TODO this actually makes drawCardPlacement redundant

  }

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
    Optional<Integer> resourceDeckTopCardId,
    Optional<Integer> goldDeckTopCardId
  ) {
    if (drawnCardId != null) {
      Card drawnCard = cardsLoader.getCardFromId(drawnCardId);
      gameBoard.orElseThrow().getCurrentPlayer().getHand().add(drawnCard);
    }

    switch (source) {
      case CardPairFirstCard, CardPairSecondCard -> {
        Card newPairCard = cardsLoader.getCardFromId(newPairCardId);
        CardPair<Card> cardPairToUpdate =
          switch (deck) {
            case GOLD -> gameBoard.orElseThrow().getGoldCards();
            case RESOURCE -> gameBoard.orElseThrow().getResourceCards();
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

  @Override
  public void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Optional<Integer> resourceDeckTopCardId,
    Optional<Integer> goldDeckTopCardId
  ) {
    LocalGameBoard gameBoard = this.gameBoard.orElseThrow();

    gameBoard.setCurrentPlayerIndex(playerIndex);
    gameBoard.getCurrentPlayer().setAvailableSpots(availableSpots);
    gameBoard.getCurrentPlayer().setForbiddenSpots(forbiddenSpots);

    gameBoard.setResourceDeckTopCard(
      resourceDeckTopCardId.map(
        id -> (PlayableCard) cardsLoader.getCardFromId(id)
      )
    );
    gameBoard.setGoldDeckTopCard(
      goldDeckTopCardId.map(id -> (PlayableCard) cardsLoader.getCardFromId(id))
    );

    currentPlayerHasPlacedCard = false;
  }

  @Override
  public void gameOver() {
    clientContextContainer.set(ClientContext.GAME_OVER);
  }

  @Override
  public void playerScoresUpdate(Map<String, Integer> newScores) {
    gameBoard
      .orElseThrow()
      .getPlayers()
      .forEach(player -> player.setPoints(newScores.get(player.getNickname())));
  }

  @Override
  public void remainingRounds(String gameID, int remainingRounds) {
    if (Objects.equals(gameBoard.orElseThrow().getGameId(), gameID)) {
      this.gameBoard.orElseThrow().setRemainingRounds(remainingRounds);
    }
  }

  @Override
  public void winningPlayer(String nickname) {
    lobby = Optional.empty();
    gameBoard = Optional.empty();
  }

  @Override
  public void lobbyInfo(LobbyUsersInfo usersInfo) {
    // Update the current players in the game entry (like for menus)
    this.menu.getGames()
      .get(usersInfo.getGameID())
      .setCurrentPlayers(usersInfo.getUsers().size());

    if (lobby.isPresent()) {
      lobby.get().getPlayers().clear();
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
    }
  }

  @Override
  public void chatMessage(String gameID, ChatMessage message) {
    if (Objects.equals(gameID, gameBoard.orElseThrow().getGameId())) {
      gameBoard.orElseThrow().getChat().postMessage(message);
    }
  }

  /**
   * Gets whether the current player has already placed a card for their turn, and
   * they now have to draw.
   */
  public boolean currentPlayerHasPlacedCard() {
    return currentPlayerHasPlacedCard;
  }
}
