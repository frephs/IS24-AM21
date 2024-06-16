package polimi.ingsw.am21.codex.controller;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.exceptions.CardAlreadyPlacedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.Game;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.GameManager;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Lobby.Lobby;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.*;

public class GameController {

  public enum UserGameContextStatus {
    MENU,
    IN_LOBBY,
    IN_GAME,
  }

  public class UserGameContext {

    private String gameId;
    private UserGameContextStatus status;

    private RemoteGameEventListener listener;

    public UserGameContext() {
      this.gameId = null;
      this.status = UserGameContextStatus.MENU;
    }

    public UserGameContext(RemoteGameEventListener listener) {
      this();
      this.listener = listener;
    }

    public UserGameContext(String gameId, UserGameContextStatus status) {
      this.gameId = null;
      this.status = UserGameContextStatus.MENU;
    }

    public UserGameContext(
      String gameId,
      UserGameContextStatus status,
      RemoteGameEventListener listener
    ) {
      this();
      this.listener = listener;
    }

    public RemoteGameEventListener getListener() {
      return listener;
    }

    public void setListener(RemoteGameEventListener listener) {
      this.listener = listener;
    }

    public void setGameId(String gameId, UserGameContextStatus status) {
      this.gameId = gameId;
      this.status = status;
    }

    public void removeGameId() {
      this.gameId = null;
      this.status = UserGameContextStatus.MENU;
    }

    public Optional<String> getGameId() {
      return Optional.ofNullable(gameId);
    }

    public UserGameContextStatus getStatus() {
      return status;
    }
  }

  GameManager manager;

  Map<UUID, UserGameContext> userContexts = new HashMap<>();
  List<RemoteGameEventListener> listeners = new ArrayList<>();

  public GameController() {
    manager = new GameManager();
  }

  public Set<String> getGames() {
    return manager.getGames();
  }

  public Map<String, Integer> getCurrentSlots() {
    return manager.getCurrentSlots();
  }

  public Map<String, Integer> getMaxSlots() {
    return manager.getMaxSlots();
  }

  public Game getGame(String gameId) throws GameNotFoundException {
    return manager.getGame(gameId).orElseThrow(GameNotFoundException::new);
  }

  public void removePlayerFromLobby(Game game, UUID socketID) {
    Lobby lobby = game.getLobby();
    Pair<CardPair<ObjectiveCard>, PlayableCard> oldPlayerCards =
      lobby.removePlayer(socketID);
    game.insertObjectiveCard(oldPlayerCards.getKey().getFirst());
    game.insertObjectiveCard(oldPlayerCards.getKey().getSecond());
    PlayableCard starterCard = oldPlayerCards.getValue();
    starterCard.clearPlayedSide();
    game.insertStarterCard(starterCard);
    userContexts.get(socketID).removeGameId();
  }

  public void removePlayerFromLobby(String gameId, UUID socketID)
    throws GameNotFoundException {
    Game game = this.getGame(gameId);
    this.removePlayerFromLobby(game, socketID);
  }

  public void joinLobby(String gameId, UUID socketID)
    throws GameNotFoundException, LobbyFullException, GameAlreadyStartedException {
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    try {
      if (lobby.containsSocketID(socketID)) {
        this.removePlayerFromLobby(game, socketID);
      }
      lobby.addPlayer(
        socketID,
        game.drawObjectiveCardPair(),
        game.drawStarterCard()
      );
      if (userContexts.containsKey(socketID)) {
        userContexts
          .get(socketID)
          .setGameId(gameId, UserGameContextStatus.IN_LOBBY);
      } else {
        userContexts.put(
          socketID,
          new UserGameContext(gameId, UserGameContextStatus.IN_LOBBY)
        );
      }
      this.getLobbyListeners(gameId)
        .stream()
        .filter(listener -> listener.getValue() != null)
        .forEach(listener -> {
          try {
            listener.getValue().playerJoinedLobby(gameId, socketID);
          } catch (RemoteException e) {
            // TODO: Handle in a better way
            throw new RuntimeException(e);
          }
        });
    } catch (EmptyDeckException e) {
      throw new RuntimeException("EmptyDeckException");
    }
  }

  private List<Pair<UUID, RemoteGameEventListener>> getLobbyListeners(
    String gameId
  ) {
    List<Pair<UUID, RemoteGameEventListener>> pairs = userContexts
      .keySet()
      .stream()
      .filter(
        sID ->
          userContexts.get(sID).getStatus() == UserGameContextStatus.IN_LOBBY &&
          userContexts.get(sID).getGameId().map(gameId::equals).orElse(false)
      )
      .map(sID -> new Pair<>(sID, userContexts.get(sID).getListener()))
      .collect(Collectors.toList());

    listeners
      .stream()
      .map(listener -> new Pair<>(UUID.randomUUID(), listener))
      .forEach(pairs::add);
    return pairs;
  }

