package polimi.ingsw.am21.codex.view.TUI;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.localModel.GameBoard;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.ConnectionType;

public class Client {

  private GameBoard game;
  private ClientContext context = ClientContext.LOBBY;

  Scanner scanner = new Scanner(System.in);

  void start(ConnectionType connectionType, String address, int port) {
    String line;
    String[] command;
    ClientConnectionHandler client;

    if (connectionType == ConnectionType.TCP) {
      client = new SocketClient(address, port);
    } else {
      client = new RmiClient(address, port);
    }

    ExecutorService executorService = Executors.newCachedThreadPool();

    while (true) {
      line = scanner.nextLine().trim();
      command = line.split(" ");

      switch (command[0]) {}
    }
  }

  public static void main(String[] args) {}
}
