# Network Protocol
The network protocol we designed is meant to be implemented with both RMI and Client-Server Socket functionality.
Both client and server are equipped with listeners which implement the same interface to ask the controller to update the game, the clients to communicate the updates and the `ClientGameEventHandler`s to update every client's status and `View`. 

## General message handling 

### Client Action general handling
The following diagram reports a general rapresentation of our architecture's interactions when a `ClientAction` is sent to the server and an update is sent back to the concerned clients.

Both in client and server we set up listeners implementing the `RemoteGameEventListener` class or the `GameEventListener` (which extends it but does not throw `RemoteException`). This allows us to require all clients to process all the `GameEvents`. 

As for the client listeners, the `RMICLientConnectionHandler` calls the omonymous methods in the Controller, while the `TCPClientConnectionHandler` method implementation send a message to the server which is parsed calling the same methods in the controller. 

As for the server listeners, we have every `RMIClientConnectionHandler` reference and every `TCPServerConnectionHandler` object registered in the server handling the connection with the client by invoking directly a method of the `ClientGameEventHandler` in the case of RMI, or sending a message which will be parsed by the `TCPCLientConnectionHandler` which will call the `ClientGameEventHandler` in the case of TCP. 

In the following diagram we report a general rapresentation using `gameEventListenerMethod()` as an alias for a generic method.

```mermaid
sequenceDiagram
Actor Client 
Client  -) ServerConnectionHandler : client.gameEventListener()
ServerConnectionHandler -> Controller : controller.gameEventListenerMethod(...)
Note right of Controller: The action is performed by the controller which <br> the updates the correct game through the game manager. 
Controller -> Controller : Action is performed 
Note over Controller, Clients: A view update is sent to every concerned client
loop For each registered listener
Controller -) Clients: listener.gameEventListenerMethod(...)
end
Clients -> ClientGameEventHandler : clientConnectionHandler.gameEventListenerMethod(...) 
ClientGameEventHandler -> ClientGameEventHandler  : localModelContainer.gameEventListenerMethod(...)
ClientGameEventHandler -) ClientGameEventHandler : View.gameEventListenerMethod(...)

```

### Failed connection handling
After a connection is enstablished, the client starts sending regular heartbeats. After two missed heartbeats the client loosing connection's `ClientConnectionHandler` notifies the user while the other clients are notified by the server. The client is considered disconnected after 10 missed heartbeats.

As described above (but omitted in the following diagram), `ClientGameEventHandler` updates the `LocalModelContainer` synchronously and the view aynchronously.

```mermaid
sequenceDiagram
    actor Client 1
    loop 2 times
    Client 1 -x ServerConnectionHandler: client.hearBeat() (timed out)
    end 
    Client 1 -> Client 1 : ClientConnectionHandler.failedHeartBeats++
    Client 1 -) Client 1 : View.postnotification()"You're loosing connection to the server")
    ServerConnectionHandler ->  ServerConnectionHandler : Controller.playerConnectionChanged(connectionID, IS_LOSING)
    ServerConnectionHandler -) Other clients : listener.playerConnectionChanged(IS_LOSING)
    Other clients -> Other clients : ClientGameEventHandler.playerConnectionChanged(...)
    alt The next heartbeats don't fail
        Client 1 -) ServerConnectionHandler: client.heartBeat()

        Client 1 -> Client 1 : ClientConnectionHandler.failedHeartBeats = 0
    Client 1 -) Client 1 : View.postnotification()"Connection restored")
    ServerConnectionHandler ->  ServerConnectionHandler : Controller.playerConnectionChanged(connectionID, CONNECTED)
    ServerConnectionHandler -) Other clients : listener.PlayerConnectionChanged(CONNECTED)
    Other clients -> Other clients : ClientGameEventHandler.playerConnectionChanged(...)
    else 
    loop 8 times more
        Client 1 -x ServerConnectionHandler: Heartbeat 
    end 
    Client 1 -> Client 1 : ClientConnectionHandler.connectionFailed()

    Client 1 -) Client 1 : view.postNotification("Connection lost")

    Client 1 -> Client 1 : exit
    Destroy Client 1
    Client 1 --> Client 1 : 
    ServerConnectionHandler ->  ServerConnectionHandler : Controller.playerConnectionChanged(connectionID, DISCONNECTED)
    ServerConnectionHandler -) Other clients: listener.playerConnectionChanged(DISCONNECTED)
    Other clients -> Other clients : ClientGameEventHandler.playerConnectionChanged(...)
    end
    

```