  private List<Pair<UUID, RemoteGameEventListener>> getGameListeners(
    String gameID
  ) {
    List<Pair<UUID, RemoteGameEventListener>> pairs = userContexts
      .keySet()
      .stream()
      .filter(
        sID ->
          userContexts.get(sID).getStatus() == UserGameContextStatus.IN_GAME &&
          userContexts.get(sID).getGameId().map(gameID::equals).orElse(false)
      )
      .map(sID -> new Pair<>(sID, userContexts.get(sID).getListener()))
      .collect(Collectors.toList());

    listeners
      .stream()
      .map(listener -> new Pair<>(UUID.randomUUID(), listener))
      .forEach(pairs::add);
    return pairs;
  }

  private List<Pair<UUID, RemoteGameEventListener>> getMenuListeners(
    String gameId
  ) {
    List<Pair<UUID, RemoteGameEventListener>> pairs = userContexts
      .keySet()
      .stream()
      .filter(
        sID ->
          userContexts.get(sID).getGameId().map(gameId::equals).orElse(false)
      )
      .map(sID -> new Pair<>(sID, userContexts.get(sID).getListener()))
      .collect(Collectors.toList());

    listeners
      .stream()
      .map(listener -> new Pair<>(UUID.randomUUID(), listener))
      .forEach(pairs::add);
    return pairs;
  }

  public void lobbySetTokenColor(
    String gameId,
    UUID socketID,
    TokenColor color
  )
    throws GameNotFoundException, TokenAlreadyTakenException, GameAlreadyStartedException {
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    lobby.setToken(socketID, color);
    this.getLobbyListeners(gameId).forEach(listener -> {
        try {
          listener.getValue().playerSetToken(gameId, socketID, color);
        } catch (RemoteException e) {
          // TODO: handle in a better way
          throw new RuntimeException(e);
        }
      });
  }

  public void lobbySetNickname(String gameId, UUID socketID, String nickname)
    throws GameNotFoundException, NicknameAlreadyTakenException, GameAlreadyStartedException {
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    lobby.setNickname(socketID, nickname);
    this.getLobbyListeners(gameId).forEach(listener -> {
        try {
          listener.getValue().playerSetNickname(gameId, socketID, nickname);
        } catch (RemoteException e) {
          // TODO: handle in a better way
          throw new RuntimeException(e);
        }
      });
  }

  public void lobbyChooseObjective(String gameId, UUID socketID, Boolean first)
    throws GameNotFoundException, GameAlreadyStartedException {
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    lobby.setObjectiveCard(socketID, first);
    this.getLobbyListeners(gameId).forEach(listener -> {
        try {
          if (listener.getKey() != socketID) {
            listener
              .getValue()
              .playerChoseObjectiveCard(
                gameId,
                socketID,
                lobby.getPlayerNickname(socketID).orElse(null)
              );
          }
        } catch (RemoteException e) {
          // TODO: handle in a better way
          throw new RuntimeException(e);
        }
      });
  }

  private void startGame(String gameId, Game game)
    throws GameNotReadyException, GameAlreadyStartedException {
    game.start();
    this.getGameListeners(gameId).forEach(listener -> {
        try {
          listener
            .getValue()
            .gameStarted(
              gameId,
              game.getPlayerIds(),
              new Pair<>(
                game.getGameBoard().getResourceCards().getFirst().getId(),
                game.getGameBoard().getResourceCards().getSecond().getId()
              ),
              new Pair<>(
                game.getGameBoard().getGoldCards().getFirst().getId(),
                game.getGameBoard().getGoldCards().getSecond().getId()
              ),
              new Pair<>(
                game.getGameBoard().getObjectiveCards().getFirst().getId(),
                game.getGameBoard().getObjectiveCards().getSecond().getId()
              )
            );
        } catch (RemoteException e) {
          // TODO: handle in a better way
          throw new RuntimeException(e);
        }
      });
  }

  public void startGame(String gameId)
    throws GameNotFoundException, GameNotReadyException, GameAlreadyStartedException {
    Game game = getGame(gameId);
    this.startGame(gameId, game);
  }

  public void joinGame(String gameId, UUID socketID, CardSideType sideType)
    throws GameNotFoundException, IncompletePlayerBuilderException, EmptyDeckException, GameNotReadyException, IllegalCardSideChoiceException, IllegalPlacingPositionException, GameAlreadyStartedException {
    Game game = this.getGame(gameId);
    Player newPlayer = game
      .getLobby()
      .finalizePlayer(socketID, sideType, game.drawHand());
    game.addPlayer(newPlayer);
    this.getGameListeners(gameId).forEach(listener -> {
        try {
          listener
            .getValue()
            .playerJoinedGame(
              gameId,
              socketID,
              newPlayer.getNickname(),
              newPlayer.getToken(),
              newPlayer
                .getBoard()
                .getHand()
                .stream()
                .map(PlayableCard::getId)
                .collect(Collectors.toList()),
              newPlayer.getBoard().getPlayedCards().get(new Position()).getId(),
              newPlayer
                .getBoard()
                .getPlayedCards()
                .get(new Position())
                .getPlayedSideType()
                .orElse(CardSideType.FRONT)
            );
        } catch (RemoteException e) {
          // TODO: handle in a better way
          throw new RuntimeException(e);
        }
      });
    if (game.getPlayersSpotsLeft() == 0) {
      this.startGame(gameId, game);
    }
  }

