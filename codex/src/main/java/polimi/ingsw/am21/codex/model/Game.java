package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.GameBoard.GameBoard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Game {

    private final int MAX_PLAYERS = 4;
    private Player currentPlayer;
    private List<Player> players = new ArrayList<>(MAX_PLAYERS);
    private GameBoard;

    private GameState;

    private TokenColors tokens[] = Arrays.stream(TokenColors.values()).filter(token -> token != TokenColors.BLACK).;

    public Game(){

    }

    public void addPlayer(Player player){
        if(players.size() < MAX_PLAYERS){
            int i = 0;
            while(i< this.players.size() && !this.players.get(i).nickname.equals(player.nickname))
                ++i;
            if(i == this.players.size())
                this.players.add(player);
        }
    }

    public void addPlayer(String nickname, String token){
        Player player = new Player(nickname, token);
        this.addPlayer(player);
    }

    public playTurn(Player currentPlayer, int deck, int source){
        PlayedCard card;
        player.playCard();

        //BOH

        source = currentPlayer.choseDrawingSource(source);
        deck = currentPlayer.choseDrawingDeck(deck);
        card = GameBoard.drawCard(source, deck);
        currentPlayer.


    }
}
