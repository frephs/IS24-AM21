package polimi.ingsw.am21.codex.model;

import java.util.*;
import java.util.stream.Collectors;
import polimi.ingsw.am21.codex.model.Game;

public class GameManager {

  private final Map<String, Game> games;

  public GameManager() {
    games = new HashMap<>();
  }

  public Set<String> getGames() {
    return games
      .keySet()
      .stream()
      .filter(
        gameName ->
          games.get(gameName).getLobby().getRemainingPlayerSlots() != 0
      )
      .collect(Collectors.toSet());
  }

  public Optional<Game> getGame(String gameName) {
    return Optional.ofNullable(games.get(gameName));
  }

  public Game createGame(String gameName, Integer players) {
    Game newGame = new Game(players);
    games.put(gameName, newGame);
    return newGame;
  }

  public void deleteGame(String gameName) {
    games.remove(gameName);
  }
}
