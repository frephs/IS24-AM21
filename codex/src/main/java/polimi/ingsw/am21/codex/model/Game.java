package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.GameBoard.*;

import java.util.*;

import org.json.JSONArray;


public class Game {
    static final int WINNING_POINTS = 20;
    private final List<Player> players;
    private final GameBoard gameBoard;
    private Lobby lobby;
    private GameState state;
    private Integer remainingRounds = null;
    Integer currentPlayer;


    public Game(int players, JSONArray cards) {
        this.lobby = new Lobby();
        this.state = GameState.GAME_INIT;
        this.lobby = new Lobby(players);
        this.gameBoard = GameBoard.fromJSON(cards);
        this.players = new ArrayList<>();
    }

    public Lobby getLobby() {
        return this.lobby;
    }

    public void start() {
        this.state = GameState.PLAYING;
        Collections.shuffle(players);

    }

    public GameState getState() {
        return this.state;
    }

    public PlayerState getPlayerState(String nickname) {
        int i = 0;
        while (i < players.size() && !players.get(i).getNickname().equals(nickname)) {
            i++;
        }

        if (i >= players.size()) throw new PlayerNotFoundException(nickname);

        if (i == currentPlayer) return PlayerState.PLAYING;
        return PlayerState.WAITING;
    }

    public HashMap<String, Integer> getScoreBoard() {
        HashMap<String, Integer> scoreBoard = new HashMap<>();
        for (Player player : players) {
            scoreBoard.put(player.getNickname(), player.getPoints());
        }
        return scoreBoard;
    }

    public Player getCurrentPlayer() {
        return this.players.get(currentPlayer);
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void nextTurn() throws GameOverException {

        if (this.state == GameState.GAME_OVER) throw new GameOverException();
        currentPlayer = (currentPlayer + 1) % players.size();
        if (this.currentPlayer == 0 && this.remainingRounds != null) {
            this.remainingRounds--;
            if (this.remainingRounds == 0) {
                this.state = GameState.GAME_OVER;
                for (Player player : players) {
                    player.evaluateSecretObjective();
                    CardPair<ObjectiveCard> objectiveCards = gameBoard.getObjectiveCards();
                    player.evaluate(objectiveCards.getFirst());
                    player.evaluate(objectiveCards.getSecond());
                }
                throw new GameOverException();
            }
        }
        if (this.players.get(currentPlayer).getPoints() >= Game.WINNING_POINTS) {
            this.state = GameState.GAME_OVER;
            throw new GameOverException();
        }

    }

    public Boolean isGameOver() {
        return this.state == GameState.GAME_OVER;
    }

    public void setGameOver() {
        this.state = GameState.GAME_OVER;
    }

    public Optional<Integer> getRemainingRounds() {
        if (this.remainingRounds == null) return Optional.empty();
        return Optional.of(this.remainingRounds);
    }


    public Boolean isResourceDeckEmpty() {
        return this.gameBoard.resourceCardsLeft() == 0;
    }

    public Boolean isGoldDeckEmpty() {
        return this.gameBoard.goldCardsLeft() == 0;
    }

    public Boolean areDecksEmpty() {
        return this.isResourceDeckEmpty() && this.isGoldDeckEmpty();
    }

    public void drawCurrentPlayerCardFromDeck(DrawingDeckType deckType) throws EmptyDeckException, GameOverException {
        if (this.state == GameState.GAME_OVER) throw new GameOverException();
        try {
            PlayableCard card;
            if (deckType == DrawingDeckType.RESOURCE) {
                card = this.gameBoard.drawResourceCardFromDeck();
            } else {
                card = this.gameBoard.drawGoldCardFromDeck();
            }
            this.players.get(this.currentPlayer).drawCard(card);
        } catch (EmptyDeckException e) {
            if (this.remainingRounds == null) {
                this.remainingRounds = 2;

            }
            throw e;
        }
    }

    public PlayableCard drawPlayerCardFromPair(DrawingDeckType deckType, boolean first) throws EmptyDeckException, GameOverException {
        if (this.state == GameState.GAME_OVER) {
            throw new GameOverException();
        }
        try {
            if (deckType == DrawingDeckType.RESOURCE) {
                return this.gameBoard.drawResourceCardFromPair(first);
            } else {
                return this.gameBoard.drawGoldCardFromPair(first);
            }

        } catch (EmptyDeckException e) {
            if (this.remainingRounds == null) {
                this.remainingRounds = 2;
            }
            throw e;
        }
    }

    protected List<String> getPlayersOrder() {
        return this.players.stream().map(Player::getNickname).toList();
    }

}
