# Network Protocol
The network protocol we designed will implement both RMI and Socket functionality.
Both client and server are equipped with a message parser and RMI interfaces which are meant to update the views in the client side and call controller methods in the server side.

## Notes on RMI 
In this documentation, we're going to represent only the Socket messages since every message corresponds to a remote method invocation as there is duality in the approaches.

### General Flow for non-permitted requests


### Player Lobby Flow
the Player Building Process in the Game Lobby, being it divided into sequential essential steps,
a different message from the last one received is meant as a confirm message 


```mermaid
sequenceDiagram
    autonumber
    Client -) Server : JoinLobbyMessage
    loop until the value is acceptable
    
    # Player Nickname
    Server ->> Client : SetPlayerNicknameMessage
    Client --) Server : PlayerNicknameSetMessage 
    end
    
    loop to all the other clients
    Server --) Other clients in the lobby:  PlayerNicknameSet
    end

    # Player Token color 
    loop until the value is acceptable
    Server ->> Client : SetTokenColorMessages (populated with the available ones)
    
    Client --) Server : TokenColorSetMessage 
    end
    
    loop to all the other clients
    Server --) Other clients in the lobby:  RemoveTokenColorMessage
    end


    # Player Secret Objective 
    Server ->> Client : SetObjectiveCardMessage (populated with the available ones)
    Client --) Server : SelectFromPairMessage

    #Player Starter Card Side to place
    Server ->> Client : SetStarterCardSide (populated with the available ones)
    Client --) Server : SelectFromPairMessage
    Server ->> Client : GameJoinMessage
    
    loop to all the other clients
    Server --) Other clients already in game:  PlayerGameJoinMessage
    end

```

## Normal game turns flow 

```mermaid
sequenceDiagram
    # Game Start
    autonumber
    Server ->> All clients : GameStatusChangeMessage (START)
    
    Server -) All clients : PlayerStatusChange (populated with nickname)
    
    loop until a confirm is received 
        Playing client --) Server : PlaceCardMessage
        Server --) All clients : CardPlacedMessage (CONFIRMS)
    end

    Note right of All clients : conferming the card placement and updating views 
    Server --) All clients : PlayerScoreUpdateMessage
    Server --) All clients : NextPlayerActionMessage

    Playing client -) Server : DeckDrawMessage OR CardPairMessage
    Server --) All clients : DeckCardDrawMessage OR CardPairDrawMessage



```


## Game over flow
```mermaid
sequenceDiagram
    Note over Server, All clients : Normal Turn flow interactions
    loop until a GameOverException is thrown
    Server --> All clients: turn flow messages 
    end 
    Note over Server, All clients: A Game Over Exception <br> is thrown in the model
    Server -) All clients : RemainingTurnsMessage 
    loop for all players 
        Server -> All clients: normal turn interactions 
    end 
    Server -) All clients : GameOverMessage
    
    loop for all the players 
        loop for each objective card 
            Server -) All clients : PlayerScoreUpdateMessage
        end 
    end

    Server -) All clients : WinningPlayerMessage  


```

## Chat
This comunication happen when a player want to write a message in the chat. They send the postMessage to the Server that will notify the player that the message has been received and posted; later it will send a notification to all the other players that there is a new message in the chat.
```mermaid
sequenceDiagram
    Client -) Server: postMessage
    Server -) Client: messagePosted

    loop for each recipient
        Server -) Recipient: newMessageInChat
    end
```


