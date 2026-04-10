# Nightfall — Implementation TODO List

> **Project:** Nightfall — Android Multiplayer Social Deduction Game  
> **Team size:** 4 developers  
> **Duration:** 3 sprints × 2 weeks = 6 weeks  
> **Stack:** Kotlin · Jetpack Compose · Firebase RTDB · Firebase Auth · Hilt · Clean Architecture + MVVM

---

## Team Assignment Legend

| Label                           | Role                                        |
|---------------------------------|---------------------------------------------|
| **Sarca Denis-Dumitru**         | Auth, Session & User management             |
| **Istrate Camelia-Elena**       | Lobby system & Firebase sync infrastructure |
| **Ogrzeanu Alexandru Valentin** | Game engine, state machine & role engine    |
| **Calmac Stefan**               | UI/UX, Navigation, Compose screens          |

---

---

# 🏃 SPRINT 1 — Foundation & Infrastructure
### Weeks 1–2 | Goal: project compiles, Firebase is connected, auth flow works end-to-end, lobby can be created and joined

---

## Sarca Denis-Dumitru — Auth & User Infrastructure

### TASK-A1 · Bootstrap Application class
**File:** `com/nightfall/App.kt`  
**Description:** Annotate with `@HiltAndroidApp`. Call `FirebaseApp.initializeApp(this)` inside `onCreate()`. Register the app in `AndroidManifest.xml` under `android:name`.  
**Acceptance:** App launches without crash; Firebase console shows an active project connection.

---

### TASK-A2 · Domain model — User
**File:** `com/nightfall/domain/model/User.kt`  
**Description:** Define `data class User(val userId: String, val displayName: String, val email: String, val createdAt: Long)`. All fields must have default values so Firebase deserialization works.  
**Acceptance:** Unit test can instantiate `User` with and without arguments.

---

### TASK-A3 · AuthRepository interface
**File:** `com/nightfall/domain/repo/AuthRepository.kt`  
**Description:** Convert from `class` to `interface`. Define the following suspend/flow functions:
- `suspend fun login(email: String, password: String): Result<User>`
- `suspend fun register(email: String, password: String, displayName: String): Result<User>`
- `suspend fun logout()`
- `fun observeAuthState(): Flow<User?>`

**Acceptance:** Interface compiles; no implementation yet.

---

### TASK-A4 · AuthDataSource — Firebase Auth wrapper
**File:** `com/nightfall/data/firebase/AuthDataSource.kt`  
**Description:** Inject `FirebaseAuth` via Hilt. Implement:
- `suspend fun signIn(email, password): FirebaseUser` — wraps `signInWithEmailAndPassword` using `suspendCoroutine`
- `suspend fun register(email, password): FirebaseUser` — wraps `createUserWithEmailAndPassword`
- `suspend fun updateDisplayName(name: String)` — calls `userProfileChangeRequest`
- `fun getCurrentUser(): FirebaseUser?`
- `suspend fun signOut()`

All Firebase callbacks must be converted to coroutine-friendly suspend functions using `suspendCoroutine` or `suspendCancellableCoroutine`.  
**Acceptance:** Can sign in and sign out against Firebase emulator or real project.

---

### TASK-A5 · AuthRepositoryImpl
**File:** `com/nightfall/data/repo/AuthRepositoryImpl.kt`  
**Description:** Implement `AuthRepository`. Inject `AuthDataSource`. Map `FirebaseUser` → `User` domain model. Wrap all calls in `try/catch` and return `Result.Success` or `Result.Error`. Implement `observeAuthState()` using `callbackFlow` around `FirebaseAuth.addAuthStateListener`.  
**Acceptance:** Login returns a valid `User`; logout clears state; `observeAuthState()` emits `null` after logout.

---

### TASK-A6 · SessionManager
**File:** `com/nightfall/core/session/SessionManager.kt`  
**Description:** Inject `FirebaseAuth`. Expose:
- `fun isSessionActive(): Boolean` — returns `true` if `currentUser != null && !token.isExpired`
- `suspend fun restoreSession(): User?` — attempts token refresh via `currentUser.getIdToken(false)`, returns mapped `User` or `null`
- `fun getCurrentUserId(): String?`

**Acceptance:** On cold start with a stored session, `restoreSession()` returns a non-null User without showing the login screen.

---

### TASK-A7 · Auth Hilt module
**File:** `com/nightfall/di/AuthModule.kt`  
**Description:** Create `@Module @InstallIn(SingletonComponent::class)` object. Provide:
- `@Provides @Singleton fun provideFirebaseAuth(): FirebaseAuth`
- `@Binds fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository`

**Acceptance:** Hilt graph builds without errors; `AuthRepository` can be injected into a ViewModel.

---

### TASK-A8 · AuthViewModel
**File:** `com/nightfall/ui/auth/AuthViewModel.kt`  
**Description:** Annotate with `@HiltViewModel`. Inject `LoginUseCase`, `RegisterUseCase`, `SessionManager`. Expose:
- `val uiState: StateFlow<AuthUiState>` (sealed class: Idle, Loading, Success, Error)
- `fun login(email: String, password: String)`
- `fun register(email: String, password: String, displayName: String)`

