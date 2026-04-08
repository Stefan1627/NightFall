# Nightfall 🌙

A real-time multiplayer social deduction game for Android (5–15 players), built with Kotlin, Jetpack Compose, Firebase, and Clean Architecture.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Module Breakdown](#module-breakdown)
- [Data Flow](#data-flow)
- [Firebase Database Schema](#firebase-database-schema)
- [Game State Machine](#game-state-machine)
- [Getting Started](#getting-started)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| Async | Kotlin Coroutines + Flow |
| DI | Hilt |
| Backend / Sync | Firebase Realtime Database |
| Auth | Firebase Authentication |
| Navigation | Jetpack Navigation Compose |
| Build | Gradle (Kotlin DSL) |
| Min SDK | API 26 (Android 8.0) |

---

## Architecture Overview

The project follows **Clean Architecture** with three distinct layers, combined with the **MVVM** pattern on the presentation layer. All game state is persisted and synchronized through **Firebase Realtime Database**, which acts as the central message bus between all connected clients.

```
┌─────────────────────────────────────────────┐
│              Presentation Layer              │
│   Compose UI  ←→  ViewModel  ←→  UseCase    │
├─────────────────────────────────────────────┤
│               Domain Layer                  │
│     Repository Interfaces + Use Cases       │
├─────────────────────────────────────────────┤
│                Data Layer                   │
│   Firebase DataSources + Repo Impls         │
├─────────────────────────────────────────────┤
│             External Services               │
│   Firebase Realtime DB | Firebase Auth      │
└─────────────────────────────────────────────┘
```

---

## Project Structure

```
com/nightfall/
│
├── App.kt                              # Application class — Hilt + Firebase init
├── MainActivity.kt                     # Single activity host — NavHost entry point
│
├── di/                                 # Dependency Injection (Hilt modules)
│   ├── AppModule.kt                    # Firebase, SessionManager bindings
│   ├── AuthModule.kt                   # Auth repository + data source bindings
│   ├── GameModule.kt                   # Game engine, role engine bindings
│   └── SyncModule.kt                   # Sync repository bindings
│
├── domain/                             # Pure Kotlin — no Android, no Firebase
│   ├── model/                          # Business entities
│   │   ├── User.kt
│   │   ├── Player.kt
│   │   ├── Lobby.kt
│   │   ├── GameState.kt
│   │   ├── Role.kt
│   │   ├── Vote.kt
│   │   ├── ChatMessage.kt
│   │   ├── NightAction.kt
│   │   └── GamePhase.kt                # Sealed class: Lobby, Night, Day, Voting, etc.
│   │
│   ├── repo/                           # Repository interfaces
│   │   ├── AuthRepository.kt
│   │   ├── LobbyRepository.kt
│   │   ├── GameRepository.kt
│   │   └── ChatRepository.kt
│   │
│   └── usecase/                        # One class per use case
│       ├── auth/
│       │   ├── LoginUseCase.kt
│       │   ├── RegisterUseCase.kt
│       │   ├── LogoutUseCase.kt
│       │   └── GetCurrentUserUseCase.kt
│       ├── lobby/
│       │   ├── CreateLobbyUseCase.kt
│       │   ├── JoinLobbyUseCase.kt
│       │   ├── LeaveLobbyUseCase.kt
│       │   ├── StartGameUseCase.kt
│       │   └── ObserveLobbyUseCase.kt
│       ├── game/
│       │   ├── ObserveGameStateUseCase.kt
│       │   ├── SubmitVoteUseCase.kt
│       │   ├── SubmitNightActionUseCase.kt
│       │   ├── TransitionPhaseUseCase.kt
│       │   └── CheckWinConditionUseCase.kt
│       └── chat/
│           ├── SendMessageUseCase.kt
│           └── ObserveMessagesUseCase.kt
│
├── data/                               # Firebase implementations
│   ├── model/                          # Firebase DTOs (separate from domain models)
│   │   ├── UserDto.kt
│   │   ├── PlayerDto.kt
│   │   ├── LobbyDto.kt
│   │   ├── GameStateDto.kt
│   │   ├── VoteDto.kt
│   │   ├── NightActionDto.kt
│   │   └── ChatMessageDto.kt
│   │
│   ├── mappers/                        # DTO ↔ Domain model converters
│   │   ├── UserMapper.kt
│   │   ├── LobbyMapper.kt
│   │   ├── GameStateMapper.kt
│   │   └── ChatMapper.kt
│   │
│   ├── firebase/                       # Raw Firebase data sources
│   │   ├── AuthDataSource.kt           # Firebase Auth calls
│   │   ├── LobbyDataSource.kt          # RTDB: /lobbies, /lobby_players
│   │   ├── GameDataSource.kt           # RTDB: /games, /votes, /night_actions
│   │   └── ChatDataSource.kt           # RTDB: /chats
│   │
│   └── repo/                           # Repository implementations
│       ├── AuthRepositoryImpl.kt
│       ├── LobbyRepositoryImpl.kt
│       ├── GameRepositoryImpl.kt
│       └── ChatRepositoryImpl.kt
│
├── engine/                             # Game logic — no Android, no Firebase
│   ├── GameStateMachine.kt             # Orchestrates phase transitions
│   ├── PhaseManager.kt                 # Timer management per phase
│   ├── VoteManager.kt                  # Vote tallying + tie resolution
│   ├── WinConditionChecker.kt          # Evaluates end-game conditions
│   └── RoleDistributor.kt             # Random role assignment algorithm
│
├── roles/                              # Role Engine — isolated, extensible
│   ├── RoleEngine.kt                   # Applies abilities; entry point for ViewModel
│   ├── RoleDefinition.kt               # Interface / contract for all roles
│   ├── impl/
│   │   ├── VillagerRole.kt
│   │   ├── MafiaRole.kt
│   │   ├── DetectiveRole.kt
│   │   ├── DoctorRole.kt
│   │   └── NarratorRole.kt
│   └── RoleRegistry.kt                 # Maps roleId → RoleDefinition
│
├── core/                               # Cross-cutting infrastructure
│   ├── session/
│   │   └── SessionManager.kt           # Token restore, auth state observation
│   ├── connectivity/
│   │   └── NetworkMonitor.kt           # Detects online/offline; drives reconnect UX
│   └── result/
│       └── Result.kt                   # Sealed class: Success, Error, Loading
│
├── ui/                                 # Presentation layer
│   ├── theme/
│   │   ├── Color.kt                    # Brand palette (night/dark theme)
│   │   ├── Theme.kt                    # MaterialTheme configuration
│   │   └── Type.kt                     # Typography scale
│   │
│   ├── nav/
│   │   ├── NavGraph.kt                 # Compose NavHost + all routes
│   │   └── Screen.kt                   # Sealed class of screen routes
│   │
│   ├── component/                      # Reusable, stateless composables
│   │   ├── PlayerCard.kt
│   │   ├── PhaseTimer.kt
│   │   ├── VoteSlider.kt
│   │   ├── ChatBubble.kt
│   │   ├── RoleBadge.kt
│   │   └── LoadingOverlay.kt
│   │
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   ├── LoginScreen.kt
│   │   └── RegisterScreen.kt
│   │
│   ├── lobby/
│   │   ├── LobbyViewModel.kt
│   │   ├── CreateLobbyScreen.kt
│   │   ├── JoinLobbyScreen.kt
│   │   └── LobbyWaitingScreen.kt
│   │
│   ├── game/
│   │   ├── GameViewModel.kt            # Central game orchestrator; hosts state machine
│   │   ├── NightPhaseScreen.kt
│   │   ├── DayPhaseScreen.kt
│   │   ├── VotingScreen.kt
│   │   ├── EliminationScreen.kt
│   │   └── EndGameScreen.kt
│   │
│   └── home/
│       ├── HomeViewModel.kt
│       └── HomeScreen.kt
│
└── util/                               # Kotlin helpers and constants
    ├── Constants.kt                    # Phase durations, min players, Firebase paths
    ├── Extensions.kt                   # Kotlin extension functions
    ├── CoroutineUtils.kt               # Dispatcher providers, scope helpers
    └── FirebasePaths.kt                # Typed path constants for RTDB nodes
```

---

## Module Breakdown

### `domain/`
Pure Kotlin with zero Android or Firebase imports. Contains the business rules, repository contracts, and use cases. This is the most stable layer — it should almost never change due to infrastructure decisions.

### `data/`
Firebase implementations of the domain interfaces. DTOs map Firebase JSON to domain models via the `mappers/` package. Each `DataSource` wraps raw Firebase SDK calls, and each `RepositoryImpl` combines one or more data sources.

### `engine/`
Stateless game logic that runs on the host client. The `GameStateMachine` drives phase transitions; it reads the current game state and writes the next phase to Firebase via the `GameRepository`. It has no direct dependency on Firebase — it works through the domain repository interfaces.

### `roles/`
Each role is an isolated implementation of `RoleDefinition`. The `RoleEngine` accepts an actor, a target, and the current `GameState`, delegates to the correct role implementation, and returns an updated `GameState`. New roles are added by creating a new `impl/` class and registering it in `RoleRegistry` — no other code changes required.

### `core/`
Infrastructure utilities shared across layers. `SessionManager` handles Firebase token persistence and restores sessions on cold start. `NetworkMonitor` emits connectivity events so ViewModels can surface offline banners. `Result<T>` is a common sealed wrapper used across all layers.

### `ui/`
MVVM: each screen folder contains a `ViewModel` and one or more `@Composable` screen functions. ViewModels expose `StateFlow` / `SharedFlow`; screens collect them with `collectAsStateWithLifecycle()`. No business logic lives in composables.

---

## Data Flow

```
User Action (Compose UI)
        │
        ▼
   ViewModel
  (StateFlow / event handling)
        │
        ▼
    UseCase
  (domain logic, validation)
        │
        ▼
  RepositoryImpl          ◄──── observes ────► Firebase RTDB
  (data layer)                                  (push updates)
        │
        ▼
  DataSource (Firebase SDK)
```

Firebase pushes state updates to all connected clients simultaneously. The `GameRepository` exposes `Flow<GameState>`, which propagates through the use case to the `GameViewModel` and then to the UI.

---

## Firebase Database Schema

```
/
├── users/
│   └── {userId}/
│       ├── displayName     : String
│       ├── email           : String
│       └── createdAt       : Timestamp
│
├── lobbies/
│   └── {lobbyId}/
│       ├── hostId          : String
│       ├── gameMode        : String
│       └── status          : String        # "waiting" | "in_progress" | "finished"
│
├── lobby_players/
│   └── {lobbyId}/
│       └── {playerId}/
│           ├── displayName : String
│           ├── isAlive     : Boolean
│           ├── isConnected : Boolean
│           └── role        : String        # null until game starts
│
├── games/
│   └── {lobbyId}/
│       ├── currentPhase    : String        # "night" | "day" | "voting" | "elimination" | "end"
│       ├── round           : Int
│       └── winner          : String        # null until game ends
│
├── votes/
│   └── {gameId}/
│       └── {voteId}/
│           ├── voterId     : String
│           └── targetId    : String
│
├── night_actions/
│   └── {gameId}/
│       └── {actorId}/
│           ├── targetId    : String
│           └── abilityType : String
│
└── chats/
    └── {lobbyId}/
        └── {messageId}/
            ├── senderId    : String
            ├── text        : String
            └── timestamp   : Timestamp
```

---

## Game State Machine

```
         ┌──────────────────────┐
    ●───►│        Lobby         │◄─────────────────────────────┐
         │ waiting for players  │                              │
         └────────┬─────────────┘                              │
                  │ host starts game                           │
                  ▼                                            │
         ┌──────────────────────┐                              │
         │    RoleAssignment    │                              │
         │  roles distributed   │                              │
         └────────┬─────────────┘                              │
                  │ roles assigned                             │
                  ▼                                            │
         ┌──────────────────────┐                              │
    ┌───►│        Night         │                              │
    │    │ special roles act    │                              │
    │    └────────┬─────────────┘                              │
    │             │ actions done / timeout                     │
    │             ▼                                            │
    │    ┌──────────────────────┐                              │
    │    │    DayDiscussion     │                              │
    │    │  players debate      │                              │
    │    └────────┬─────────────┘                              │
    │             │ timer expires                              │
    │             ▼                                            │
    │    ┌──────────────────────┐                              │
    │    │       Voting         │                              │
    │    │  synchronized vote   │                              │
    │    └────────┬─────────────┘                              │
    │             │ all voted / timeout                        │
    │             ▼                                            │
    │    ┌──────────────────────┐                              │
    │    │     Elimination      │                              │
    │    │  tally + eliminate   │                              │
    │    └────────┬─────────────┘                              │
    │             │ state updated                              │
    │             ▼                                            │
    │    ┌──────────────────────┐    winner found    ┌─────────┴──────────┐
    │    │      CheckWin        │───────────────────►│      EndGame       │──►●
    │    │  evaluate conditions │                    │  reveal roles      │
    │    └────────┬─────────────┘                    └────────────────────┘
    │             │ no winner yet
    └─────────────┘ continue to next round
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17+
- A Firebase project with Realtime Database and Authentication enabled
- `google-services.json` placed in `app/`

### Setup

```bash
git clone https://github.com/your-org/nightfall.git
cd nightfall
# Add your google-services.json to app/
./gradlew assembleDebug
```

### Firebase Rules

Deploy the security rules from `firebase/database.rules.json` before running in a shared environment:

```bash
firebase deploy --only database
```

### Running Tests

```bash
# Unit tests (domain + engine layers — no emulator needed)
./gradlew test

# Instrumented tests (Firebase emulator or connected device)
./gradlew connectedAndroidTest
```

---

## Key Architectural Decisions

**Why Clean Architecture?** It isolates the game logic from Firebase entirely. The `engine/` and `domain/` layers can be unit-tested without any Android or Firebase dependencies, and swapping Firebase for another backend later requires only touching the `data/` layer.

**Why no dedicated server?** Firebase RTDB acts as the synchronization bus, reducing infrastructure cost and complexity. The trade-off is that the host client drives phase transitions — a limitation mitigated by host migration on disconnect (see `SessionManager`).

**Why is `RoleEngine` separate from `GameStateMachine`?** The state machine governs *when* things happen; the role engine governs *what* happens for a specific role. This separation means adding a new role never requires touching the phase transition logic.