  public void createGame(String gameId, Integer players)
    throws EmptyDeckException, GameAlreadyExistsException, InvalidGameNameException {
    manager.createGame(gameId, players);

    this.getGameListeners(gameId).forEach(listener -> {
        try {
          listener.getValue().gameCreated(gameId, 0, players);
        } catch (RemoteException e) {
          // TODO: handle in a better way
          throw new RuntimeException(e);
        }
      });
  }

  public Boolean isLastRound(String gameId) throws GameNotFoundException {
    return this.getGame(gameId).isLastRound();
  }

  public void deleteGame(String gameId) {
    manager.deleteGame(gameId);
    this.getGameListeners(gameId).forEach(listener -> {
        try {
          listener.getValue().gameDeleted(gameId);
        } catch (RemoteException e) {
          // TODO: handle in a better way
          throw new RuntimeException(e);
        }
      });
  }

  private void checkIfCurrentPlayer(Game game, String playerNickname)
    throws PlayerNotActive {
    Player player = game.getCurrentPlayer();
    if (
      !player.getNickname().equals(playerNickname)
    ) throw new PlayerNotActive();
  }

  public void nextTurn(String gameId, String playerNickname)
    throws GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException {
    Game game = this.getGame(gameId);
    this.checkIfCurrentPlayer(game, playerNickname);
    game.nextTurn();
    this.getGameListeners(gameId).forEach(listener -> {
        try {
          listener
            .getValue()
            .changeTurn(
              gameId,
              game.getCurrentPlayer().getNickname(),
              game.isLastRound()
            );
        } catch (RemoteException e) {
          // TODO: handle in a better way
          throw new RuntimeException(e);
        }
      });
  }

  public void nextTurn(
    String gameId,
    String playerNickname,
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  )
    throws GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException, EmptyDeckException {
    Game game = this.getGame(gameId);
    this.checkIfCurrentPlayer(game, playerNickname);
    game.nextTurn(
      drawingSource,
      deckType,
      (playerCardId, pairCardId) ->
        this.getGameListeners(gameId).forEach(listener -> {
            try {
              listener
                .getValue()
                .changeTurn(
                  gameId,
                  playerNickname,
                  game.isLastRound(),
                  drawingSource,
                  deckType,
                  playerCardId,
                  pairCardId
                );
            } catch (RemoteException e) {
              // TODO: handle in a better way
              throw new RuntimeException(e);
            }
          })
    );
  }

  public void registerListener(
    UUID socketID,
    RemoteGameEventListener listener
  ) {
    if (!userContexts.containsKey(socketID)) {
      userContexts.put(socketID, new UserGameContext(listener));
    } else {
      userContexts.get(socketID).setListener(listener);
    }
  }

  public void registerGlobalListener(RemoteGameEventListener listener) {
    listeners.add(listener);
  }

  public void unregisterGlobalListener(RemoteGameEventListener listener) {
    listeners.remove(listener);
  }

  public void placeCard(
    String gameId,
    String playerNickname,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  )
    throws GameNotFoundException, PlayerNotActive, IllegalCardSideChoiceException, IllegalPlacingPositionException, CardAlreadyPlacedException {
    Game game = this.getGame(gameId);
    this.checkIfCurrentPlayer(game, playerNickname);
    this.checkIfCardWasPlaced(game, playerNickname);
    Player currentPlayer = game.getCurrentPlayer();
    currentPlayer.toggleCardPlacedThisTurn();
    PlayableCard playedCard = currentPlayer.placeCard(
      playerHandCardNumber,
      side,
      position
    );

    this.getGameListeners(gameId).forEach(listener -> {
        try {
          listener
            .getValue()
            .cardPlaced(
              gameId,
              playerNickname,
              playerHandCardNumber,
              playedCard.getId(),
              side,
              position,
              currentPlayer.getPoints(),
              currentPlayer.getBoard().getResources(),
              currentPlayer.getBoard().getObjects(),
              currentPlayer.getBoard().getAvailableSpots(),
              currentPlayer.getBoard().getForbiddenSpots()
            );
        } catch (RemoteException e) {
          // TODO: handle in a better way
          throw new RuntimeException(e);
        }
      });
  }

  private void checkIfCardWasPlaced(Game game, String playerNickname)
    throws CardAlreadyPlacedException {
    Player player = game.getCurrentPlayer();
    if (player.hasPlacedCardThisTurn()) {
      throw new CardAlreadyPlacedException();
    }
  }

  public void sendChatMessage(String gameId, ChatMessage message)
    throws GameNotFoundException {
    Game game = this.getGame(gameId);
    game.getChat().postMessage(message);
    listeners.forEach(listener -> {
      try {
        listener.chatMessageSent(gameId, message);
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public Set<TokenColor> getAvailableTokens(String gameId)
    throws GameNotFoundException {
    Game game = this.getGame(gameId);
    return game.getLobby().getAvailableColors();
  }
}