Use `viewModelScope.launch` for all async calls.  
**Acceptance:** Login from UI updates `uiState` to `Success`; invalid credentials update to `Error` with message.

---

## Istrate Camelia-Elena — Firebase Infrastructure & Lobby Backend

### TASK-B1 · Domain models — Lobby & Player
**Files:** `com/nightfall/domain/model/Lobby.kt`, `com/nightfall/domain/model/Player.kt`  
**Description:**
- `data class Lobby(val lobbyId: String, val hostId: String, val gameMode: String, val status: String, val players: Map<String, Player>)`
- `data class Player(val playerId: String, val displayName: String, val isAlive: Boolean, val isConnected: Boolean, val role: String?)`

All fields with defaults for Firebase deserialization.  
**Acceptance:** Both classes instantiate correctly with partial data.

---

### TASK-B2 · Firebase path constants
**File:** `com/nightfall/util/FirebasePaths.kt`  
**Description:** Define a singleton `object FirebasePaths` with typed string constants for every RTDB node:
```
const val USERS = "users"
const val LOBBIES = "lobbies"
const val LOBBY_PLAYERS = "lobby_players"
const val GAMES = "games"
const val VOTES = "votes"
const val NIGHT_ACTIONS = "night_actions"
const val CHATS = "chats"
```
Also add helper functions like `fun lobbyPlayers(lobbyId: String) = "$LOBBY_PLAYERS/$lobbyId"`.  
**Acceptance:** All paths used across data sources reference this file — no hardcoded strings.

---

### TASK-B3 · LobbyDataSource
**File:** `com/nightfall/data/firebase/LobbyDataSource.kt`  
**Description:** Inject `FirebaseDatabase`. Implement:
- `suspend fun createLobby(lobby: LobbyDto): String` — pushes to `/lobbies`, returns generated `lobbyId`
- `suspend fun joinLobby(lobbyId: String, player: PlayerDto)` — writes to `/lobby_players/{lobbyId}/{playerId}`
- `suspend fun leaveLobby(lobbyId: String, playerId: String)` — removes player node
- `fun observeLobby(lobbyId: String): Flow<LobbyDto?>` — uses `callbackFlow` + `ValueEventListener`
- `fun observePlayers(lobbyId: String): Flow<List<PlayerDto>>` — observes `/lobby_players/{lobbyId}`
- `suspend fun setPlayerConnected(lobbyId: String, playerId: String, connected: Boolean)`
- `suspend fun migrateHost(lobbyId: String, newHostId: String)` — updates `hostId` in `/lobbies/{lobbyId}`

**Acceptance:** Create lobby in emulator, join from second instance — both observe player list update in real time.

---

### TASK-B4 · LobbyRepository interface + impl
**Files:** `com/nightfall/domain/repo/LobbyRepository.kt`, `com/nightfall/data/repo/LobbyRepositoryImpl.kt`  
**Description:** Interface defines domain-level contracts. Impl injects `LobbyDataSource` and `LobbyMapper`, delegates calls, maps DTOs to domain models. Include `onDisconnect` hook: when a player joins, register `onDisconnect().setValue(false)` on their `isConnected` node.  
**Acceptance:** Player disconnect is automatically detected and written to Firebase without any explicit logout call.

---

### TASK-B5 · DTO classes + Lobby mapper
**Files:** `com/nightfall/data/model/LobbyDto.kt`, `com/nightfall/data/model/PlayerDto.kt`, `com/nightfall/data/mappers/LobbyMapper.kt`  
**Description:** DTOs mirror Firebase JSON exactly. Mapper provides `LobbyDto.toDomain()`, `Lobby.toDto()`, `PlayerDto.toDomain()`, `Player.toDto()` extension functions.  
**Acceptance:** Round-trip test: domain → DTO → domain returns identical object.

---

### TASK-B6 · AppModule + SyncModule Hilt setup
**Files:** `com/nightfall/di/AppModule.kt`, `com/nightfall/di/SyncModule.kt`  
**Description:**
- `AppModule`: provide `FirebaseDatabase.getInstance()`, `FirebaseDatabase` reference, `SessionManager`
- `SyncModule`: bind `LobbyRepositoryImpl → LobbyRepository`

**Acceptance:** Hilt graph resolves without missing binding errors.

---

### TASK-B7 · NetworkMonitor
**File:** `com/nightfall/core/connectivity/NetworkMonitor.kt`  
**Description:** Inject `ConnectivityManager`. Expose `val isOnline: Flow<Boolean>` using `callbackFlow` + `NetworkCallback`. Emit `true`/`false` on connectivity changes.  
**Acceptance:** Turning airplane mode on/off emits the correct boolean downstream.

---

### TASK-B8 · LobbyViewModel
**File:** `com/nightfall/ui/lobby/LobbyViewModel.kt`  
**Description:** Inject `CreateLobbyUseCase`, `JoinLobbyUseCase`, `ObserveLobbyUseCase`, `SessionManager`. Expose:
- `val lobbyState: StateFlow<LobbyUiState>` (Loading, Active(lobby), Error)
- `val players: StateFlow<List<Player>>`
- `fun createLobby(gameMode: String)`
- `fun joinLobby(lobbyId: String)`
- `fun leaveLobby()`

