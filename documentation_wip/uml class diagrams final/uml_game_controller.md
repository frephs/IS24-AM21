

## Game Controller
Since we chose a model heavy architecture, the game controller is merely a container of the game, that handles parsing requests from the `ServerConnectionHandlers` and updates their views trough their listeners.

```mermaid
classDiagram
  direction TB 

class GameController {
  + GameController() 
  ~ GameManager manager
  ~ Map~UUID, UserGameContext~ userContexts
  + lobbySetNickname(UUID, String) void
  - notifyClients(List~Pair~UUID, UserGameContext~~, RemoteListenerFunction, Boolean) void
  - getSameContextListeners(UUID, Boolean, EventDispatchMode) List~Pair~UUID, UserGameContext~~
  + removePlayerFromLobby(Game, UUID) void
  + getLobbyStarterCard(UUID) Integer
  + lobbyChooseObjective(UUID, Boolean) void
  + getCurrentSlots() Map~String, Integer~
  - sendGameStartedNotification(String, Game) void
  + joinGame(UUID, String, CardSideType) void
  + getGames() Set~String~
  - notifySameContextClients(UUID, RemoteListenerFunction) void
  + nextTurn(UUID) void
  + checkClientConnections() void
  + nextTurn(UUID, DrawingCardSource, DrawingDeckType) void
  + notifyDisconnectionsSameContext(List~UUID~, List~Pair~UUID, UserGameContext~~) void
  + lobbySetTokenColor(UUID, TokenColor) void
  + getGamesMaxPlayers() Map~String, Integer~
  + deleteGame(UUID, String) void
  + getGamesCurrentPlayers() Map~String, Integer~
  + joinLobby(UUID, String) void
  + isLastRound(String) Boolean
  - evaluateObjectives(Game, UUID) void
  + checkClientConnections(UUID) void
  + quitFromLobby(UUID) void
  + heartBeat(UUID) void
  + getLobbyObjectiveCards(UUID) Pair~Integer, Integer~
  + connect(UUID, RemoteGameEventListener) void
  + placeCard(UUID, Integer, CardSideType, Position) void
  - getUserContext(UUID) UserGameContext
  + notifyDisconnections(List~UUID~) void
  - nextTurnEvent(UUID, String, Game) void
  + getGame(String) Game
  - notifyClients(List~Pair~UUID, UserGameContext~~, RemoteListenerFunction) void
  + startGame(UUID) void
  - checkIfCurrentPlayer(Game, UUID) void
  + getAvailableTokens(String) Set~TokenColor~
  + getMaxSlots() Map~String, Integer~
  + sendChatMessage(UUID, ChatMessage) void
  - notifySameContextClients(UUID, RemoteListenerFunction, EventDispatchMode) void
  + createGame(UUID, String, Integer) void
}


  GameEventListener  <--  GameController : implements
style GameEventListener stroke:#ff,stroke-width:4px


class GameManager {
  + GameManager() 
  - Map~String, Game~ games
  + getCurrentSlots() Map~String, Integer~
  + createGame(String, Integer) Game
  + getMaxSlots() Map~String, Integer~
  + getGames() Set~String~
  + getGame(String) Optional~Game~
  + deleteGame(String) void
} 


class EventDispatchMode {
<<enumeration>>
  + EventDispatchMode() 
  +  BOTH_WAYS
  +  BOTTOM_UP_FULL
  +  TOP_DOWN
  +  BOTH_WAYS_FULL
  +  BOTTOM_UP
  +  TOP_DOWN_FULL
  +  SAME_CONTEXT
  - isMenu(UserGameContextStatus) Boolean
  + valueOf(String) EventDispatchMode
  + checkDispatchable(UserGameContextStatus, UserGameContextStatus) Boolean
  - getContextRanking(UserGameContextStatus) Integer
  - isBothMenu(UserGameContextStatus, UserGameContextStatus) Boolean
  + values() EventDispatchMode[]
}


class UserGameContext {
  - UserGameContext(String, UserGameContextStatus, String, RemoteGameEventListener) 
  + UserGameContext() 
  + UserGameContext(String) 
  + UserGameContext(RemoteGameEventListener) 
  - Date lastHeartBeat
  - String gameId
  - RemoteGameEventListener listener
  - String nickname
  - UserGameContextStatus status
  + disconnected() Boolean
  + setNickname(String) void
  + removeGameId() void
  + setLobbyGameId(String) void
  + getGameId() Optional~String~
  + heartBeat() Boolean
  + checkConnection() Optional~ConnectionStatus~
  + getStatus() UserGameContextStatus
  + getConnectionStatus() ConnectionStatus
  + setGameId(String, String) void
  + setListener(RemoteGameEventListener) void
  + getNickname() Optional~String~
  + getListener() RemoteGameEventListener
}
class UserGameContextStatus {
<<enumeration>>
  + UserGameContextStatus() 
  +  MENU
  +  IN_GAME
  +  IN_LOBBY
  + valueOf(String) UserGameContextStatus
  + values() UserGameContextStatus[]
}


class ConnectionStatus {

<<enumeration>>
  + ConnectionStatus() 
  +  DISCONNECTED
  +  CONNECTED
  +  LOSING
  + values() ConnectionStatus[]
  + valueOf(String) ConnectionStatus
}
  GameController "1" <-- "1" GameManager : composition
  GameController "1" <-- "0..*" UserGameContext : composition
  GameController -- EventDispatchMode : link
  UserGameContext <-- UserGameContextStatus : composition
  UserGameContext "1" <-- 1 ConnectionStatus  : composition
 ```