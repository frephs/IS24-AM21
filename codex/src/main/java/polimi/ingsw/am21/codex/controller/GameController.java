package polimi.ingsw.am21.codex.controller;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
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
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

public class GameController {

  GameManager manager;
  List<RemoteGameEventListener> listeners;

  public GameController() {
    manager = new GameManager();
    listeners = new ArrayList<>();
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
      listeners.forEach(listener -> {
        try {
          listener.playerJoinedLobby(
            gameId,
            socketID,
            lobby.getAvailableColors()
          );
        } catch (RemoteException e) {
          // TODO: Handle in a better way
          throw new RuntimeException(e);
        }
      });
    } catch (EmptyDeckException e) {
      throw new RuntimeException("EmptyDeckException");
    }
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
    listeners.forEach(listener -> {
      try {
        listener.playerSetToken(gameId, socketID, color);
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
    listeners.forEach(listener -> {
      try {
        listener.playerSetNickname(gameId, socketID, nickname);
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
  }

  private void startGame(String gameId, Game game)
    throws GameNotReadyException, GameAlreadyStartedException {
    game.start();
    listeners.forEach(listener -> {
      try {
        listener.gameStarted(gameId, game.getPlayerIds());
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
    listeners.forEach(listener -> {
      try {
        listener.playerJoinedGame(
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

  public void createGame(String gameId, UUID socketID, Integer players)
    throws EmptyDeckException {
    try {
      Game newGame = manager.createGame(gameId, players);
      newGame
        .getLobby()
        .addPlayer(
          socketID,
          newGame.drawObjectiveCardPair(),
          newGame.drawStarterCard()
        );
    } catch (LobbyFullException ignored) {}

    listeners.forEach(listener -> {
      try {
        listener.gameCreated(gameId, 1, players);
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
    listeners.forEach(listener -> {
      try {
        listener.gameDeleted(gameId);
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
      player.getNickname().equals(playerNickname)
    ) throw new PlayerNotActive();
  }

  public void nextTurn(String gameId, String playerNickname)
    throws GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException {
    Game game = this.getGame(gameId);
    this.checkIfCurrentPlayer(game, playerNickname);
    game.nextTurn();
    listeners.forEach(listener -> {
      try {
        listener.changeTurn(
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
        listeners.forEach(listener -> {
          try {
            listener.changeTurn(
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

  public void addListener(RemoteGameEventListener listener) {
    listeners.add(listener);
  }

  public void removeListener(RemoteGameEventListener listener) {
    listeners.remove(listener);
  }

  public void placeCard(
    String gameId,
    String playerNickname,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  )
    throws GameNotFoundException, PlayerNotActive, IllegalCardSideChoiceException, IllegalPlacingPositionException {
    Game game = this.getGame(gameId);
    this.checkIfCurrentPlayer(game, playerNickname);
    Player currentPlayer = game.getCurrentPlayer();
    PlayableCard playedCard = currentPlayer.placeCard(
      playerHandCardNumber,
      side,
      position
    );
    listeners.forEach(listener -> {
      try {
        listener.cardPlaced(
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

  public Set<TokenColor> getAvailableTokens(String gameId)
    throws GameNotFoundException {
    Game game = this.getGame(gameId);
    return game.getLobby().getAvailableColors();
  }
}