Start observing lobby and players in `init {}` block after join/create.  
**Acceptance:** UI reflects real-time player list changes from Firebase.

---

## Ogrezeanu Alexandru Valentin — Domain Use Cases & Result wrapper

### TASK-C1 · Result sealed class
**File:** `com/nightfall/core/result/Result.kt`  
**Description:** Define:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```
Add extension functions: `Result<T>.onSuccess`, `Result<T>.onError`, `Result<T>.getOrNull()`.  
**Acceptance:** All repository functions return this type; no raw exceptions leak to ViewModels.

---

### TASK-C2 · Auth use cases
**Files:** `com/nightfall/domain/usecase/auth/LoginUseCase.kt`, `RegisterUseCase.kt`, `LogoutUseCase.kt`, `GetCurrentUserUseCase.kt`  
**Description:** Each use case is a class with a single `operator fun invoke(...)` function. Inject the relevant repository. Validate inputs (non-empty email, password min 6 chars) before calling repository. Return `Result<T>`.  
**Acceptance:** `LoginUseCase("", "")` returns `Result.Error` without making a network call.

---

### TASK-C3 · Lobby use cases
**Files:** `com/nightfall/domain/usecase/lobby/CreateLobbyUseCase.kt`, `JoinLobbyUseCase.kt`, `LeaveLobbyUseCase.kt`, `ObserveLobbyUseCase.kt`, `StartGameUseCase.kt`  
**Description:**
- `CreateLobbyUseCase`: generates a `lobbyId` (UUID), builds `Lobby` object with `status = "waiting"`, calls repo
- `JoinLobbyUseCase`: validates lobby is not full (max 15) and status is `"waiting"`, then joins
- `StartGameUseCase`: validates `players.size >= minPlayers` (from GameMode config), updates lobby status to `"in_progress"`
- `ObserveLobbyUseCase`: delegates to `LobbyRepository.observeLobby()`, returns `Flow<Lobby?>`

**Acceptance:** `JoinLobbyUseCase` on a full lobby returns `Result.Error("Lobby is full")`.

---

### TASK-C4 · Global constants
**File:** `com/nightfall/util/Constants.kt`  
**Description:** Define:
```kotlin
object Constants {
    const val MIN_PLAYERS = 5
    const val MAX_PLAYERS = 15
    const val NIGHT_PHASE_DURATION_MS = 60_000L
    const val DAY_PHASE_DURATION_MS = 120_000L
    const val VOTING_PHASE_DURATION_MS = 60_000L
    const val GAME_MODE_CLASSIC = "classic"
    const val GAME_MODE_CHAOS = "chaos"
}
```
**Acceptance:** No magic numbers in any other file in the project.

---

### TASK-C5 · Kotlin extension utilities
**File:** `com/nightfall/util/Extensions.kt`  
**Description:** Implement:
- `fun <T> Flow<T>.collectIn(scope: CoroutineScope, action: suspend (T) -> Unit)` — convenience collector
- `fun String.isValidEmail(): Boolean` — simple regex check
- `fun Map<String, Player>.aliveCount(): Int`
- `fun Map<String, Player>.mafiaCount(): Int`

**Acceptance:** Unit tests cover each extension function.

---

## Calmac Stefan — Navigation & Base UI Shell

### TASK-D1 · Screen sealed class
**File:** `com/nightfall/ui/nav/Screen.kt`  
**Description:** Define:
```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CreateLobby : Screen("create_lobby")
    object LobbyWaiting : Screen("lobby_waiting/{lobbyId}") {
        fun createRoute(lobbyId: String) = "lobby_waiting/$lobbyId"
    }
    object Game : Screen("game/{lobbyId}") {
        fun createRoute(lobbyId: String) = "game/$lobbyId"
    }
    object EndGame : Screen("end_game/{lobbyId}")
}
```
**Acceptance:** Each route is unique; no string duplication elsewhere.

---

### TASK-D2 · NavGraph
**File:** `com/nightfall/ui/nav/NavGraph.kt`  
**Description:** Set up `NavHost` with `rememberNavController()`. Wire all `composable(Screen.X.route)` entries. Handle the auth gate: if `SessionManager.isSessionActive()` is false, always navigate to `Screen.Login`; otherwise to `Screen.Home`. Pass `navController` to each screen.  
**Acceptance:** Deep link to `Screen.Home` without auth redirects to log in. After login, back button does not return to log in screen.

---

### TASK-D3 · LoginScreen + RegisterScreen
**Files:** `com/nightfall/ui/auth/LoginScreen.kt`, `com/nightfall/ui/auth/RegisterScreen.kt`  
**Description:**  
`LoginScreen`:
- Email `OutlinedTextField`, Password `OutlinedTextField` (with visibility toggle)
- "Login" button — calls `authViewModel.login(...)`
- "Don't have an account? Register" text button — navigates to Register
- Show `CircularProgressIndicator` while `uiState == Loading`
- Show `SnackBar` on `Error`

`RegisterScreen`:
- Display name, email, password fields
- "Register" button — calls `authViewModel.register(...)`
- Navigate back to Log in on success

All UI built with Jetpack Compose + Material 3.  
**Acceptance:** Full auth flow works on a real device: register → login → land on HomeScreen.

---

### TASK-D4 · HomeScreen + HomeViewModel
**Files:** `com/nightfall/ui/home/HomeScreen.kt`, `com/nightfall/ui/home/HomeViewModel.kt`  
**Description:**  
`HomeViewModel`: inject `SessionManager`, expose `val currentUser: StateFlow<User?>`, `fun logout()`  
`HomeScreen`:
- Display `"Welcome, {displayName}"`
- Two primary action buttons: "Create Lobby" and "Join Lobby" (with a lobby code input field)
- "Logout" icon in top bar

**Acceptance:** Username renders correctly; logout navigates back to LoginScreen.

---

### TASK-D5 · App theme — Nightfall branding
**Files:** `com/nightfall/ui/theme/Color.kt`, `Theme.kt`, `Type.kt`  
**Description:** Replace the default Material purple palette with a dark, atmospheric night theme:
- Primary: deep navy `#1A1A2E`
- Secondary: blood-red / dark crimson `#8B0000`
- Surface: dark grey `#16213E`
- On-surface: off-white `#E0E0E0`
- Accent: moonlight silver `#A8B2C1`

