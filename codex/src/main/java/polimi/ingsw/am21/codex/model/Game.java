package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.CardPair;
import polimi.ingsw.am21.codex.model.Cards.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.PlayableCard;
import polimi.ingsw.am21.codex.model.GameBoard.GameBoard;
import polimi.ingsw.am21.codex.model.GameBoard.Lobby;
import polimi.ingsw.am21.codex.model.GameBoard.PlayerNotFoundException;

import java.util.*;
import org.json.JSONArray;

public class Game {
    List<Player> players;
    GameBoard gameBoard;
    Lobby lobby;
    GameState state;
    Optional<Integer> remainingRounds;
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

    public GameState getState(){
        return this.state;
    }

    public PlayerState getPlayerState(String nickname) {
        int i =0;
        while(i< players.size() && !players.get(i).getNickname().equals(nickname)){
            i++;
        }

        if(i >= players.size()) throw new PlayerNotFoundException(nickname);

        if(i == currentPlayer) return PlayerState.PLAYING;
        return PlayerState.WAITING;
    }

    public HashMap<String, Integer> getScoreBoard(){
        HashMap<String, Integer> scoreBoard = new HashMap<>();
        for(Player player : players){
            scoreBoard.put(player.getNickname(), player.getPoints());
        }
        return scoreBoard;
    }

    public Player getCurrentPlayer(){
        return this.players.get(currentPlayer);
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void nextTurn() throws GameOverException {
        if(this.state == GameState.GAME_OVER) throw new GameOverException();
        currentPlayer = (currentPlayer + 1) % players.size();
    }

    public Boolean getGameOver(){
        return this.state == GameState.GAME_OVER;
    }

    public void setGameOver(){
        this.state = GameState.GAME_OVER;
    }

    public Optional<Integer> getRemainingRounds(){
        return this.remainingRounds;
    }

    public Boolean isResourceDeckEmpty(){
        return this.gameBoard.resourceCardsLeft() == 0;
    }

    public Boolean isGoldDeckEmpty(){
        return this.gameBoard.goldCardsLeft() == 0;
    }

    public Boolean areDecksEmpty(){
        return this.isResourceDeckEmpty() && this.isGoldDeckEmpty();
    }

    public PlayableCard drawCurrentPlayerCardFromDeck(DrawingDeckType deckType) throws EmptyDeckException, GameOverException {
        if(this.state == GameState.GAME_OVER) throw new GameOverException();
        if(deckType == DrawingDeckType.RESOURCE){
            return this.gameBoard.drawResourceCardFromDeck();
        } else {
            return this.gameBoard.drawGoldCardFromDeck();
        }
    }

    public PlayableCard drawPlayerCardFromPair(DrawingDeckType deckType, boolean first) throws EmptyDeckException, GameOverException {
        if(this.state == GameState.GAME_OVER) throw new GameOverException();
        if(deckType == DrawingDeckType.RESOURCE){
            return this.gameBoard.drawResourceCardFromPair(first);
        } else {
            return this.gameBoard.drawGoldCardFromPair(first);
        }
    }

}
