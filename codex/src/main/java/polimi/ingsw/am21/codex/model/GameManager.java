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

  /**
   * Returns the current number of players in each game.
   *
   * @return a map with the game name as key and the number of players as value
   */
  public Map<String, Integer> getCurrentSlots() {
    Map<String, Integer> currentPlayers = new HashMap<>();
    games
      .keySet()
      .forEach(key -> {
        currentPlayers.put(
          key,
          games.get(key).getMaxPlayers() -
          games.get(key).getLobby().getRemainingPlayerSlots()
        );
      });
    return currentPlayers;
  }

  /**
   * Returns the maximum number of players in each game.
   *
   * @return a map with the game name as key and the maximum number of players as value
   */
  public Map<String, Integer> getMaxSlots() {
    Map<String, Integer> maxSlots = new HashMap<>();
    games
      .keySet()
      .forEach(key -> {
        maxSlots.put(key, games.get(key).getMaxPlayers());
      });
    return maxSlots;
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