### "Not allowed" message handling
In the event a client sends a message for an action that the server which is untimely for the user's context  or that they cannot perform in that moment, and in the event a client might be modified or 'enhanced' in a way the server does not contemplate, we have messages in place to send to the aforesaid client. 

Client's side the error's are parsed by the `ClientGameEventHandler` which implements the `GameErrorListener` interface.

Since updates are only register by the local model once the server validates them only the `View` implements the class as the errors need only to be notified to the users. 


```mermaid
sequenceDiagram
    actor Client

    Note over Client,ServerConnectionHandler: Client sends a type of message <br> that is not expected 
    Client -) ServerConnectionHandler: Untimely or forbidden action 
ServerConnectionHandler -> Controller : Controller.gameEventListenerMethod(...)
    Controller --> Controller : < ? extends InvalidActionException> thrown
     Controller -) ServerConnectionHandler  : corresponding listener.invalidActionMethod()

    ServerConnectionHandler -) Client : corresponding listener.invalidActionMethod()
    
    Note over Client,ServerConnectionHandler: We covered the possibility of a Tcp Client sending <br>  a message which is not recognized by the server
    Client -->> ServerConnectionHandler: <Unknown-type message>
    ServerConnectionHandler -->>  Client : unknownMessageTypeMessage
```

## Game Dynamics' Flows
### Connection and menu flow
As the user connects to the server, a `UUID` identifying the user's client connection is sent to the server and a listener is registered attached to that UUID.
The user then asks for the available games at the moment and the server responds with a list of game entries, containing the gameId, the current number of players and maximum number of players, which are displayed in the menu.

```mermaid
sequenceDiagram
    actor Client
    Note over Client, ServerConnectionHandler: The user connects to the server
    Client -) ServerConnectionHandler : client.connect(connectionID) 
    ServerConnectionHandler -> Controller : registerListener(connectionID, handler)

    # Get the available lobbies
    Note over Client,ServerConnectionHandler : Client is in the game menu 
    Client -) ServerConnectionHandler : client.getAvailableGameLobbies()
    ServerConnectionHandler -> Controller : controller.getAvailableGameLobbies()
    alt there are no games available

        Controller --> Controller : Returns games: List.of()
    else there are games available 
        Controller --> Controller : Returns games: List<GameEntry>
    end 
    Controller -) ServerConnectionHandler : listener.availableGameLobbies(...)
    ServerConnectionHandler -) Client : listener.availableGameLobbies(...)
    Client -> Client : cLientGameEventHandler.avaialableGameLobbies(...)
```

### Lobby Flow
The player building process requires a series of essential steps, which are reported in the following sequence diagram.

For the sake of Semplicity we omit the `ServerConnectionHandler` and Listener actors. The architecture remains the same as described above.
The `GameEventListener` method implentations of the `Controller` class call the methods omonymous the listeners which send an update the client.

