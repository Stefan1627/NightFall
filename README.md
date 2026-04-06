# NightFall
aplicație multiplayer Android pentru jocuri de deducție socială, în care mai mulți utilizatori participă sincronizat într-un lobby și într-o sesiune de joc bazată pe stări discrete, cu alternanță automată între fazele de noapte și zi.( a.k.a Mafia :)) ) 


A. Evită "Listen on Root"
Dacă pui un listener pe tot obiectul game_123, de fiecare dată când cineva votează, toți ceilalți 9 jucători vor descărca toată lista de jucători, tot chat-ul și toată starea.

Soluție: Creează noduri separate. Pune un listener doar pe current_phase pentru UI și unul pe votes/player_id doar când e momentul votării.

B. Structura "Shallow" (Plată)
Nu adânci JSON-ul. În loc de:
games -> id -> players -> [data]
Folosește noduri paralele:

/metadata/{gameId} (starea jocului: zi/noapte)

/players/{gameId}/{userId} (doar datele jucătorilor)

/actions/{gameId} (ultimul vot procesat)

C. Cloud Functions (Atenție la "Blaze Plan")
Pentru a face un joc de Mafia corect (unde un jucător nu poate vedea în baza de date cine e Mafia), ai nevoie de logica pe server (Cloud Functions).

Vestea proastă: Firebase cere un card bancar (Blaze Plan) pentru a folosi Cloud Functions (din cauza limitărilor de securitate Google/v2 functions).

Vestea bună: Ai un "Free Tier" generos înăuntru (primele 2M invocări sunt moca). Dacă nu vrei să introduci cardul, va trebui să folosești abordarea "Host Client" (unul din telefoane e "serverul"), care rămâne 100% gratuită pe Spark.



1. Granularitatea (Evitarea "Listen on Root")În Firebase, când pui un ValueEventListener pe un nod, primești tot ce este sub el.Greșeală de junior: Pui un listener pe games/$gameId. De fiecare dată când un cronometru scade cu o secundă, aplicația descarcă din nou numele tuturor jucătorilor, statusul lor, chat-ul, etc.Abordare de senior: Pui un ValueEventListener doar pe nodul timer.Kotlin// DOAR secundele sunt descărcate la fiecare update
database.child("games").child(gameId).child("timer").addValueEventListener(...)
2. Tehnica "Fan-out" (Data Flattening)Iată cum ar trebui să arate structura ta pentru a minimiza traficul. În loc să ai un singur obiect "Game" gigant, "împrăștii" datele:Calea în Baza de DateRolFrecvență Update/lobby/{gameId}/statusSpune dacă jocul a început sau e în așteptare.Foarte mică/game_state/{gameId}/phase"NIGHT", "DAY_DISCUSSION", "VOTING".O dată la câteva minute/timers/{gameId}Secundele rămase din faza curentă.Liniar (în fiecare secundă)/votes/{gameId}/{voterId}Cine pe cine a votat.Doar în faza de vot/chat/{gameId}Mesajele trimise (doar ziua).Mare (dar textul e mic)De ce facem asta? Jucătorii morți nu mai au nevoie să asculte /votes sau /timers, pot rămâne doar pe /chat. Economisești bandwidth la fiecare nod separat.3. Problema "Mafia trișează" (Fără Cloud Functions)Dacă vrei să rămâi pe Spark Plan (0€) și nu vrei să introduci un card pentru Cloud Functions, ai o provocare: Cum ascunzi rolurile?Dacă scrii în baza de date sub /players/id/role = "Mafia", un prieten mai "hacker" poate să deschidă un tool de sniffing sau pur și simplu un debugger și să vadă rolurile tuturor.Soluția "Host-Client" (Fără Server):Criptare ușoară: Host-ul generează rolurile, le criptează cu o cheie unică pentru fiecare jucător și le urcă în DB. Doar jucătorul X are cheia pentru a-și vedea propriul rol. (Puțin cam complicat).Noduri cu Permisiuni (Recomandat): Folosești Firebase Security Rules.Creezi un nod /roles/{gameId}/{userId}.Setezi o regulă în Firebase: "Doar userul cu UID-ul X poate citi nodul /roles/gameId/X".Astfel, chiar dacă aplicația e "curioasă", Firebase va bloca orice tentativă de a citi rolul altcuiva.JSON// Exemplu de Firebase Security Rule (simplificat)
{
  "rules": {
    "roles": {
      "$gameId": {
        "$userId": {
          ".read": "auth.uid === $userId", // Doar eu îmi văd rolul
          ".write": "auth.uid === data.parent().child('hostId').val()" // Doar host-ul scrie
        }
      }
    }
  }
}

4. Alternativa Ktor (Local) - Realitatea crudă
Dacă alegi un server de Ktor pe laptopul tău:

Problema IP-ului: Prietenii tăi nu se pot conecta la 192.168.1.x. Va trebui să folosești un serviciu ca ngrok (care are limite la varianta free) sau să faci port-forwarding în router (nesigur și complicat pentru unii).

Stabilitate: Dacă îți pică netul acasă sau se închide laptopul, tot jocul îngheață.