Disable `dynamicColor` (force the custom palette). Define typography scale with a bold title font for phase headers.  
**Acceptance:** All screens render with the dark theme; no Material default purple visible.

---

---

# 🏃 SPRINT 2 — Game Engine, Role System & Lobby UI
### Weeks 3–4 | Goal: full lobby flow works, game starts, state machine transitions through phases, roles are distributed

---

## Sarca Denis-Dumitru — Chat system & User profile

### TASK-A9 · Domain model — ChatMessage
**File:** `com/nightfall/domain/model/ChatMessage.kt`  
**Description:** `data class ChatMessage(val messageId: String, val lobbyId: String, val senderId: String, val senderName: String, val text: String, val timestamp: Long)`.  
**Acceptance:** Serializes/deserializes correctly from Firebase.

---

### TASK-A10 · ChatDataSource
**File:** `com/nightfall/data/firebase/ChatDataSource.kt`  
**Description:** Inject `FirebaseDatabase`. Implement:
- `suspend fun sendMessage(lobbyId: String, message: ChatMessageDto)`— pushes to `/chats/{lobbyId}`
- `fun observeMessages(lobbyId: String): Flow<List<ChatMessageDto>>` — `callbackFlow` + `ChildEventListener`, appends new messages only (do not reload full list on each event)

**Acceptance:** Messages from two clients appear in real time in under 500 ms.

---

### TASK-A11 · Chat use cases + ChatRepository
**Files:** `com/nightfall/domain/repo/ChatRepository.kt`, `SendMessageUseCase.kt`, `ObserveMessagesUseCase.kt`, `com/nightfall/data/repo/ChatRepositoryImpl.kt`  
**Description:** Interface + impl following the same pattern as Auth/Lobby. `SendMessageUseCase` validates that message text is non-empty and under 300 characters.  
**Acceptance:** Sending an empty message returns `Result.Error` without hitting Firebase.

---

### TASK-A12 · UserDataSource — write user profile to RTDB
**File:** `com/nightfall/data/firebase/UserDataSource.kt`  
**Description:** After successful registration, write the `User` record to `/users/{userId}`. Implement:
- `suspend fun createUserProfile(user: UserDto)`
- `suspend fun getUserProfile(userId: String): UserDto?`

**Acceptance:** After register, `/users/{uid}` node exists in Firebase with correct `displayName`.

---

## Istrate Camelia-Elena — Game data layer & Firebase sync for game state

### TASK-B9 · Domain models — GameState, Vote, NightAction, GamePhase
**Files:** `com/nightfall/domain/model/GameState.kt`, `Vote.kt`, `NightAction.kt`, `GamePhase.kt`  
**Description:**
- `data class GameState(val lobbyId: String, val currentPhase: GamePhase, val round: Int, val winner: String?)`
- `sealed class GamePhase { object Lobby; object Night; object Day; object Voting; object Elimination; object CheckWin; object EndGame }`
- `data class Vote(val voteId: String, val gameId: String, val voterId: String, val targetId: String)`
- `data class NightAction(val actionId: String, val gameId: String, val actorId: String, val targetId: String, val abilityType: String)`

**Acceptance:** `GamePhase` as sealed class can be matched exhaustively in a `when` block.

---

### TASK-B10 · GameDataSource
**File:** `com/nightfall/data/firebase/GameDataSource.kt`  
**Description:** Implement:
- `suspend fun initGameState(gameState: GameStateDto)` — writes to `/games/{lobbyId}`
- `fun observeGameState(lobbyId: String): Flow<GameStateDto?>` — `callbackFlow` on `/games/{lobbyId}`
- `suspend fun updatePhase(lobbyId: String, phase: String)` — writes `/games/{lobbyId}/currentPhase`
- `suspend fun submitVote(gameId: String, vote: VoteDto)` — writes to `/votes/{gameId}/{voteId}`
- `fun observeVotes(gameId: String): Flow<List<VoteDto>>` — observes `/votes/{gameId}`
- `suspend fun submitNightAction(gameId: String, action: NightActionDto)` — writes to `/night_actions/{gameId}/{actorId}` (last-write-wins per actor)
- `fun observeNightActions(gameId: String): Flow<List<NightActionDto>>`

