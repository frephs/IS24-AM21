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
  private Optional<LocalLobby> lobby = Optional.empty();

  /**
   * Contains all the players in the game and the gameboard
   * */
  private Optional<LocalGameBoard> gameBoard = Optional.empty();

  private final CardsLoader cardsLoader = new CardsLoader();

  private UUID connectionID;

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
              localPlayer.addPlayedCards(card, cardInfo.getValue(), position);
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
          (PlayableCard) cardsLoader.getCardFromId(
            gameInfo.getResourceDeckTopCardId()
          )
        );
      gameBoard
        .get()
        .setGoldDeckTopCard(
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
    PlayableCard card = (PlayableCard) cardsLoader.getCardFromId(cardId);
    LocalPlayer localPlayer = gameBoard.orElseThrow().getCurrentPlayer();

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
    LocalGameBoard gameBoard = this.gameBoard.orElseThrow();

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

  public boolean currentPlayerHasPlacedCard() {
    return currentPlayerHasPlacedCard;
  }
}
