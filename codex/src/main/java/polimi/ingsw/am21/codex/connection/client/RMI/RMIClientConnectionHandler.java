package polimi.ingsw.am21.codex.connection.client.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.UUID;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.server.RMI.RMIConnectionHandler;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

public class RMIClientConnectionHandler
  implements Remote, ClientConnectionHandler {

  private LocalPlayer localPlayer;
  private final LocalModelContainer localModelContainer;
  private final RMIConnectionHandler rmiConnectionHandler;
  private final UUID index;

  public RMIClientConnectionHandler(RMIConnectionHandler rmiConnectionHandler) {
    this.rmiConnectionHandler = rmiConnectionHandler;
    index = UUID.randomUUID();
    // TODO
    this.localModelContainer = null;
  }

  @Override
  public void connect() {
    // TODO
    //    localModelContainer = new LocalModelContainer(this.index, new );
  }

  @Override
  public void disconnect() {
    // TODO
  }

  @Override
  public void listGames() {
    try {
      rmiConnectionHandler.getGames();
      //TODO add method to modify the local model
    } catch (RemoteException e) {
      System.err.println("A remote exception occurred" + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void connectToGame(String gameId) {
    try {
      rmiConnectionHandler.joinLobby(gameId, this.index);
      localModelContainer.playerJoinedLobby(gameId, this.index);
    } catch (GameAlreadyStartedException e) {
      System.err.println("The game already started" + e.getMessage());
      localModelContainer.playerLeftLobby(gameId, this.index);
      e.printStackTrace();
    } catch (LobbyFullException e) {
      System.err.println("The lobby is full" + e.getMessage());
      localModelContainer.playerLeftLobby(gameId, this.index);
      e.printStackTrace();
    } catch (GameNotFoundException e) {
      System.err.println("The game was not found" + e.getMessage());
    } catch (RemoteException e) {
      System.err.println("A remote exception occurred" + e.getMessage());
    }
  }

  @Override
  public void leaveGameLobby() {
    // TODO
  }

  @Override
  public void createAndConnectToGame(String gameId, int numberPlayers)
    throws EmptyDeckException, RemoteException, LobbyFullException, GameNotFoundException {
    try {
      rmiConnectionHandler.createGame(gameId, this.index, numberPlayers);
      rmiConnectionHandler.joinLobby(gameId, this.index);
      // TODO
      //      localModelContainer.gameCreated(gameId, numberPlayers);
      localModelContainer.playerJoinedLobby(gameId, this.index);
    } catch (GameAlreadyStartedException e) {
      System.err.println("The game already started" + e.getMessage());
    } catch (LobbyFullException e) {
      System.err.println("The lobby is full" + e.getMessage());
    } catch (GameNotFoundException e) {
      System.err.println("The game was not found" + e.getMessage());
    } catch (EmptyDeckException e) {
      System.err.println("A remote exception occurred" + e.getMessage());
    }
  }

  //TODO how can i know which game started and which no?
  @Override
  public void checkIfGameStarted() throws RemoteException {}

  @Override
  public void lobbySetToken(TokenColor color) {
    try {
      rmiConnectionHandler.lobbySetTokenColor(
        localModelContainer.getGameId(),
        this.index,
        color
      );
      localModelContainer.playerSetToken(
        localModelContainer.getGameId(),
        this.index,
        color
      );
      localPlayer = new LocalPlayer(color);
    } catch (GameAlreadyStartedException e) {
      System.err.println("The game already started" + e.getMessage());
    } catch (GameNotFoundException e) {
      System.err.println("The game was not found" + e.getMessage());
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Set<TokenColor> getAvailableTokens() {
    return localModelContainer.getTokenColor();
  }

  @Override
  public void lobbySetNickname(String nickname) {
    try {
      rmiConnectionHandler.lobbySetNickname(
        localModelContainer.getGameId(),
        this.index,
        nickname
      );
      this.localPlayer.setNickname(nickname);
    } catch (NicknameAlreadyTakenException e) {
      System.err.println("Nickname already taken" + e.getNickname());
    } catch (GameAlreadyStartedException e) {
      System.err.println("The game already started" + e.getMessage());
    } catch (GameNotFoundException e) {
      System.err.println("The game was not found" + e.getMessage());
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void lobbyChooseObjectiveCard(Boolean first) {
    try {
      rmiConnectionHandler.lobbySetObjectiveCard(
        localModelContainer.getGameId(),
        this.index,
        first
      );
      // TODO
      //      localModelContainer.playerChoseObjectiveCard(localModelContainer.getGameId(), , this.index, first);
    } catch (GameAlreadyStartedException e) {
      localModelContainer.playerLeftLobby(
        localModelContainer.getGameId(),
        this.index
      );
    } catch (GameNotFoundException e) {
      System.err.println("The game was not found");
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void lobbyJoinGame(CardSideType cardSide) {
    try {
      rmiConnectionHandler.lobbyJoinGame(
        localModelContainer.getGameId(),
        this.index,
        cardSide
      );
      // TODO
      //      localModelContainer.playerJoinedGame(localModelContainer.getGameId(), this.index, localPlayer.getNickname(), localPlayer.getToken(), );
    } catch (GameNotReadyException e) {} catch (GameAlreadyStartedException e) {
      localModelContainer.playerLeftLobby(
        localModelContainer.getGameId(),
        this.index
      );
    } catch (EmptyDeckException e) {
      // TODO
    } catch (IllegalCardSideChoiceException e) {
      // TODO
    } catch (IllegalPlacingPositionException e) {
      // TODO
    } catch (GameNotFoundException e) {
      // TODO
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void placeCard(
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  )
    throws PlayerNotActive, IllegalCardSideChoiceException, RemoteException, IllegalPlacingPositionException, GameNotFoundException {
    // TODO use localModelContainer
    //    rmiConnectionHandler.placeCard(
    //      localGameBoard.getGameId(),
    //      localPlayer.getNickname(),
    //      playerHandCardNumber,
    //      side,
    //      position
    //    );
  }

  @Override
  public void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  )
    throws PlayerNotActive, GameOverException, EmptyDeckException, InvalidNextTurnCallException, GameNotFoundException {
    // TODO use localModelContainer
    //    rmiConnectionHandler.nextTurn(
    //      localGameBoard.getGameId(),
    //      localPlayer.getNickname(),
    //      drawingSource,
    //      deckType
    //    );
  }

  @Override
  public void nextTurn()
    throws PlayerNotActive, GameOverException, InvalidNextTurnCallException, RemoteException, GameNotFoundException {
    // TODO use localModelContainer
    //    rmiConnectionHandler.nextTurn(
    //      localGameBoard.getGameId(),
    //      localPlayer.getNickname()
    //    );
  }

  @Override
  public GameState getGameState() {
    // TODO
    return null;
  }
}