**Acceptance:** Phase update by host client is observed by second client within 300 ms.

---

### TASK-B11 · GameRepository interface + impl
**Files:** `com/nightfall/domain/repo/GameRepository.kt`, `com/nightfall/data/repo/GameRepositoryImpl.kt`  
**Description:** Interface defines all game data operations. Impl injects `GameDataSource`, maps DTOs. Add `GameModule.kt` in `di/` to bind the implementation.  
**Acceptance:** `observeGameState()` emits correctly when phase changes in Firebase.

---

### TASK-B12 · Game use cases
**Files:** `ObserveGameStateUseCase.kt`, `SubmitVoteUseCase.kt`, `SubmitNightActionUseCase.kt`, `TransitionPhaseUseCase.kt`, `CheckWinConditionUseCase.kt`  
**Description:**
- `SubmitVoteUseCase`: validates player is alive, voting phase is active, player has not already voted (checks existing votes)
- `TransitionPhaseUseCase`: only callable by host (validates `currentUserId == hostId`), updates `currentPhase` in Firebase
- `CheckWinConditionUseCase`: takes `List<Player>`, counts alive mafia vs alive villagers — returns `"mafia"`, `"village"`, or `null`

**Acceptance:** Win condition correctly returns `"mafia"` when `mafiaCount >= villagerCount`.

---

## Ogrezeanu Alexandru Valentin — Game Engine & State Machine

### TASK-C6 · GamePhase → String serialization helper
**File:** `com/nightfall/engine/GamePhaseSerializer.kt`  
**Description:** `object GamePhaseSerializer` with `fun serialize(phase: GamePhase): String` and `fun deserialize(raw: String): GamePhase`. Used by `GameDataSource` to write/read phase from Firebase.  
**Acceptance:** Round-trip serialize/deserialize returns identical `GamePhase` for all 7 states.

---

### TASK-C7 · PhaseManager (timer)
**File:** `com/nightfall/engine/PhaseManager.kt`  
**Description:** Inject nothing (pure logic). Expose:
- `fun startTimer(durationMs: Long, onTick: (remainingMs: Long) -> Unit, onExpire: () -> Unit): Job` — uses `CoroutineScope` + `flow` to emit ticks every second
- `fun cancelTimer(job: Job)`

Timer must be cancellable. `onExpire` triggers exactly once.  
**Acceptance:** Unit test: 3-second timer calls `onTick` 3 times and `onExpire` once.

---

### TASK-C8 · VoteManager
**File:** `com/nightfall/engine/VoteManager.kt`  
**Description:** Pure logic class. Implement:
- `fun tally(votes: List<Vote>): String?` — counts votes per target, returns `targetId` of player with most votes; returns `null` on tie (no elimination)
- `fun isTie(votes: List<Vote>): Boolean`
- `fun hasAllVoted(votes: List<Vote>, alivePlayers: List<Player>): Boolean`

**Acceptance:** Unit tests: single winner detected correctly; exact tie returns `null`; three-way tie also returns `null`.

---

### TASK-C9 · WinConditionChecker
**File:** `com/nightfall/engine/WinConditionChecker.kt`  
**Description:** Pure logic class. Implement:
- `fun check(players: List<Player>): GameOutcome` where `GameOutcome` is a sealed class `{ data class Winner(val faction: String); object NoWinner }`
- Win conditions: Mafia wins if `mafiaAlive >= villagersAlive`; Village wins if `mafiaAlive == 0`

**Acceptance:** Unit tests cover: all mafia dead → village wins; mafia outnumber villagers → mafia wins; balanced → no winner.

---

### TASK-C10 · RoleDefinition interface & RoleRegistry
**Files:** `com/nightfall/roles/RoleDefinition.kt`, `com/nightfall/roles/RoleRegistry.kt`  
**Description:**
```kotlin
interface RoleDefinition {
    val roleId: String
    val displayName: String
    val faction: String        // "village" or "mafia"
    val hasNightAction: Boolean
    fun applyAbility(actor: Player, target: Player, state: GameState): GameState
    fun getRoleInfo(): String  // description shown to player
}
```
`RoleRegistry`: `object` with `val roles: Map<String, RoleDefinition>` — populated with all concrete implementations.  
**Acceptance:** `RoleRegistry.roles["mafia"]` returns non-null.

---

### TASK-C11 · Role implementations
**Files:** `com/nightfall/roles/impl/VillagerRole.kt`, `MafiaRole.kt`, `DetectiveRole.kt`, `DoctorRole.kt`  
**Description:**
- `VillagerRole`: no night action; `faction = "village"`
- `MafiaRole`: night action eliminates target (`target.isAlive = false`); `faction = "mafia"`
- `DetectiveRole`: night action reveals target's faction (stored as result in `NightAction.abilityType`); does NOT eliminate
- `DoctorRole`: night action protects target — sets a `protected` flag; if mafia targets the same player, elimination is canceled

**Acceptance:** Unit test: `MafiaRole.applyAbility(...)` returns `GameState` where target `isAlive == false`.

---

