package polimi.ingsw.am21.codex.controller.listeners;

import java.io.Serializable;
import java.util.Optional;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class FullUserGameContext implements Serializable {

  private final GameController.UserGameContextStatus status;
  private final String gameID;
  private final String nickname;
  private final TokenColor tokenColor;
  private final Boolean choseObjectiveCard;
  private final LobbyUsersInfo lobbyUsers;
  private final GameInfo gameInfo;

  private FullUserGameContext(
    GameController.UserGameContextStatus status,
    String gameID,
    String nickname,
    TokenColor tokenColor,
    Boolean choseObjectiveCard,
    LobbyUsersInfo lobbyUsers,
    GameInfo gameInfo
  ) {
    this.status = status;
    this.gameID = gameID;
    this.nickname = nickname;
    this.tokenColor = tokenColor;
    this.choseObjectiveCard = choseObjectiveCard;
    this.lobbyUsers = lobbyUsers;
    this.gameInfo = gameInfo;
  }

  public FullUserGameContext() {
    this(
      GameController.UserGameContextStatus.MENU,
      null,
      null,
      null,
      false,
      null,
      null
    );
  }

  public FullUserGameContext(
    String gameID,
    String nickname,
    TokenColor tokenColor,
    Boolean choseObjectiveCard,
    LobbyUsersInfo lobbyUsers
  ) {
    this(
      GameController.UserGameContextStatus.IN_LOBBY,
      gameID,
      nickname,
      tokenColor,
      choseObjectiveCard,
      lobbyUsers,
      null
    );
  }

  public FullUserGameContext(
    String gameID,
    String nickname,
    TokenColor tokenColor,
    GameInfo gameInfo
  ) {
    this(
      GameController.UserGameContextStatus.IN_GAME,
      gameID,
      nickname,
      tokenColor,
      true,
      null,
      gameInfo
    );
  }

  public GameController.UserGameContextStatus getStatus() {
    return status;
  }

  public Optional<String> getGameID() {
    return Optional.ofNullable(gameID);
  }

  public Optional<String> getNickname() {
    return Optional.ofNullable(nickname);
  }

  public Optional<TokenColor> getTokenColor() {
    return Optional.ofNullable(tokenColor);
  }

  public Optional<Boolean> getChoseObjectiveCard() {
    return Optional.ofNullable(choseObjectiveCard);
  }

  public Optional<LobbyUsersInfo> getLobbyUsers() {
    return Optional.ofNullable(lobbyUsers);
  }

  public Optional<GameInfo> getGameInfo() {
    return Optional.ofNullable(gameInfo);
  }
}
