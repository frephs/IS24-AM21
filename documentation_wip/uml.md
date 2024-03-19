# UML progetto di ingegneria del software
 A rough view of the UML of Model View Controller:
- **Model**: will be connected to the server side controller
    - *Game*: enforces the games rhythm, general rules about the game status, calls for turns and rounds, creates the game boards (common and personal), instantiates the players (passing their boards as parameters) and the decks of cards, keeps track of the game state and the game over condition.
    - *Player*: can play a turn (try to play a card on the player board given as parameter to their constructor and receive drawn cards), has points, 
        - *Player Board*: enforces rules for card placement and keeps tracks of resources, objets the geometry of the cards placed onto it
    - *Cards hierarchy*: The uml is pretty self explanatory
    - *GameBoard*: composed of (composition class to keep things tidy (is it necessary tho?)
        - *Score Board*: hashmap with buckets, of (lists of) player tokens (optional utility to useful for the GUI development eventually)
        - *Common Board*: common cards for everyone to use, methods to get and draw cards from here (the latter also draws a new card from the deck) (card retrieval methods comprehend common goals retrival for point calculation). 
        - *Card's Decks* : methods to shuffle and retrieve cards from the decks.
        -
- **View**:
    - Cli: text based implementation of the game components for the client
    - Gui: javaFX implementation of the game components for the client
- **Controller**:
    - Client: will handle input received from the respective views and will submit it to the server.
    - Server: will instantiate the game and handle the communications with all the clients.

## Model

### Cards
```mermaid
classDiagram

class ResourceType {
    <<Enumeration>>
    PLANT_KINGDOM
    ANIMAL_KINGDOM
    FUNGI_KINGDOM
    INSECT_KINGDOM

    +toString() String
    +has(Object value) boolean
}

class ObjectType {
    <<Enumeration>>
    QUILL
    INKWELL
    MANUSCRIPT

    +toString() String
    +has(Object value) boolean
}

class CardSideType {
    <<Enumeration>>
    FRONT
    BACK
}

class CornerPosition {
    <<Enumeration>>
    UP_LEFT
    DOWN_LEFT
    UP_RIGHT
    DOWN_RIGHT
}

class Card {
    <<Abstract>>
    -id: int

    +Card(int id)

    +getId() int
    +evaluate(PlayerBoard playerBoard) int*
}

class Corner~T~ {
    %%set in the constructor
    -content: Optional~T~
    -isCovered: bool
    
    +Corner()
    +Corner(T content)

    +isEmpty() bool
    +getContent() Optional~T~
    +cover() void
}
PlayableSide "1" *-- "1..4" Corner: composition

class PointConditionType {
    <<Enumeration>>
    OBJECTS
    CORNERS
}

class ObjectiveCard {
    -points: int
    -objective: Objective

    +ObjectiveCard(int id, int points, Objective objective)

    +evaluate(PlayerBoard playerBoard) int
    %% return points * objective.evaluate()
}

class Objective {
    <<Abstract>>
    %% how many times the objective has to be satisfied
    +evaluate(PlayerBoard playerBoard) int*
    %% returns
    %% lo realizzeremo dentro evaluate count: int
}
ObjectiveCard "1" *-- "1" Objective: composition
Card <|.. ObjectiveCard: realization 

class GeometricObjective {
    -geometry: ResourceType[3][3]

    +GeometricObjective(ResourceType[3][3] geometry)

    +evaluate(PlayerBoard playerBoard) int
}
Objective <|.. GeometricObjective : realization
%% ResourceType "3..n" <-- "n" GeometricObjective: dependency


class CountingObjective {
    -resources: HashMap~ResourceType; int~
    -objects: HashMap~ObjectType; int~
    
    +CountingObjective(HashMap~ResourceType; int~ resources, HashMap~ObjectType; int~ objects)
    
    +evaluate(PlayerBoard playerBoard) int
}
Objective <|.. CountingObjective : realization
%% ResourceType "0..4" <-- "n" CountingObjective: dependency
%% ObjectType "0..3" <-- "n" CountingObjective: dependency

class PlayableCard {
    -frontSide: PlayableFrontSide
    -backSide: PlayableBackSide
    -playedSide: CardSideType[0..1]
    -coveredCorners: int
    -kingdom: ResourceType[0..1]

    +PlayableCard(int id, PlayableSide front, PlayableSide back)
    +PlayableCard(int id, PlayableSide front, PlayableSide back, ResourceType kingdom)

    +getKingdom() ResourceType[0..1]
    +getPlayedSide() PlayableSide
    +setPlayedSide(CardSideType sideType) void
    +getCoveredCorners() int
    +setCoveredCorners(int n) void
    +evaluate(PlayerBoard playerBoard) int
}
Card <|.. PlayableCard: realization
%% CardSideType "0..1" <-- "n" PlayableCard: dependency

class PlayableSide {
    <<Abstract>>
    -corners: Corner[1..4]

    +getCorners() Corner[1..4]
    +setCorner(CornerPosition position, ResourceType resource)
    +setCorner(CornerPosition position, ObjectType object)
    +evaluate(PlayerBoard playerBoard) int*
}
%% CornerPosition "1..4" <-- "n" PlayableSide: dependency
%% ResourceType "0..4" <-- "n" PlayableSide: dependency
%% ObjectType "0..4" <-- "n" PlayableSide: dependency

class PlayableBackSide {
    -permanentResources: ResourceType[1..3]

    +PlayableBackSide(ResourceType[1..3] permanentResources)

    +getResources() ResourceType[1..3]
    +evaluate(PlayerBoard playerBoard) int
}
PlayableSide <|.. PlayableBackSide: realization
PlayableCard "1" *-- "1"  PlayableBackSide: composition
%% ResourceType "1..3" <-- "n" PlayableBackSide: dependency

class PlayableFrontSide {
    <<Abstract>>
}
PlayableSide <-- PlayableFrontSide: inheritance
PlayableCard "1" *-- "1" PlayableFrontSide: composition

class StarterCardFrontSide {
    +StarterCardFrontSide()

    +evaluate(PlayerBoard playerBoard) int
}
PlayableFrontSide <|.. StarterCardFrontSide: realization

class ResourceCardFrontSide {
    -points: int

    +ResourceCard(int points)

    +evaluate(PlayerBoard playerBoard) int
}
PlayableFrontSide <|.. ResourceCardFrontSide: realization

class GoldCardFrontSide {
    -placementCondition: ResourceType[1..5]
    -pointCondition: PointConditionType[0..1]
    -pointConditionObject: ObjectType[0..1]

    +GoldCard(int points, ResourceType[1..5] placementCondition, PointConditionType[0..1] pointCondition, ObjectType[0..1] pointConditionObject)

    +evaluate(PlayerBoard playerBoard) int
}
ResourceCardFrontSide <|-- GoldCardFrontSide: inheritance
%% ResourceType "1..5" <-- "n" GoldCardFrontSide: dependency
%% PointConditionType "0..1" <-- "n" GoldCardFrontSide: dependency
%% ObjectType "0..1" <-- "n" GoldCardFrontSide: dependency
```

### Game model

```mermaid

---
title: Rest of the model
---
classDiagram

class TokenColors{
    <<Enumeration>>
    RED
    BLUE
    GREEN
    YELLOW
    BLACK
    
    +toString() String
}


class Game{
    
    -tokens: Token[9] 
    -players: Player[2..4] 
    -gameBoard: GameBoard
    -?state: GameStates 

    -currentPlayer: Player

    
    Game(int players)
    %% contstructor: creates all the game assets.

    -isGameOver() bool
    %%GameOver() void
    
    
    +addPlayer(String nickname) bool
    %% TODO: decidere come gestire il caso in cui viene rifiutata la richiesta di aggiunta di un giocatore (nickname già presente o troppi giocatori)
    

    +?getGameStates() GameStates
    
    getPlayersNames()

    playTurn() void
    %% to be specified

    +evaluateObjectives()
    %% calls player.evaluateObjectives() for each player with the common objectives as parameter
}

%% TODO decidere se implementarlo come un obietti
class GameState_OR_PlayerStates{
    <<Enumeration>>
    GAME_INIT
    %%WAITING
    PLAYING
    GAME_OVER
}

class PlayerState{
    <<Enumeration>>
    WAITING
    PLAYING
}


class Deck~T~ {
    cards: Set~Card~ 
    %% Una pila forse
    Deck(Card[n])
    shuffle() void
    draw() Card
    draw(int) Card[*]
    cardsLeft() int
    insert(T ) void
    %% insert card back after card drawing. (ie when you don't choose an objective)
}

class Player {
    nickname: String 
    points: int
    token: Token
    board: playerBoard
    Player(String nickname, Token token)
    getPoints() int
    setPoints(int) void
    %% points are abviously private
    

    +drawCard(SidedCard card) void
    %% receive  card and put it in the player's hand

    +chooseObjective(ObjectiveCard[2] availableObjectives) ObjectiveCard
    
    +chooseDrawingDeck(int source, int deck) DrawingSources, DeckDrawingSources
    
    +chooseToken()

    placeCard(int cardNumber) void
    %% removes the card from the player's hand and places it on the board calling the playerboard method

    evaluateObjectives(ObjectiveCard[2] commonObjectives) void
}

class DeckDrawingSources{
    <<Enumeration>>
    GOLD_DECK
    RESOURCE_DECK
}

class DrawingSources{
    <<Enumeration>>
    DECK
    COMMON_BOARD
}

class Token {
    color: TokenColors
    Token(TokenColors)
}

class Position{
    x: int
    y: int

    computeLinkingPosition(CornerEnum linkedCorner) Position
    Position(x,y)
    
    %% Overriding default hashmap key methods
    equals(Position) bool
    hashCode() int
}

class GameBoard {
    goldDeck : deck~GoldCard~
    resourceDeck: deck~ResourceCard~
    starterDeck: deck~StarterCard~
    objectiveDeck: deck~ObjectiveCard~ 
    commonBoard: CommonBoard
    scoreBoard: ScoreBoard
    
    GameBoard()
    loadCardsSchemas() Static void
    %% loads the cards from the json/xml files and creates the decks

    drawGoldCard() GoldCard
    drawGoldCard(int cards) GoldCard[]
    drawResourceCard() ResourceCard
    drawResourceCard(int cards) ResourceCard[]
    %% draw cards from the common board and replace them
}

class CommonBoard{
    goldCards: GoldCard[2]
    resourceCards: ResourceCard[2]
    objectiveCards: ObjectiveCard[2]
    
    CommonBoard(Goldcards goldCards[2], \nResourceCard resourceCards[2], objectiveCards)
    %% without parameters cause we'll draw 2 cards with deck.draw(2) for each type.

    getObjectiveCards() ObjectiveCard[2]
}

%% is This redundant?
class ScoreBoard {
    scores: Hasmap~String; int~
    %%HashMap~TokenColors; int~ redundant???
}

class PlayerBoard {
    cards: SidedCard[3]
    objectiveCards: ObjectiveCard
    %% the geometry is an hasmap of positions and played cards

    playedCards: HashMap~Position; PlayedCard~
    AvailableSpots: List~Position~
    
    resources: HashMap~ResourceType; int~
    objects: HashMap~Objects; int~

    PlayerBoard(SidedCard[3] cards, ObjectiveCard objectiveCard, startCard)
    
    +chosePlacingPosition(int) Position
    
    placeCard(SidedCard card, cardSidesTypes side, Position position) void
    %% instanciates a played card and places it on the board, updates the resources and objects calling the helper method, updates the available spots calling the helper method

    updateResourcesandObjects(PlayedCard playedCard, Position position) void

    updateAvailableSpots(Position position) void

    evaluatePoints(PlayedCard card) int
}

Game "2"*--"4" Player : is composed of 
Game "1"*--"1" GameBoard : is composed of
Game "1"*--"9" Token : is composed of
Token <-- TokenColors : uses
GameBoard "1"*--"4" Deck : is composed of
GameBoard "1"*--"1" CommonBoard : is composed of
Game "1"*--"1" ScoreBoard : is composed of

PlayerBoard <-- Position : uses
Player --|> Iterable : implements
Player --* PlayerBoard: composed of

Player <-- DrawingSources : uses
Player <-- DeckDrawingSources : uses
```

## Considerations
The rationale is to implement every element that can become graphical as a separate class, so that there is a correspondence once the view is implemented. Each element will have a decorator toString to realize the cli and a method to draw it on the GUI
