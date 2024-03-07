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

class ResourcesTypes {
    <<Enumeration>>
    PLANT_KINGDOM
    ANIMAL_KINGDOM
    FUNGI_KINGDOM
    INSECT_KINGDOM

    +toString() String
}

class ObjectTypes{
    <<Enumeration>>
    QUILL
    INKWELL
    MANUSCRIPT

    +toString() String
}

class CardSides{
    <<Enumeration>>
    FRONT
    BACK
}

class Card {
    %% potrebbe essere un'interfaccia
    <<Abstract>>
    %%+createCard() void
}

class CornerEnum {
    <<Enumeration>>
    UP_LEFT
    DOWN_LEFT
    UP_RIGHT
    DOWN_RIGHT
}

class SidedCard {
    sides : CardSide[2]
    %% cardSide[1] will be instanced as CardBackSide obv. 
    SidedCard(CardSide, CardBackSide)
    
    %%calls the method link of the corner and adjusts the content of the corners
}

class PlayableCard{
    playedSide: CardSides
    card: SidedCard
    adjacentCards: PlayableCard[2]
    relativePositionX: int
    relativePositionY: int
    getAvailableCorners() Corner[]
    getLinkedCards()
    linkCard(card: SidedCard, corner: Corner) void
    %% the adjacent cards are the ones above and below the card which are not actually linked together but are useful to have a connection to to calculate the points of geometrical objective

}

class CardSide {
    corners: Optional~Corner~ [4] 
    CardSide(Corner corners[])
    
    %% You actually position cardSides not cards
    getResources() hashmap~ResourceType, int~
    getObjects() hashmap~Objects, int~ 
    %% devo dargli i corners come input.
}

class CardBackSide {
    centralResources: Optional~ResourceTypes~[2]
    %% overrides 
    getResources() hashmap~ResourceType, int~
}

class Corner~T~{
    cornerNumber: Enum:1,2,3,4~ 
    content: Optional~T~
    actualContent: Optional~T~
    linkedCard: Optional~Card~
    %% it is important that we link a card and not a corner cause otherwise we'd have to implement something like corner.parentCard and honestly ew.

    isLinked() bool
    linkCorner(Corner) void
    %% changes the value of linkedCorner and the value of actualContent if the content of the linked corner is different
    getActualContent() T 
    %% returns the content of the linked corner if it's linked, else returns the content of the corner, also depending on which card is above (the one underneath will be the one that has the linked corner)
}

%%class Linkable{
  %%  <<Interface>>
    %%-link() void
    %% couples that connect are 1-4 2-3 with transitivity
    %% come implementiamo link? 
%%}




class ResourceCard{
    kingdom: ResourceType
    points: Optional~int~
}

class GoldCard{
    conditionalSet: Optional<ResourceType>[5]
    pointCondition: optional~PointConditionTypes~
    %% o è meglio una lista?
    conditionalObject optional~ObjectTypes~
    %% object that if present on the player board will grant points
    evalutePoints()
    %%override
}

class PointConditionTypes{
    <<Enumeration>>
    OBJECTS
    CORNERS
}

CardSide "1"<|--"1" CardBackSide : inherits from

class StarterCard{
 
    starterCard()
    %% solo risorse niente oggetti negli angoli ma con
    firstPlayerToken: bool 
    %% useful for the GUI
}

%%class StarterCardBackSide {
  %%  centralResources: ResourceTypes[2]
%%}

%%CardSide "1"<|--"1" %%StarterCardBackSide : extends



class ObjectiveCard{
    points: final int
    objective: Optional~ObjectTypes[3]~
    %% è meglio una lista?
    +conditionalRule() bool
}

class Objective{
    <<abstract class>>
    count: int
    %% how many times the objective has to be satisfied
    evaluate() bool
}

class GeometricObjective{
    geometry: ResourceTypes[3][3]
    %% how many times the 
    evaluate() bool
}

class CountingObjective{
    resources: HashMap~ResourceType, int~
    objects: HashMap~Objects, int~
    evaluate() bool
}

PlayableCard -- CardSides : uses
 PlayableCard --* SidedCard: is composed of 
