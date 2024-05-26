package polimi.ingsw.am21.codex.client.localModel;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.localModel.remote.LocalModelGameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.GameErrorListener;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
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

  public LocalModelContainer(UUID socketID, View view) {
    this.view = view;
    this.socketId = socketID;
    // TODO receive socketId from the server

    cardsLoader.loadCards();

    try {
      listener = new LocalModelGameEventListener(this);
    } catch (RemoteException e) {
      // TODO: handle this
      throw new RuntimeException("Failed creating client", e);
    }
  }

  public RemoteGameEventListener getRemoteListener() {
    // TODO implement this in TCP or change location.
    return listener;
  }

  public LocalGameBoard getLocalGameBoard() {
    // TODO remove every usage
    return localGameBoard;
  }

  @Override
  public void unknownResponse() {
    // TODO use this
    view.postNotification(Notification.UNKNOWN_RESPONSE);
  }

  @Override
  public void actionNotAllowed() {
    // TODO use this in RMI
    view.postNotification(NotificationType.WARNING, "Action not allowed");
  }

  @Override
  public void gameAlreadyStarted() {
    // TODO use this in TCP
    view.postNotification(NotificationType.ERROR, "Game has already started");
  }

  @Override
  public void gameNotStarted() {
    // TODO use this
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

  public void gameCreatedAndConnected(
    String gameId,
    UUID gameCreatorId,
    int currentPlayers,
    int maxPlayers
  ) {
    gameCreated(gameId, 1, maxPlayers);

    playerJoinedLobby(
      gameId,
      gameCreatorId,
      Arrays.stream(TokenColor.values()).collect(Collectors.toSet())
    );

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
    view.postNotification(
      NotificationType.ERROR,
      "Game " + gameId + " not found. "
    );
  }

  @Override
  public void notInLobby() {
    view.postNotification(NotificationType.ERROR, "You are not in any lobby. ");
  }

  public Set<TokenColor> getAvailableTokens() {
    //TODO use this in TCP? or remove it in RMI
    return lobby.getAvailableTokens();
  }

  public void loadGameLobby(Map<UUID, Pair<String, TokenColor>> players) {
    //TODO use this in RMI

    players.forEach((uuid, nicknameTokenPair) -> {
      addToLobby(uuid);
      String nickname = nicknameTokenPair.getKey();
      if (nickname != null) {
        playerSetNickname(uuid, nickname);
      }
      TokenColor tokenColor = nicknameTokenPair.getValue();
      if (tokenColor != null) {
        playerSetToken(uuid, tokenColor);
      }
    });
  }

  /**
   * Removes a player slot from the game entry in the menu.
   * Adds the player to your lobby if you have one.
   * Creates a lobby if you join a lobby.
   * @param gameId the id of the game lobby
   * @param socketId the id of the player that joined the lobby
   * @param availableTokenColors the available token colors in the game
   * */
  @Override
  public void playerJoinedLobby(
    String gameId,
    UUID socketId,
    Set<TokenColor> availableTokenColors
  ) {
    //TODO check player joins are not filtered by the server to my lobby.
    //TODO check if tcp and rmi servers send a message / call the methods when a "late" player joins (lobbystatusmessage)
    menu
      .getGames()
      .computeIfPresent(gameId, (gameID, gameEntry) -> {
        gameEntry.setCurrentPlayers(gameEntry.getCurrentPlayers() + 1);
        return gameEntry;
      });

    if (lobby != null && lobby.getGameId().equals(gameId)) {
      addToLobby(socketId);
      view.postNotification(
        NotificationType.UPDATE,
        "Player" + socketId + " joined your game " + gameId
      );
      lobby.getPlayers().put(socketId, new LocalPlayer(socketId));
    } else if (socketId == this.socketId) {
      lobby = new LocalLobby(gameId, availableTokenColors);
      localGameBoard = new LocalGameBoard(
        gameId,
        menu.getGames().get(gameId).getMaxPlayers()
      );
      addToLobby(socketId);

      view.postNotification(
        NotificationType.RESPONSE,
        "You joined the lobby of the game" + gameId
      );
    } else {
      view.postNotification(
        NotificationType.UPDATE,
        "Player " + socketId + " joined game " + gameId
      );
    }
    view.drawLobby(lobby.getPlayers());
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

  public void playerSetToken(UUID socketId, TokenColor color) {
    playerSetToken(lobby.getGameId(), socketId, color);
  }

  public void listTokenColors() {
    view.drawAvailableTokenColors(lobby.getAvailableTokens());
  }

  @Override
  public void playerSetToken(String gameId, UUID socketId, TokenColor token) {
    Set<TokenColor> availableTokens = lobby.getAvailableTokens();
    availableTokens.remove(token);

    lobby.getPlayers().get(socketId).setToken(token);

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
          new String[] { socketId.toString(), " chose the ", " token. " },
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

  private void playerSetNickname(UUID socketId, String nickname) {
    this.playerSetNickname(lobby.getGameId(), socketId, nickname);
  }

  @Override
  public void playerSetNickname(String gameId, UUID socketId, String nickname) {
    lobby.getPlayers().get(socketId).setNickname(nickname);

    if (this.socketId.equals(socketId)) {
      view.postNotification(
        NotificationType.UPDATE,
        "You chose the nickname" + nickname + ". "
      );
    } else {
      view.postNotification(
        NotificationType.UPDATE,
        "Player " + socketId + " chose the nickname" + nickname + ". "
      );
    }
    view.drawLobby(lobby.getPlayers());
  }

  @Override
  public void nicknameTaken(String nickname) {
    view.postNotification(
      NotificationType.ERROR,
      "The nickname " + nickname + " is already taken. "
    );
  }

  public void listObjectiveCards(Pair<Integer, Integer> cardIdPair) {
    //TODO use this in RMI
    lobby.setAvailableObjectives(
      cardsLoader.getCardFromId(cardIdPair.getKey()),
      cardsLoader.getCardFromId(cardIdPair.getValue())
    );
    view.drawObjectiveCardChoice(lobby.getAvailableObjectives());
  }

  @Override
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
    //TODO send a player joined game message for every player already in game if a player joins "late" the lobby (expand joinlobbystatusMessage)

    List<Card> hand = cardsLoader.getCardsFromIds(handIDs);
    lobby.getPlayers().get(socketID).setHand(hand);

    Card starterCard = cardsLoader.getCardFromId(starterCardID);
    lobby
      .getPlayers()
      .get(socketID)
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

    Arrays.stream(ResourceType.values()).forEach(resourceType -> {
      diffMessage(
        updatedResources.get(resourceType) -
        localGameBoard.getCurrentPlayer().getResources().get(resourceType),
        resourceType
      );
    });

    Arrays.stream(ObjectType.values()).forEach(objectType -> {
      diffMessage(
        updatedObjects.get(objectType) -
        localGameBoard.getCurrentPlayer().getResources().get(objectType),
        objectType
      );
    });

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
    newScores.forEach((nickname, newScore) -> {
      localGameBoard
        .getPlayers()
        .stream()
        .filter(player -> player.getNickname().equals(nickname))
        .forEach(player -> {
          int diff = newScore - player.getPoints();
          player.setPoints(newScore);
          diffMessage(diff, "points");
        });
    });
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
  public void playerNotActive() {
    view.postNotification(NotificationType.ERROR, "It's not your turn. ");
  }

  @Override
  public void invalidNextTurnCall() {
    view.postNotification(NotificationType.ERROR, "Invalid next turn call. ");
  }

  @Override
  public void emptyDeck() {
    view.postNotification(NotificationType.ERROR, "Deck is empty. ");
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
