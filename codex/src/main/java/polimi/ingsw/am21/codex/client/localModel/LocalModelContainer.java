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

public class LocalModelContainer implements GameEventListener {

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

  /**
   * A boolean that keeps track of whether the current player has place their card
   * for their turn
   */
  private boolean currentPlayerHasPlacedCard = false;

  public static class ClientContextContainer {

    private ClientContext context;

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
    if (lobby != null) {
      return Optional.ofNullable(lobby.getGameId());
    } else {
      return Optional.empty();
    }
  }

  public LocalModelContainer() {
    cardsLoader.loadCards();
  }

  public void setSocketId(UUID socketId) {
    this.socketId = socketId;
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
      gameBoard = new LocalGameBoard(
        gameId,
        menu.getGames().get(gameId).getMaxPlayers()
      );
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

  @Override
  public void getObjectiveCards(Pair<Integer, Integer> cardIdPair) {
    lobby.setAvailableObjectives(
      cardsLoader.getCardFromId(cardIdPair.getKey()),
      cardsLoader.getCardFromId(cardIdPair.getValue())
    );
  }

  @Override
  public void getStarterCard(Integer cardId) {
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
    // Players are initialized in gameStarted
  }

  @Override
  public void gameStarted(String gameId, GameInfo gameInfo) {
    if (this.gameBoard.getGameId().equals(gameId)) {
      gameBoard.getPlayers().clear();

      clientContextContainer.set(ClientContext.GAME);

      gameInfo
        .getUsers()
        .forEach((GameInfo.GameInfoUser player) -> {
          LocalPlayer localPlayer = lobby
            .getPlayers()
            .get(player.getSocketID());

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
              localPlayer.addPlayedCards(card, cardInfo.getValue(), position);
            });
          localPlayer.setAvailableSpots(player.getAvailableSpots());
          localPlayer.setForbiddenSpots(player.getForbiddenSpots());
          localPlayer.getResources().putAll(player.getResources());
          localPlayer.getObjects().putAll(player.getObjects());

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

      gameBoard.setGoldCards(
        CardPair.fromCardIndexPair(cardsLoader, gameInfo.getGoldCards())
      );
      gameBoard.setObjectiveCards(
        CardPair.fromCardIndexPair(cardsLoader, gameInfo.getObjectiveCards())
      );
      gameBoard.setResourceCards(
        CardPair.fromCardIndexPair(cardsLoader, gameInfo.getResourceCards())
      );

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

    currentPlayerHasPlacedCard = true;
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

    currentPlayerHasPlacedCard = false;
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
  }

  @Override
  public void chatMessage(String gameID, ChatMessage message) {
    if (Objects.equals(gameID, gameBoard.getGameId())) {
      gameBoard.getChat().postMessage(message);
    }
  }

  public boolean currentPlayerHasPlacedCard() {
    return currentPlayerHasPlacedCard;
  }
}