GoldCard -- PointConditionTypes : uses 
CornerEnum -- CardSide : uses 
Card <|-- SidedCard : Inherits from
CardSide "1"--* "4" Corner: is composed of
SidedCard  "1"*--"2" CardSide : is composed of 
SidedCard  "1"<|--"1" ResourceCard : inherits from 
SidedCard "1"<|--"1" StarterCard: inherits from 
Card*-- ObjectiveCard: is composed of 

ResourceCard <|-- GoldCard : inherits from
ObjectiveCard --* Objective : is composed of
Objective ..|> GeometricObjective : realization
Objective ..|> CountingObjective : realization
Corner --|> Iterable : implements
CardSide --|> Iterable : implements




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
    -players: List~Player~ 
    -gameBoard: GameBoard
    -state: GameStates

    -currentPlayer: Player

    
    Game(int players)
    %% contstructor: creates all the game assets.

    -isGameOver() bool
    %%GameOver() void
    
    
    +addPlayer(String nickname) bool
    %% TODO: decidere come gestire il caso in cui viene rifiutata la richiesta di aggiunta di un giocatore (nickname già presente o troppi giocatori)
    

    +getGameStates() GameStates
    
    getPlayersNames()
}

class GameStates{
    <<Enumeration>>
    WAITING
    PLAYING
    GAME_OVER
}


class Deck {
    cards: Set~Card~ 
    %% Una pila forse
    Deck(Card[])
    shuffle() void
    draw() Card
    draw(int) Card []
    cardsLeft() int
}

class Player {
    nickname: String 
    %%forse va nel client o forse si può mantenere per la persistenza del client.
    points: int
    cards: SidedCards[3]
    objectiveCards: ObjectiveCard[2]
    token: Token
    board: personalBoard
    %%+chooseToken(Token [] availableTokens) Token maybe implemented in client?
    Player(personalBoard)
    getPoints() int
    setPoints(int) void
    %% points are abviously private
    playTurn() void
    drawCard() card

}

class Token {
    color: TokenColors
    Token(TokenColors)

}

class GameBoard {
    goldDeck : deck~GoldCard~
    resourceDeck: deck~ResourceCard~
    starterDeck: deck~StarterCard~
    objectiveDeck: deck~ObjectiveCard~ 
    commonBoard: CommonBoard
    scoreBoard: ScoreBoard
    GameBoard()
    %% ? +ceint[30]
}

class CommonBoard{
    goldCards: GoldCard[2]
    resourceCards: ResourceCard[2]
    objectiveCards: ObjectiveCard[2]
    
    CommonBoard()
    %% without parameters cause we'll draw 2 cards with deck.draw(2) for each type.

    drawGoldCard() GoldCard
    drawResourceCard() ResourceCard
    %% the card drawn are replaced
    getObjectiveCards() ObjectiveCard[]
}

class ScoreBoard {
    buckets: HashMap ~int~~Token~
    addPoints(TokenColors, int) void
    getPoints(TokenColors) int
    getPoints() HashMap~TokenColors, int~

}

class PlayerBoard {
    cards: SidedCard[3]
    geometry: StarterCard
    %% the geometry is a graph with root a link to the starter card
    resources: HashMap~ResourceType, int~
    objects: HashMap~Objects, int~
    placeCard(SidedCard) bool
    evaluatePoints() int
}

Game "2"--"4" Player : has
Game "1"--"1" GameBoard : is composed of
Game "1"--*"9" Token : is composed of
Token -- TokenColors : uses
GameBoard "1"--*"4" Deck : is composed of
GameBoard "1"--*"1" CommonBoard : is composed of
GameBoard "1"--*"1" ScoreBoard : is composed of

Player --|> Iterable : implements
Player --* PersonalBoard: composed of

```

## Considerazioni
La ratio è implementare ogni elemento che può diventare grafico come una classe a se stante, in modo tale che ci sia corrispondenza una volta che si implementa la view. Ogni elemento avrà un decorator toString per la realizzare la cli e un metodo per disegnarlo sulla GUI