#### Selecting or creating a game
```mermaid
sequenceDiagram
    actor Client

   
    # Join the Game Lobby
    alt Client selects a game lobby

    Client -) ServerConnectionHandler : client.connectToGame
    alt Lobby is full
        ServerConnectionHandler -> Controller : controller.connectToGame(socketId, lobbyId)
        Controller --> Controller : LobbyFullException is thrown
        Controller -) ServerConnectionHandler : listener.lobbyFull()
        ServerConnectionHandler -) Client : listener.lobbyFull()
    else Lobby is not full
         ServerConnectionHandler -> Controller : controller.joinLobby(socketId, gameId)
         Note over Controller, ServerConnectionHandler:  The client who joined a lobby get's the info of <br> the players already inside the lobby. (Tokens, nicknames)
        Controller -) ServerConnectionHandler : listener.lobbyInfo(...)
        ServerConnectionHandler -) Client : listener.lobbyInfo(...)
        loop for each client in the menu 
            Note over Controller, Client in the menu view: The clients in the menu get <br> their game entries updated <br> with the correct number of players
            Controller -) Client in the menu view : listener.playerJoinedLobby(connectionId, gameId)
        end
        loop for each client in the lobby
            Note Over Controller, Client in the lobby view: The clients in the lobby get <br> a notification a new  <br> player has joined
            Controller -) Client in the lobby view :  listener.playerJoinedLobby(connectionId, gameId)
        end
    end
    

    else Client creates a new game lobby
        Client -) ServerConnectionHandler : client.createGame(connectionID, gameID, players)
        ServerConnectionHandler -> Controller : Controller.createGame(...)
        Note over Controller, Client in the menu view : The clients in the menu register it and redraw the available games
        Controller -) Client in the menu view : listener.gameCreated() 
        Note over Controller, Client in the game view : The clients in the lobbies and game register it 
        Controller -) Client in the lobby view : listener.gameCreated()
        Controller -) Client in the game view : listener.gameCreated()


    end

```
#### Player Setup Process
```mermaid
sequenceDiagram
    actor Client

    # Player Token Color 
    Note over Client,ServerConnectionHandler : Client selects a token color

        Client -) ServerConnectionHandler : client.setToken() 
        ServerConnectionHandler -> Controller: controller.lobbySetTokenColor(nickname, tokenColor)
        alt Token color is already taken
            Controller --> Controller : NoSuchElementException
            Controller -) ServerConnectionHandler : listener.tokenTaken() 
            ServerConnectionHandler -) Client : listener.tokenTaken
        else Token color is accepted
            loop for each client in the lobby
                ServerConnectionHandler -) Client in the lobby view:  listener.playerSetToken()
            end
        end

    # Player Nickname
    Note over Client,ServerConnectionHandler : Client selects a nickname
        Client -) ServerConnectionHandler : client.setNickname(..)
        ServerConnectionHandler -> Controller: controller.lobbySetNickname(socketId, nickname)
        alt Nickname is already taken   
            Controller --> Controller: NicknameAlreadyTakenException
            Controller -) ServerConnectionHandler : listener.nicknameTaken()
            ServerConnectionHandler -) Client : listener.nicknameTaken()
        else Nickname is accepted
            loop for each client in the lobby
                ServerConnectionHandler -) Client in the lobby view:  listener.playerSetNickname
            end
        end    

  


    # Player Secret Objective 
    Note over Client,ServerConnectionHandler: Client selects a secret objective
    Client -) ServerConnectionHandler : client.getObjectiveCards()
    ServerConnectionHandler -) Controller : Controller.getObjectiveCards()
    Controller --> Controller : Returns cardIds: Pair<int> 
    Controller -) ServerConnectionHandler : listener.getObjectiveCards(pair(id1,id2))
    ServerConnectionHandler -) Client : listener.getObjectiveCards(pair(id1,id2))
    
    Client -) ServerConnectionHandler : listener.playerChooseObjectiveCard(boolean first) 
     loop for each client in the lobby
            ServerConnectionHandler -) Client in the lobby view:  listener.playerChoseObjectiveCard(first)
     end
    

    # Player Starter Card Side to Place
    Note over Client,ServerConnectionHandler: Client selects a starter card side to play
    Client -) ServerConnectionHandler : client.getStarterCardSides()
    ServerConnectionHandler -) Controller : Controller.getStarterCardSides()
    Controller -> Controller : Returns cardIds: Pair<int> 
    Controller -) ServerConnectionHandler : listener.gameInfo(...)
    ServerConnectionHandler -) Client : listener.gameInfo()

    
     loop for each client in the lobby 
            ServerConnectionHandler -) Client in the lobby view:  listener.playerJoinedGame(first)
     end

    loop for each client in the game
        ServerConnectionHandler -) Client in the game view: listener.playerJoinedGame
    end

    Note over Client,Client in the game view: The player is now in waitroom
    
    alt Not all players are in the game
        Note over Client,ServerConnectionHandler: The player knows he's still connected to the server.
    loop
        Client -) ServerConnectionHandler : client.heartBeat()
    end
    else All players are in the game
        ServerConnectionHandler -> Controller: controller.playerJoinedGame(..)
        loop for each client in the game
        ServerConnectionHandler -) Client in the lobby view: listener.playerJoinedGame
        ServerConnectionHandler -) Client in the game view: listener.playerJoinedGame
    end
        Controller -> Controller : Controller.gameStarted(gameId)
        loop for each client in the game view
            Controller -) Client in the game view: listener.gameStarted(...)
        end
    end
```

