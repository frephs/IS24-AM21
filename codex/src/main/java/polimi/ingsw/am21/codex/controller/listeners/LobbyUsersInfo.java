package polimi.ingsw.am21.codex.controller.listeners;

import java.io.Serializable;
import java.util.*;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Game;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.PlayerNotFoundGameException;

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
    List<Map.Entry<UUID, GameController.UserGameContext>> filteredUsers =
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
        .toList();

    for (Map.Entry<
      UUID,
      GameController.UserGameContext
    > entry : filteredUsers) {
      try {
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
                        () -> new PlayerNotFoundGameException(entry.getKey())
                      )
                  )
                  .getToken(),
              entry.getValue().getStatus() ==
                GameController.UserGameContextStatus.IN_LOBBY
                ? game.getLobby().hasSelectedFirstObjectiveCard(entry.getKey())
                : true,
              entry.getValue().getStatus() ==
              GameController.UserGameContextStatus.IN_GAME
            )
          );
      } catch (PlayerNotFoundGameException e) {
        // if we have a PlayerNotFoundException we reset their user context
        allUsers.put(
          entry.getKey(),
          new GameController.UserGameContext(entry.getValue().getListener())
        );
      }
    }
  }

  public String getGameID() {
    return gameID;
  }

  public Map<UUID, LobbyInfoUser> getUsers() {
    return users;
  }
}
