package polimi.ingsw.am21.codex.controller.listeners;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Game;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.PlayerNotFoundException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class LobbyUsersInfo implements Serializable {

  public static class LobbyInfoUser implements Serializable {

    private final String nickname;
    private final TokenColor color;
    private final Boolean objectiveCardChosen;
    private final Boolean inGame;

    public LobbyInfoUser(
      String nickname,
      TokenColor color,
      Boolean objectiveCardChosen,
      Boolean inGame
    ) {
      this.nickname = nickname;
      this.color = color;
      this.objectiveCardChosen = objectiveCardChosen;
      this.inGame = inGame;
    }

    public Optional<String> getNickname() {
      return Optional.ofNullable(nickname);
    }

    public Optional<TokenColor> getTokenColor() {
      return Optional.ofNullable(color);
    }

    public Optional<Boolean> getObjectiveCardChosen() {
      return Optional.ofNullable(objectiveCardChosen);
    }

    public Boolean getInGame() {
      return inGame;
    }
  }

  private final String gameID;
  private final Map<UUID, LobbyInfoUser> users;

  public LobbyUsersInfo(
    Map<UUID, GameController.UserGameContext> allUsers,
    String gameID,
    Game game
  ) {
    this.gameID = gameID;
    this.users = new HashMap<>();
    allUsers
      .entrySet()
      .stream()
      .filter(
        entry ->
          entry
            .getValue()
            .getGameId()
            .map(gid -> gid.equals(gameID))
            .orElse(false)
      )
      .forEach(
        entry ->
          this.users.put(
              entry.getKey(),
              new LobbyInfoUser(
                entry.getValue().getNickname().orElse(null),
                entry.getValue().getStatus() ==
                  GameController.UserGameContextStatus.IN_LOBBY
                  ? game
                    .getLobby()
                    .getPlayerTokenColor(entry.getKey())
                    .orElse(null)
                  : game
                    .getPlayer(
                      entry
                        .getValue()
                        .getNickname()
                        .orElseThrow(
                          () -> new PlayerNotFoundException(entry.getKey())
                        )
                    )
                    .getToken(),
                entry.getValue().getStatus() ==
                  GameController.UserGameContextStatus.IN_LOBBY
                  ? game
                    .getLobby()
                    .hasSelectedFirstObjectiveCard(entry.getKey())
                  : true,
                entry.getValue().getStatus() ==
                GameController.UserGameContextStatus.IN_GAME
              )
            )
      );
  }

  public String getGameID() {
    return gameID;
  }

  public Map<UUID, LobbyInfoUser> getUsers() {
    return users;
  }
}
