package polimi.ingsw.am21.codex.connection.server.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.CreateGameMessage;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.GetAvailableGameLobbiesMessage;

class TCPConnectionServerTest {

  public static void main(String[] args) {
    TCPConnectionServer server = new TCPConnectionServer(
      4567,
      new GameController()
    );
    try (ExecutorService executor = Executors.newCachedThreadPool()) {
      executor.execute(() -> {
        try {
          server.start();
        } catch (PortUnreachableException e) {
          throw new RuntimeException(e);
        }
      });

      Socket socket = new Socket("127.0.0.1", 4567);
      socket.setKeepAlive(true);
      System.out.println("Connected to server");
      ObjectOutputStream outputStream;
      ObjectInputStream inputStream;
      try {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
      } catch (IOException e) {
        System.err.println("Connection closed.");
        throw new RuntimeException(e);
      }

      executor.execute(() -> {
        System.out.println("Listening for responses...");
        while (true) synchronized (inputStream) {
          try {
            if (socket.isClosed()) {
              System.out.println("Connection closed.");
              break;
            }
            Message message = (Message) inputStream.readObject();
            System.out.println("Received message: " + message);
          } catch (IOException ignored) {} catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
        }
      });

      executor.execute(() -> {
        while (true) {
          if (socket.isClosed()) {
            System.out.println("Connection closed client.");
            break;
          }
        }
      });
      try {
        outputStream.writeObject(new CreateGameMessage("TestGame", 4));
        outputStream.flush();
        outputStream.reset();
        outputStream.writeObject(new GetAvailableGameLobbiesMessage());
        outputStream.flush();
        outputStream.reset();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } catch (IOException e) {
      throw new RuntimeException("Socket closed. " + e);
    }
  }
}