### Normal game turns flow 
Until `Game.nextTurn()` detects that a player has a winning score, the messages between the server and the clients are exchanged as follows.

As before, other than the `ConfirmMessage`, we have a series of messages whose recipients are all the clients in the game. They are used to update the views of the clients and to notify them of the status of the player turn.

```mermaid
sequenceDiagram
    actor Playing client
    participant Server as ServerConnectionHandler
    participant Controller 
    actor Client
    Controller -> Controller: gameStarted()
    Controller -) Client : listener.gameStarted()

    loop until the game is over
        # New turn 
        Note over Server,Playing client: Current Player Changes
        
        # Place card  
        Note over Playing client,Server: The playing client can place a card  
        loop until the card placement is valid
            Playing client -) Server : client.placeCard(handIndex, position, side)
            Server -> Controller: Controller.placeCard(...)
            alt card placement is not valid
                Controller --> Controller: InvalidCardPlacementException <br> IllegalCardSideChoice <br>
                Controller -) Playing client : listener.invalidCardPlacement(exceptionMessage)
            else card placement is valid
                loop for every client in the game
                    Controller -) Client : listener.cardPlaced(...)
                end
            end
        end        

        # Draw card 
        Note over Playing client,Server: The playing client can draw a card
        Playing client ->> Server : listener.nextTurn(drawingSource, drawingDeckType)
        Server -) Controller: Controller.nextTurn(...)
        alt deck is empty
            Controller --> Controller: EmptyDeckException
            loop for each client
                Controller -) Client : listener.remainingTurns()
            end
        else deck is not empty
            loop for each client
                Controller -) Client : listener.nextTurn(...)
            end
        end
    end
```
### Game over flow
When `controller.nextTurn()` detects that a player has a winning score or an `EmptyDeckException` is caught by the controller, all the clients are notified of the number of remaining rounds.

After the final rounds are played, the server will notify the clients the final scores after the objectives are evaluated and the winning player nickname.

```mermaid
sequenceDiagram
    participant Controller
    actor Client

    loop until game.remainingTurns is set
        Note over Controller, Client : Normal turn flow
    end 

    Note over Controller, Client: Last round is reached
    Controller -> Controller: GameOverExcpetion 

    Controller -) Client : listener.nextTurn() 
    loop for each client 
        Controller --> Client : normal turn interactions 
    end 
    
    Note over Controller, Client: Game overs
    Controller --> Controller: GameOverException <br> EmptyDeckException
    loop for each client 
        Controller -) Client : listener.gameOver()
        loop for each player
            opt if the player's score is updated
                Controller -) Client : listener.playerScoreUpdate(newScores)
            end
        end
        Controller -) Client : listener.winningPlayer(nickname)
    end
```


## Advanced Features
### Chat
The chat feature works similarly to the other view updates. Every view update is sent to the concerned clients present in the `ChatMessage` object. 

```mermaid
sequenceDiagram
actor Client
participant Server as ServerConnectionHandler
Note over Client, Server: The messages can be either <br>broadcasts or whispers.
alt The message is a broadcast message
    Client -) Server: client.chatMessage()
    loop for each client in the same game
        Server -) Recipient: listener.chatMessageSent
    end
else The message is a private message to an user
    actor Recipient
    Client -) Server: SendChatMessage
    Server -) Recipient: listener.chatMessageSent()
    Server -) Client: listener.chatMessageSent()
end



```