### TASK-C12 · RoleDistributor
**File:** `com/nightfall/engine/RoleDistributor.kt`  
**Description:** Implement:
- `fun distribute(players: List<Player>, gameMode: String): Map<String, String>` — returns `playerId → roleId` map
- Classic mode: 1 mafia per 3 players, 1 detective, 1 doctor, rest are villagers
- Shuffle using `players.shuffled()` to ensure randomness

**Acceptance:** For 6 players, exactly 2 mafia, 1 detective, 1 doctor, 2 villagers are assigned.

---

## Calmac Stefan — Lobby UI screens & Compose components

### TASK-D6 · CreateLobbyScreen
**File:** `com/nightfall/ui/lobby/CreateLobbyScreen.kt`  
**Description:** Compose screen with:
- Game mode selector (`RadioButton` group: Classic / Chaos)
- "Create Lobby" button — calls `lobbyViewModel.createLobby(gameMode)`
- Loading state overlay
- On success, navigate to `LobbyWaitingScreen` with the new `lobbyId`

**Acceptance:** Lobby appears in Firebase under `/lobbies` immediately after creation.

---

### TASK-D7 · JoinLobbyScreen
**File:** `com/nightfall/ui/lobby/JoinLobbyScreen.kt`  
**Description:** Compose screen with:
- `OutlinedTextField` for lobby code entry
- "Join" button — calls `lobbyViewModel.joinLobby(code)`
- Error snack bar: "Lobby full", "Game already started", "Lobby not found"

**Acceptance:** Correct error message shown for each rejection case.

---

### TASK-D8 · LobbyWaitingScreen
**File:** `com/nightfall/ui/lobby/LobbyWaitingScreen.kt`  
**Description:** Real-time Compose screen:
- `LazyColumn` displaying each `Player` with name + online indicator (green dot / gray dot based on `isConnected`)
- Lobby code displayed prominently (copyable)
- Host sees "Start Game" button (disabled if `players.size < MIN_PLAYERS`), player count badge
- Non-host sees "Waiting for host to start..."
- All players see live updates as others join

**Acceptance:** Adding a second device to the lobby shows their name within 1 second on first device.

---

### TASK-D9 · PlayerCard reusable component
**File:** `com/nightfall/ui/component/PlayerCard.kt`  
**Description:** Stateless `@Composable fun PlayerCard(player: Player, isCurrentUser: Boolean)`. Shows avatar placeholder, display name, alive/dead status icon, online indicator. Used in lobby and game screens.  
**Acceptance:** Renders correctly in both alive and eliminated states via Compose preview.

---

### TASK-D10 · PhaseTimer reusable component
**File:** `com/nightfall/ui/component/PhaseTimer.kt`  
**Description:** `@Composable fun PhaseTimer(remainingMs: Long, totalMs: Long)`. Shows a circular countdown with seconds remaining. Color shifts from green → yellow → red as time runs out (>50% green, 20-50% yellow, <20% red).  
**Acceptance:** Compose preview shows correct colors at 0%, 30%, 70% of time remaining.

---

---

# 🏃 SPRINT 3 — Game Loop, Polish & Edge Cases
### Weeks 5–6 | Goal: full game loop playable end-to-end, all edge cases handled, Firebase security rules deployed

---

## Sarca Denis-Dumitru — Chat UI & in-game communication

### TASK-A13 · DayPhaseScreen — chat integration
**File:** `com/nightfall/ui/game/DayPhaseScreen.kt`  
**Description:** Full day phase screen:
- `LazyColumn` for chat messages (`ChatBubble` component, own messages right-aligned)
- Bottom bar: `OutlinedTextField` + "Send" `IconButton`
- Player list sidebar showing who is alive (tapping a player shows their name)
- Phase timer at top (collected from `GameViewModel`)
- Scroll to bottom automatically on new message

**Acceptance:** Two devices send messages simultaneously; both see the conversation in real time with correct alignment.

---

### TASK-A14 · ChatBubble reusable component
**File:** `com/nightfall/ui/component/ChatBubble.kt`  
**Description:** `@Composable fun ChatBubble(message: ChatMessage, isOwnMessage: Boolean)`. Own messages: right-aligned, primary color background. Others: left-aligned, surface color. Show sender name above message for non-own messages. Format timestamp as `HH:mm`.  
**Acceptance:** Compose preview shows correct layout for both own and other messages.

---

