# Regole 
Si riporta a fini implementativi una roadmap e todo list delle regole da realizzare.

## Panoramica
- [ ] Primo giocatore che fa 20 punti innesca la lfine della partita
- Il gioco ha un metodo che itera sui giocatori per controllare ad ogni turno che nessuno abbia finito la partita. se finisce la partita si

- TODO: decidere se la classe player ha l'abilità di giocare un turno e il gioco ha un metodo che itera sui giocatori per farli giocare a turno? (round) e se il la classe client avrà accesso ad una di queste funzionalità del modello. 

## Preparazione
- [ ] (1) Viene istanziata la GameBord dal gioco.
- TODO: come implementare la gameboard? Classe astratta? un array da 30? ogni giocatore ha la sua posizione? bohhhh
- [ ] (2) Mescolare le carte risorsa, oro
- [ ] Scoprirne due sul tavolo (sono da intendersi come di tutti)
- TODO Implementare un metodo perchè **il gioco** mescoli le carte. 
- Decidere come vengono gestite le carte sul tavolo. 
- IDEA Classe commonArea che ha dei metodi per vedere le carte e per pescarle? Ovviamente il metodo pesca andrà a ripmpiazzare la carta pescata con una nuova invocando il metodo pesca del mazzo.
- [ ] (3) Ogni giocatore **riceve** una carta iniziale 
- (implementare metodo per ricevere carte da parte del giocatore e per pescarne una da parte del tavolo)
- [ ] Ogni giocatore pesca 2 carte risorsa e 1 oro
- [ ] Ogni giocatore sceglie un colore, il gioco posiziona uno dei due segnalini sul tabellone, l'altro viene posizionato sulla sua carta iniziale dal giocatore.
-  [ ] (4) Il gioco mescola le carte obiettivo, le prime due vengono posizionate scoperte sulla zona comune. sono da intendersi come obiettivi comuni per cui tutti i giocatori riceveranno punti
- [ ] (5) Ogni giocatore riceve due carte obiettivo personali da mantenere segrete.
- [ ] (6) Viene estratto a caso il primo giocatore, il segnalino nero viene posto sulla sua carta iniziale 
    - TODO: decidere se il segnalino viene memorizzato sulla carta o dal gioco, dal gioco sarebbe più easy.

## Flusso di gioco

Ogni giocatore
- [ ] (1) Gioca una carta
- [ ] (2) Pesca una carta dalla area comune odal mazzo, se pesca dall'area comune questa viene ovviamente rimpiazzata.


### Giocare una carta
- [ ] Il giocatore sceglie una delle 3 carte che ha in mano e la gioca.
#### Piazzamento di una carta
- TODO: decidere come strutturare l'area di gioco. Che struttura dati usiamo? come verifichiamo se una carta si può piazzare lì? a partire dalle carte già a terra tenendo a mente che formerà quella sorta di spina di pesce? 
- [ ] Un angolo vuoto che copre un angolo pieno cancella la presenza della risorsa coperta sull'area di gioco.  
    - TODO: come teniamo conto di che cosa rimane visibile?
    - IDEA: teniamo una dizionario int con indice la enum delle risorse per tener conto del numero di risorse di ogni giocatore? Comodo per le carte oro che richiedono risorse per essere piazzate o per le carte obiettivo che richiedono di contare le cose che si hanno. lo si aggiorna quando si piazza la carta e non va ricontrollato più.
- [ ] Una carta può coprire solo un angolo visibile di un'altra carta.
    - [ ] Gli angoli non visibili non possono essere coperti.
- [ ] Se copre due angoli dallo stesso lato, i due angoli appartengono a carte diverse.
    - TODO una carta si può piazzare in modo che si sovrapponga a due carte (una alla sua destra e una alla sua sinistra)
    - IDEA: una proprietà effective corner opzionale che contenga l'effetttivo valore in modo da contare una volta sola per carta contando solo i punti a sx?
- [ ] Un angolo non visibile può coprirne uno visibile.

- TODO: decidedere se  vogliamo trattare gli errori di piazzamento come eccezioni? (non so ma direi di no)
<!-- - Trattiamo come eccezioni tutti gli errori di gioco?  -->

##### piazzamento carte oro
- [ ] le risorse devono essere presenti prima del piazzamento della carta oro ma possono essere coperte al piazzamento, se sono coperte vanno decurtate dalla area di gioco personale. 

#### Aggiornamento dei punti
- se una carta viene piazzata vanno aggiornate 
    - [ ] le risorse disponibili
    - [ ] gli oggetti disponibili 
    - [ ] i punti (se la carta ne porta al piazzamento) 

### Pesca di una carta
- [ ] Il giocatore a fine turno sceglie se pescare una carta dal mazzo o dall'area comune. usando i rispettivi metodi.
-


## Fine della partita
- [ ] Si controlla ad ogni turno se un giocatore ha raggiunto 20 punti, in tal caso si fa giocare un ultimo turno a tutti i giocatori
- [ ] dopodichè vengono calcolati i punti dati dalle carte obiettivo e si dichiara il vincitore.
- IDEA: per gli obiettivi utilizzare la classe astratta e implementarne due diversi:
    -"geometrici" si usa una funzione che calcoli ricorsivamente con una finestra di massimo 3 carte (ma in realtà il valore della carta che definiremo dal costruttore dell'obiettivo) se la spina di pesce va su o giù (parte da 0, su +1, giù -1 con la finestra di quelle carte), se trova si ritorna true
    - di conteggio: gli attributi sono i set di risorse che devono essere presenti per ottenere i punti.  


## TODO Requisiti game agnostic

## TODO Funzionalità avanzate.