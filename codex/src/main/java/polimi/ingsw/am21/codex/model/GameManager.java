package polimi.ingsw.am21.codex.model;


import polimi.ingsw.am21.codex.model.Game;

import java.util.*;

public class GameManager {
  private final Map<String, Game> games;

  public GameManager() {
    games = new HashMap<>();
  }

  public Set<String> getGames() {
    return games.keySet();
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