### TASK-A15 · VotingScreen
**File:** `com/nightfall/ui/game/VotingScreen.kt`  
**Description:**
- `LazyColumn` of alive players, each with a "Vote" button (disabled after voting)
- Show real-time vote counts (aggregated, not per-voter — don't reveal who voted for whom)
- Phase timer countdown
- After vote submitted: button shows "Voted ✓", player cannot change vote (last-write-wins per Firebase, but UI locks)
- Own player cannot vote for themselves (button disabled)

**Acceptance:** Vote submitted updates Firebase; second device sees vote count increment within 500 ms.

---

### TASK-A16 · EliminationScreen
**File:** `com/nightfall/ui/game/EliminationScreen.kt`  
**Description:**
- Display eliminated player name and role reveal animation (simple fade-in card flip)
- If tied: show "No one was eliminated this round" message
- "Continue" button (host only) or auto-advance after 5 seconds
- Shows round number

**Acceptance:** Eliminated player's role displays correctly; tie case shows the correct message.

---

## Istrate Camelia-Elena — GameViewModel & full state machine orchestration

### TASK-B13 · GameStateMachine
**File:** `com/nightfall/engine/GameStateMachine.kt`  
**Description:** Core orchestrator. Inject `PhaseManager`, `VoteManager`, `WinConditionChecker`, `RoleDistributor`. Implement `fun transition(currentPhase: GamePhase, event: GameEvent): GamePhase` where `GameEvent` is a sealed class: `StartGame`, `NightActionsComplete`, `DayTimerExpired`, `VotingComplete`, `EliminationProcessed`, `WinnerFound`, `NoWinnerFound`. This must be a pure function — no side effects, no Firebase calls.  
**Acceptance:** Unit test the full round-trip: `Lobby → Night → Day → Voting → Elimination → CheckWin → Night` (no winner case).

---

### TASK-B14 · GameViewModel — central game orchestrator
**File:** `com/nightfall/ui/game/GameViewModel.kt`  
**Description:** The most complex ViewModel. Inject all game use cases + `GameStateMachine` + `PhaseManager` + `SessionManager`. Expose:
- `val gameState: StateFlow<GameState>`
- `val players: StateFlow<List<Player>>`
- `val currentPhase: StateFlow<GamePhase>`
- `val phaseRemainingMs: StateFlow<Long>`
- `val chatMessages: StateFlow<List<ChatMessage>>`
- `val myRole: StateFlow<RoleDefinition?>`
- `fun submitVote(targetId: String)`
- `fun submitNightAction(targetId: String)`
- `fun sendMessage(text: String)`
- `fun advancePhase()` — host only, calls `TransitionPhaseUseCase`

In `init {}`: start observing `gameState`, `players`, `chatMessages` from Firebase. On phase change: start `PhaseManager` timer. On timer expire (host only): call `advancePhase()`. On `CheckWin` phase: call `CheckWinConditionUseCase`, write winner or loop back to Night.  
**Acceptance:** Simulate a full 2-round game with 5 players using Firebase emulator — phases advance correctly and winner is determined.

---

### TASK-B15 · Night actions processing (host side)
**File:** Within `GameViewModel` or a dedicated `NightPhaseProcessor.kt`  
**Description:** When transitioning from Night → Day (host only):
1. Collect all `NightAction` records from Firebase
2. Sort by priority: Doctor protects first, Mafia eliminates second, Detective investigates third
3. Apply each action via `RoleEngine.applyAbility(...)`
4. Write updated player `isAlive` states back to `/lobby_players/{lobbyId}`
5. Clear `/night_actions/{gameId}` node

**Acceptance:** Doctor protecting a mafia target cancels the elimination — player remains alive.

---

### TASK-B16 · Firebase Security Rules
**File:** `firebase/database.rules.json`  
**Description:** Implement and deploy all rules from the ADD (section 3.3.3):
```json
{
  "rules": {
    "users": {
      "$userId": {
        ".read": "auth != null",
        ".write": "auth != null && auth.uid == $userId"
      }
    },
    "lobbies": {
      "$lobbyId": {
        ".read": "auth != null",
        ".write": "auth != null",
        "currentPhase": {
          ".write": "auth != null && data.parent().child('hostId').val() == auth.uid"
        }
      }
    },
    "lobby_players": {
      "$lobbyId": {
        ".read": "auth != null",
        "$playerId": {
          ".write": "auth != null && auth.uid == $playerId"
        }
      }
    },
    "games": {
      "$lobbyId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    },
    "night_actions": {
      "$gameId": {
        "$actorId": {
          ".write": "auth != null && auth.uid == $actorId"
        }
      }
    },
    "chats": {
      "$lobbyId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```
**Acceptance:** A player attempting to write another player's night action is rejected with permission denied.

---

## Ogrezeanu Alexandru Valentin — Night phase UI & EndGame screen

### TASK-C13 · NightPhaseScreen
**File:** `com/nightfall/ui/game/NightPhaseScreen.kt`  
**Description:**
- Dark, atmospheric UI (black background, moon icon, dimmed player list)
- Players with night actions: show a target selector list (only alive players, excluding self)
- Players without night action: show "You are sleeping..." message
- Phase timer at top
- Show role badge (bottom of screen) so player knows their role

**Acceptance:** Villager sees sleep message; Mafia sees target list with all alive non-mafia players.

---

### TASK-C14 · EndGameScreen
**File:** `com/nightfall/ui/game/EndGameScreen.kt`  
**Description:**
- Large winning faction announcement: "Village Wins!" or "Mafia Wins!" with appropriate icon
- Full player list revealed with each player's actual role
- Round count summary
- Two buttons: "Play Again" (host — resets lobby) and "Leave" (navigates to Home)

**Acceptance:** All roles revealed correctly; "Play Again" resets game state in Firebase.

---

### TASK-C15 · RoleBadge component
**File:** `com/nightfall/ui/component/RoleBadge.kt`  
**Description:** `@Composable fun RoleBadge(role: RoleDefinition, revealed: Boolean)`. When `revealed = false`: shows role name and faction icon only to the current player (used in game). When `revealed = true`: shows full role info (used in EndGame). Mafia roles use red accent; village roles use blue accent.  
**Acceptance:** Compose preview shows both revealed and hidden states correctly.

---

### TASK-C16 · Host migration on disconnect
**File:** `com/nightfall/core/session/SessionManager.kt` (extend) + `LobbyDataSource.kt` (extend)  
**Description:** In `LobbyDataSource`, when a player joins, register an `onDisconnect` handler that:
1. Sets `isConnected = false` on their player node
2. If the disconnecting player is the host, find the next player in the list and call `migrateHost(lobbyId, newHostId)`

The migration logic (finding next host) runs on the remaining clients observing the lobby: they detect `isConnected = false` on the host and the first connected player (alphabetically by `playerId`) calls `migrateHost`.  
**Acceptance:** Kill the host app process during a game — within 5 seconds, another player's UI shows host controls.

---

## Calmac Stefan — Game screens integration & final polish

### TASK-D11 · GameViewModel integration into screens
**File:** All game screens  
**Description:** Wire `GameViewModel` into `NightPhaseScreen`, `DayPhaseScreen`, `VotingScreen`, `EliminationScreen`. Each screen collects the appropriate `StateFlow` from the shared `GameViewModel` (scoped to the NavBackStackEntry for the game route so all screens share one instance). Ensure `PhaseTimer` component receives `phaseRemainingMs` from ViewModel.  
**Acceptance:** Navigating between game screens does not re-create the ViewModel or reset game state.

---

### TASK-D12 · LoadingOverlay & error handling components
**File:** `com/nightfall/ui/component/LoadingOverlay.kt`  
**Description:**
- `@Composable fun LoadingOverlay(isVisible: Boolean)` — semi-transparent dark overlay with `CircularProgressIndicator`
- `@Composable fun ErrorSnackbar(message: String?, onDismiss: () -> Unit)` — standard snack bar, auto-dismisses after 4 seconds

Apply these consistently across all screens where async operations occur.  
**Acceptance:** All loading states across the app use `LoadingOverlay`; no screen blocks UI without visual feedback.

---

### TASK-D13 · Offline / connectivity UX
**File:** All screens + `NetworkMonitor`  
**Description:** Collect `NetworkMonitor.isOnline` in a top-level `MainViewModel` (injected into `MainActivity`). When offline, show a persistent top banner: "No internet connection — reconnecting...". When connection restores, banner disappears and `SessionManager.restoreSession()` is called to re-subscribe to Firebase listeners.  
**Acceptance:** Toggling airplane mode during a game shows/hides the banner; game state is restored when reconnected.

---

### TASK-D14 · Role reveal animation
**File:** `com/nightfall/ui/game/NightPhaseScreen.kt` (role reveal dialog)  
**Description:** When the game first starts and roles are distributed, show a full-screen modal bottom sheet with an animated card flip (`AnimatedVisibility` + `graphicsLayer { rotationY }`) that reveals the player's role. Player must tap "I understand my role" to dismiss. This only appears once per game start.  
**Acceptance:** Animation plays once on game start; dismissing stores a flag in ViewModel so it doesn't replay on recomposition.

---

### TASK-D15 · End-to-end smoke test on device
**Description:** Manually run a complete game with 5 devices (or emulators). Verify:
- [ ] All 5 players can authenticate and join the same lobby
- [ ] Roles are distributed correctly (check Firebase console)
- [ ] Night phase: mafia eliminates a villager; detective investigates; doctor protects
- [ ] Day phase: chat works, timer counts down
- [ ] Voting: all players vote, elimination is processed
- [ ] Win condition triggers correctly after enough rounds
- [ ] Host disconnect triggers host migration
- [ ] EndGame shows correct roles and winner

Document any bugs found as issues. Fix any critical blockers.  
**Acceptance:** Full game completes without manual Firebase intervention.

---

## Summary Table

| Sprint   | Sarca Denis-Dumitru                                 | Istrate Camelia-Elena                                    | Ogrezeanu Alexandru Valentin                                            | Calmac Stefan                                                  |
|----------|-----------------------------------------------------|----------------------------------------------------------|-------------------------------------------------------------------------|----------------------------------------------------------------|
| Sprint 1 | App init, Auth (Firebase Auth, repo, VM, session)   | Firebase infra, Lobby backend, NetworkMonitor            | Result type, Use cases, Constants, Extensions                           | Navigation, Login/Register/Home UI, Theme                      |
| Sprint 2 | Chat system (DS, repo, use cases), User profile     | Game data layer (DS, repo, use cases), game models       | State machine components (PhaseManager, VoteManager, WinChecker, Roles) | Lobby UI screens (Create, Join, Waiting), shared components    |
| Sprint 3 | Chat UI (DayPhase), VotingScreen, EliminationScreen | GameViewModel, Night processing, Firebase security rules | NightPhaseScreen, EndGameScreen, RoleBadge, Host migration              | Screen integration, Offline UX, Loading states, E2E smoke test |

---

## Definition of Done (per task)

- Code compiles without warnings
- Unit tests written for all pure logic (use cases, engine classes)
- No hardcoded strings — all constants in `Constants.kt` or `FirebasePaths.kt`
- All Firebase calls wrapped in `try/catch`, errors surfaced via `Result.Error`
- Composables have at least one `@Preview`
- Code reviewed by one other team member before merge
- Branch merged to `develop` via Pull Request
