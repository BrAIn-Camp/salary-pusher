# Salary Pusher — Build Order

This document defines the order in which every component of the application should be
built. Each task is sized to be completable by a single AI agent in one session.
Tasks are ordered by dependency — nothing in a task should require something that
hasn't been built in a prior task.

**Before starting any task:** Read `NON-NEGOTIABLES.md` in full.
**After completing any task:** The project must build cleanly with no errors.
**Checkpoints** are marked with 🔍 — at these points, run all tests and verify
the app behaves correctly before proceeding.

---

## Phase 1 — Project Foundation

These tasks establish the skeleton that every future task builds on.
No game logic. No UI. Just structure.

---

### Task 1.1 — Android Project Initialization

**Goal:** Create the Android Studio project with the correct configuration.

**Steps:**
- Create a new Android Studio project
  - Template: Empty Activity (Jetpack Compose)
  - Package name: `com.braincamp.salarypusher`
  - Language: Kotlin
  - Minimum SDK: API 26 (Android 8.0)
  - Target SDK: API 35
  - Build system: Gradle (Kotlin DSL — `build.gradle.kts`)
- Verify the default Compose app builds and runs on emulator

**Deliverable:** A running Hello World Compose app committed to the repo.

**Tests:** None at this stage — nothing to test yet.

---

### Task 1.2 — Dependency Setup

**Goal:** Add all third-party dependencies to `build.gradle.kts` so every future task
can import what it needs without touching Gradle again.

**Dependencies to add:**

```kotlin
// SceneView — 3D rendering and physics
implementation("io.github.sceneview:sceneview:4.25.0")

// Room — local database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// WorkManager — notification scheduling
implementation("androidx.work:work-runtime-ktx:2.9.0")

// ViewModel + StateFlow
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

// DataStore — lightweight preferences (for simple settings)
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Accompanist Permissions — Compose permission handling
implementation("com.google.accompanist:accompanist-permissions:0.34.0")

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("io.mockk:mockk:1.13.10")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

**Deliverable:** Project still builds cleanly with all dependencies resolved.

**Tests:** Run a trivial unit test (`1 + 1 == 2`) to confirm the test infrastructure works.

---

### Task 1.3 — Package Structure

**Goal:** Create the empty package folders that define the app's architecture.
No code yet — just the folder structure so every future agent knows where things live.

```
com.braincamp.salarypusher
├── data
│   ├── db              # Room database, DAOs, entities
│   ├── repository      # Repository classes
│   └── datastore       # DataStore preferences
├── domain
│   ├── model           # Pure Kotlin data models (no Android deps)
│   └── usecase         # Business logic use cases
├── game
│   ├── scene           # SceneView scene setup
│   ├── physics         # Physics constants and body definitions
│   └── engine          # Game loop, coin state management
├── notifications
│   ├── channels        # Channel definitions
│   ├── builders        # Notification builders per type
│   └── scheduler       # WorkManager workers and scheduling
├── ui
│   ├── onboarding      # Onboarding screens
│   ├── game            # Main game screen
│   ├── earnings        # Earnings display screen
│   ├── settings        # Settings screen
│   ├── debug           # Debug notification screen
│   ├── theme           # Compose theme, colors, typography
│   └── components      # Shared reusable composables
└── util                # Extension functions, constants, helpers
```

**Deliverable:** All packages created, project still builds.

**Tests:** None.

---

### Task 1.4 — Navigation Setup

**Goal:** Establish the Compose Navigation graph with placeholder screens so the
full app skeleton can be navigated end-to-end before any real content exists.

**Routes to define:**
```kotlin
object Routes {
    const val ONBOARDING_WELCOME = "onboarding/welcome"
    const val ONBOARDING_SALARY = "onboarding/salary"
    const val ONBOARDING_SCHEDULE = "onboarding/schedule"
    const val ONBOARDING_DENOMINATION = "onboarding/denomination"
    const val ONBOARDING_NOTIFICATIONS = "onboarding/notifications"
    const val GAME = "game"
    const val EARNINGS = "earnings"
    const val SETTINGS = "settings"
    const val DEBUG_NOTIFICATIONS = "debug/notifications"
}
```

- Each route maps to a placeholder `Text("Screen name")` composable for now
- Navigation from Welcome → Salary → Schedule → Denomination → Notifications → Game
- Game screen has navigation icons to Earnings, Settings, and Debug screens
- Onboarding is only shown if it hasn't been completed (check DataStore flag)

**Deliverable:** Full app skeleton navigable end-to-end on emulator.

**Compose UI Test:** Verify tapping through onboarding flow reaches the game screen.

---

### 🔍 Checkpoint 1 — Foundation Verified

Before proceeding to Phase 2:
- [ ] App builds with zero errors and zero warnings (except expected experimental APIs)
- [ ] All dependencies resolve
- [ ] Navigation skeleton works end-to-end on emulator
- [ ] Package structure matches the spec above
- [ ] Test infrastructure confirmed working (at least one passing unit test)
- [ ] Code committed and pushed to `main`

---

## Phase 2 — Data Layer

Build the entire data layer before any UI or game logic depends on it.

---

### Task 2.1 — Domain Models

**Goal:** Define the pure Kotlin data models that represent the app's core concepts.
No Android dependencies in this package.

**Models to create:**

```kotlin
// The player's work configuration
data class WorkProfile(
    val hourlySalary: Double,           // Always stored as hourly rate
    val workDays: Set<DayOfWeek>,
    val shiftStartHour: Int,            // 0-23
    val shiftStartMinute: Int,          // 0-59
    val shiftEndHour: Int,
    val shiftEndMinute: Int,
    val coinDenomination: CoinDenomination
)

// Available coin denominations
enum class CoinDenomination(val valueInCents: Int, val displayName: String) {
    PENNY(1, "Penny"),
    NICKEL(5, "Nickel"),
    DIME(10, "Dime"),
    QUARTER(25, "Quarter"),
    DOLLAR(100, "Dollar")
}

// A single earning event
data class EarningEvent(
    val id: Long = 0,
    val amountCents: Long,
    val timestamp: Long,                // epoch millis
    val denomination: CoinDenomination
)

// Aggregated earnings summary
data class EarningsSummary(
    val todayTotalCents: Long,
    val weekTotalCents: Long,
    val allTimeTotalCents: Long
)

// Represents a coin in the game world (not persisted)
data class CoinState(
    val id: String,                     // UUID
    val denomination: CoinDenomination,
    val isInDropQueue: Boolean,
    val isOnPlatform: Boolean
)
```

**Deliverable:** All models created in `domain/model/`.

**Unit Tests:**
- `CoinDenomination` values are correct (QUARTER.valueInCents == 25, etc.)
- `WorkProfile` correctly identifies if a given `LocalDateTime` falls within shift hours
  (this logic lives as an extension function on `WorkProfile`)

---

### Task 2.2 — Room Database Setup

**Goal:** Create the Room database with entities and DAOs for persisting earnings.

**Entity:**
```kotlin
@Entity(tableName = "earning_events")
data class EarningEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val timestamp: Long,
    val denominationOrdinal: Int        // CoinDenomination.ordinal
)
```

**DAO:**
```kotlin
@Dao
interface EarningEventDao {
    @Insert
    suspend fun insert(event: EarningEventEntity): Long

    @Query("SELECT * FROM earning_events WHERE timestamp >= :fromTimestamp ORDER BY timestamp DESC")
    fun getEventsSince(fromTimestamp: Long): Flow<List<EarningEventEntity>>

    @Query("SELECT SUM(amountCents) FROM earning_events WHERE timestamp >= :fromTimestamp")
    suspend fun sumSince(fromTimestamp: Long): Long?

    @Query("SELECT SUM(amountCents) FROM earning_events")
    suspend fun sumAll(): Long?
}
```

**Database class:** `SalaryPusherDatabase` with the single entity and DAO.

**Deliverable:** Room database compiles and can be instantiated in a test.

**Unit Tests:**
- Insert an earning event and verify it can be retrieved
- Verify `sumSince` returns correct totals for a set of test events
- Verify the DAO correctly filters by timestamp

---

### Task 2.3 — DataStore Preferences

**Goal:** Create a typed DataStore wrapper for simple app preferences that don't
need relational storage.

**Preferences to store:**
```kotlin
object PreferenceKeys {
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val HOURLY_SALARY_CENTS = longPreferencesKey("hourly_salary_cents")
    val WORK_DAYS = stringPreferencesKey("work_days")       // JSON serialized Set<Int>
    val SHIFT_START_HOUR = intPreferencesKey("shift_start_hour")
    val SHIFT_START_MINUTE = intPreferencesKey("shift_start_minute")
    val SHIFT_END_HOUR = intPreferencesKey("shift_end_hour")
    val SHIFT_END_MINUTE = intPreferencesKey("shift_end_minute")
    val COIN_DENOMINATION = intPreferencesKey("coin_denomination") // ordinal
    val NOTIF_SHIFT_START = booleanPreferencesKey("notif_shift_start")
    val NOTIF_COINS_ACCRUING = booleanPreferencesKey("notif_coins_accruing")
    val NOTIF_BOARD_FULL = booleanPreferencesKey("notif_board_full")
    val NOTIF_EARNINGS_SUMMARY = booleanPreferencesKey("notif_earnings_summary")
    val NOTIF_LUNCH_NUDGE = booleanPreferencesKey("notif_lunch_nudge")
    val NOTIF_WEEKLY_PAYDAY = booleanPreferencesKey("notif_weekly_payday")
}
```

**Create:** `UserPreferencesRepository` — a class that wraps DataStore and exposes
typed read/write functions with sensible defaults.

**Deliverable:** Preferences can be written and read in isolation.

**Unit Tests:**
- Write a preference value and verify it reads back correctly
- Verify default values are returned when no value has been set
- Verify `WorkProfile` can be fully reconstructed from stored preferences

---

### Task 2.4 — Repository Layer

**Goal:** Create repository classes that abstract the data sources from the rest of
the app. ViewModels talk to repositories, never directly to DAOs or DataStore.

**Repositories:**

`EarningsRepository`
- `suspend fun recordEarning(amountCents: Long, denomination: CoinDenomination)`
- `fun getEarningsSummary(): Flow<EarningsSummary>` — emits today/week/alltime totals
- Uses midnight and Monday-midnight as timestamp boundaries

`WorkProfileRepository`
- `fun getWorkProfile(): Flow<WorkProfile?>` — null if onboarding not complete
- `suspend fun saveWorkProfile(profile: WorkProfile)`
- `suspend fun setOnboardingComplete()`
- `fun isOnboardingComplete(): Flow<Boolean>`

**Deliverable:** Both repositories created and injectable via constructor.

**Unit Tests:**
- `EarningsRepository.getEarningsSummary()` returns correct today/week/alltime splits
  given a known set of seeded earning events at known timestamps
- `WorkProfileRepository` round-trips a `WorkProfile` through save and retrieve

---

### 🔍 Checkpoint 2 — Data Layer Verified

Before proceeding to Phase 3:
- [ ] All domain models exist and are unit tested
- [ ] Room database compiles and DAO operations are tested
- [ ] DataStore reads and writes correctly
- [ ] Both repositories are tested
- [ ] `WorkProfile.isCurrentlyWorkHours()` is tested for edge cases:
  - During shift ✅
  - Before shift ✅
  - After shift ✅
  - On a non-work day ✅
  - Exactly at shift start boundary ✅
  - Exactly at shift end boundary ✅
- [ ] Project builds clean, all tests pass, committed and pushed

---

## Phase 3 — Salary Clock Engine

The salary clock is the heart of the game. It runs during work hours and feeds
coins into the drop queue. Build and test this in complete isolation from the UI.

---

### Task 3.1 — Coin Accrual Calculator

**Goal:** A pure Kotlin class that calculates how many coins of a given denomination
should have accrued between two timestamps given a salary rate.

```kotlin
class CoinAccrualCalculator {
    fun calculateAccruedCoins(
        fromTimestamp: Long,
        toTimestamp: Long,
        workProfile: WorkProfile
    ): Int

    fun calculateAccruedCents(
        fromTimestamp: Long,
        toTimestamp: Long,
        workProfile: WorkProfile
    ): Long
}
```

Key behavior:
- Only counts time that falls within defined work hours
- Correctly handles the boundary between two days mid-calculation
- Result is always a whole number of coins (floor division)
- Remainder cents carry over (tracked separately) so no earnings are lost
  due to rounding

**Unit Tests** (these are the most important tests in the project):
- 1 hour at $20/hr with quarters = 80 coins
- 30 minutes at $60/hr with dollars = 30 coins
- A 2-hour window that spans a shift boundary (1hr in-shift, 1hr out) = only in-shift coins
- A window entirely outside work hours = 0 coins
- A window on a non-work day = 0 coins
- Fractional cent remainder does not produce phantom coins
- Two back-to-back windows produce the same total as one combined window (no loss)

---

### Task 3.2 — Salary Clock Service

**Goal:** A background-aware coroutine-based clock that ticks during work hours
and emits the number of coins earned since the last tick.

```kotlin
class SalaryClock(
    private val calculator: CoinAccrualCalculator,
    private val workProfileRepository: WorkProfileRepository
) {
    // Emits accrued coin count every tick interval during work hours
    // Emits 0 (or nothing) outside work hours
    val coinAccrualFlow: Flow<Int>

    // The timestamp of the last tick (persisted so offline time is accounted for)
    suspend fun recordTickTime()
}
```

- Tick interval: every 60 seconds while app is in foreground
- When app is reopened after being closed during work hours, calculates
  coins accrued during the gap using `CoinAccrualCalculator`
- Last tick time is persisted in DataStore

**Unit Tests:**
- Reopening after a 30-minute gap during work hours produces the correct coin count
- Reopening after a gap that spans the end of shift only counts in-shift time
- No coins emitted when work hours are not active

---

### Task 3.3 — Drop Queue ViewModel

**Goal:** A ViewModel that holds the live state of the drop queue (coins available
to drop) and exposes it to the UI.

```kotlin
class DropQueueViewModel : ViewModel() {
    val dropQueueCount: StateFlow<Int>
    val currentDenomination: StateFlow<CoinDenomination>

    fun onCoinAccrued(count: Int)     // Called by salary clock ticks
    fun onCoinDropped(): Boolean      // Returns false if queue is empty
    fun onCoinRecycled()              // Side-edge recycle — adds back to queue
}
```

**Unit Tests:**
- Queue starts at 0
- `onCoinAccrued(5)` increases count by 5
- `onCoinDropped()` decreases count by 1, returns true
- `onCoinDropped()` on empty queue returns false
- `onCoinRecycled()` increases count by 1
- Queue count never goes negative

---

### 🔍 Checkpoint 3 — Salary Clock Verified

Before proceeding to Phase 4:
- [ ] `CoinAccrualCalculator` passes all boundary condition tests
- [ ] `SalaryClock` correctly handles app-closed gaps
- [ ] `DropQueueViewModel` state management is fully unit tested
- [ ] No UI has been touched — this phase is pure business logic
- [ ] Project builds clean, all tests pass, committed and pushed

---

## Phase 4 — Onboarding UI

Build the onboarding screens now, before the game scene, so the app has real
data to work with during game development.

---

### Task 4.1 — App Theme

**Goal:** Define the visual identity of the app in Compose theme files.

- Color palette: arcade/casino inspired — dark backgrounds, gold/amber accents,
  bright whites for coin values
- Typography: clean, readable, slightly playful (suggest a rounded sans-serif)
- Define `SalaryPusherTheme` wrapping `MaterialTheme`
- All colors defined as named constants — no hardcoded hex values anywhere else

**Deliverable:** Theme applied to app, placeholder screens styled with it.

**Tests:** None — visual only.

---

### Task 4.2 — Onboarding: Welcome Screen

**Goal:** The first screen the user sees on fresh install.

**Content:**
- App name and brief concept explanation (1-2 sentences)
- "How it works" — 3 bullet points max
- "Get Started" button → navigates to Salary screen

**Compose UI Test:** "Get Started" button navigates to the Salary screen.

---

### Task 4.3 — Onboarding: Salary Input Screen

**Goal:** Collect the player's hourly rate or annual salary.

**UI:**
- Toggle between "Hourly" and "Annual" input mode
- Numeric input with currency formatting
- Annual salary auto-converts to hourly (÷ 2080 work hours/year) — displayed live
- "Next" button disabled until a valid non-zero value is entered
- Input is saved to `WorkProfileRepository` on Next

**Unit Tests:**
- Annual $104,000 → $50.00/hr
- Annual $41,600 → $20.00/hr
- $0 input keeps Next button disabled
- Negative input is rejected

**Compose UI Test:**
- Entering a salary enables the Next button
- Tapping Next navigates to Schedule screen

---

### Task 4.4 — Onboarding: Work Schedule Screen

**Goal:** Let the player define their working days and hours.

**UI:**
- Day picker: row of 7 day chips (Mon–Sun), toggleable, Mon–Fri selected by default
- Time pickers: shift start and shift end (system time picker or custom scroll wheel)
- Live preview: "Your shift: Mon–Fri, 9:00 AM – 5:00 PM"
- "Next" button — validates at least one day selected and end time is after start time

**Unit Tests:**
- End time before start time fails validation
- No days selected fails validation
- Valid schedule passes validation

**Compose UI Test:**
- Default state has Mon–Fri selected
- Tapping Next with valid input navigates to Denomination screen

---

### Task 4.5 — Onboarding: Coin Denomination Screen

**Goal:** Let the player choose their coin denomination.

**UI:**
- 5 options displayed as selectable cards: Penny, Nickel, Dime, Quarter, Dollar
- Each card shows: coin name, value, and a description of the play style
  - e.g., "Penny — More coins, longer sessions, more drops per shift"
  - e.g., "Dollar — Fewer coins, each drop counts more"
- Quarter selected by default
- "Next" button always enabled (default is pre-selected)

**Compose UI Test:**
- Tapping a denomination selects it and deselects others
- Tapping Next navigates to Notifications screen

---

### Task 4.6 — Onboarding: Notification Permission Screen

**Goal:** Request notification permission with a compelling explanation.

**UI:**
- Explanation of what notifications the app sends and why they're valuable
- List of notification types with brief descriptions
- "Enable Notifications" button → triggers `POST_NOTIFICATIONS` runtime permission request
- "Skip for Now" link — proceeds without notifications (can enable in settings later)
- Handles permission denial gracefully — no guilt-tripping, just acknowledges and moves on

**Behavior:**
- On Android 12 and below: skip this screen (permission not required)
- On Android 13+: show this screen

**Deliverable:** Permission request works on Android 13+ emulator.

**Compose UI Test:**
- Skip button navigates to game screen
- Screen does not appear on Android 12 and below

---

### Task 4.7 — Onboarding Completion

**Goal:** Wire up onboarding completion so the flow is never shown again after
the player finishes it.

- On completing the Notifications screen (either granting or skipping), call
  `WorkProfileRepository.setOnboardingComplete()`
- On app launch, check `isOnboardingComplete()` — if true, navigate directly to Game
- If false, navigate to Welcome screen

**Compose UI Test:**
- Completing onboarding sets the flag
- Relaunching the app skips onboarding and lands on Game screen

---

### 🔍 Checkpoint 4 — Onboarding Verified

Before proceeding to Phase 5:
- [ ] Full onboarding flow navigates correctly
- [ ] Salary input validates and converts correctly
- [ ] Schedule input validates correctly
- [ ] Denomination selection persists
- [ ] Notification permission screen shows only on Android 13+
- [ ] Onboarding complete flag prevents re-showing onboarding
- [ ] WorkProfile is fully populated in DataStore after completing onboarding
- [ ] All onboarding unit and Compose UI tests pass
- [ ] Project builds clean, committed and pushed

---

## Phase 5 — 3D Game Scene

This is the most complex phase. It is broken into the smallest reasonable tasks.
Read the SceneView documentation thoroughly before starting any task in this phase.

---

### Task 5.1 — Basic SceneView Scene

**Goal:** Get a 3D SceneView rendering on the Game screen with correct camera position
and lighting. Nothing interactive yet — just a lit, visible 3D viewport.

**Setup:**
- `SceneView` composable fills the game area of the screen
- Camera positioned to view the coin pusher from a front-facing slight overhead angle
  (approximating how a real coin pusher looks when you stand in front of it)
- HDR environment lighting applied
- Background: dark arcade-style color

**Deliverable:** A 3D scene renders on the game screen. The player can see it.

**Tests:** Manual visual verification on emulator. No automated tests for rendering.

---

### Task 5.2 — Static Scene Geometry

**Goal:** Build the physical boundaries of the coin pusher machine as static bodies
in the scene. No coins yet — just the machine structure.

**Geometry to create (all as `PhysicsNode` static bodies):**
- **Platform floor** — the flat surface coins rest on
- **Left wall** — side boundary
- **Right wall** — side boundary
- **Back wall** — rear boundary
- **Front edge trigger zone** — not a wall, but a detection boundary
- **Side exit zones** — left and right detection boundaries
- **Peg field** — array of cylindrical pegs in a staggered grid pattern above the platform

**Physics constants (defined as named constants, never inline):**
```kotlin
object PhysicsConstants {
    const val PLATFORM_FRICTION = 0.6f
    const val PLATFORM_RESTITUTION = 0.1f
    const val PEG_RESTITUTION = 0.4f
    const val WALL_RESTITUTION = 0.3f
}
```

**Deliverable:** The machine structure is visible and correctly proportioned in 3D.

**Tests:** Manual visual verification.

---

### Task 5.3 — Pusher Plate

**Goal:** Add the pusher plate as a kinematic physics body that moves back and forth
continuously on a defined path.

**Behavior:**
- Plate slides forward and backward along the Z axis on a continuous loop
- Speed and range defined as named constants
- Plate correctly pushes coins that are resting on the platform
- Plate is a kinematic body — it is not affected by coin weight or forces

```kotlin
object PusherConstants {
    const val PUSH_SPEED = 0.8f         // units per second
    const val PUSH_RANGE_NEAR = 0.2f    // closest point to front edge
    const val PUSH_RANGE_FAR = 0.8f     // furthest point from front edge
}
```

**Deliverable:** Pusher plate animates back and forth visibly in the scene.

**Tests:** Manual visual verification.

---

### Task 5.4 — Coin Physics Body

**Goal:** Define a single coin as a 3D physics body with correct mass, restitution,
and friction. Test it by dropping one coin programmatically into the scene.

**Coin properties:**
```kotlin
object CoinPhysicsConstants {
    const val COIN_RADIUS = 0.04f       // meters
    const val COIN_THICKNESS = 0.003f   // meters
    const val COIN_MASS = 0.005f        // kg (5 grams)
    const val COIN_RESTITUTION = 0.3f   // moderate bounce
    const val COIN_FRICTION = 0.5f
}
```

- Coin is a short cylinder (disc shape) rendered as a gold/silver circle
- Coin spawns at a given X position at the top of the peg field with zero velocity
- Coin falls under gravity, collides with pegs, lands on platform

**Deliverable:** A single coin can be spawned programmatically and falls realistically
through the peg field onto the platform.

**Tests:** Manual visual verification. Coin should not clip through pegs or platform.

---

### Task 5.5 — Tap-to-Drop Interaction

**Goal:** Wire up the tap gesture on the game screen to spawn a coin at the
tapped X position.

**Behavior:**
- Player taps anywhere on the game screen
- The X coordinate of the tap maps to an X position at the top of the peg field
- A coin spawns at that X position and falls
- Tapping only works if `DropQueueViewModel.onCoinDropped()` returns true
  (i.e., there are coins in the queue)
- If queue is empty, tap produces no coin and a subtle visual/haptic cue

**Deliverable:** Player can tap to drop coins that fall through pegs onto platform.

**Tests:** Manual visual verification.

---

### Task 5.6 — Edge Detection and Event Firing

**Goal:** Detect when coins exit the play field and fire the appropriate game events.

**Detection:**
- **Front edge:** Coin crosses the front boundary → call `EarningsRepository.recordEarning()`
  and remove coin from scene
- **Side edges:** Coin crosses a side boundary → call `DropQueueViewModel.onCoinRecycled()`
  and remove coin from scene
- Coins that come to rest fully off the platform in any direction are also caught as a
  safety net and recycled

**Deliverable:** Coins falling off front edge increase earned balance.
Coins falling off sides return to drop queue.

**Unit Tests:**
- Front edge detection fires earning event with correct denomination value
- Side edge detection fires recycle event, not earning event
- No coin is double-counted (detection fires exactly once per coin exit)

---

### Task 5.7 — Drop Queue HUD

**Goal:** Display the drop queue count and current denomination on the game screen
as a Compose overlay on top of the 3D scene.

**UI elements (overlaid on SceneView using a Box layout):**
- Coin queue count display: "🪙 47 quarters ready"
- Current salary accrual rate: "$50.00 / hr"
- Subtle "Tap to drop" hint that fades after first drop

**Deliverable:** HUD is visible over the 3D scene and updates reactively.

**Compose UI Test:** HUD displays correct count when DropQueueViewModel emits updates.

---

### 🔍 Checkpoint 5 — Game Scene Verified

Before proceeding to Phase 6:
- [ ] 3D scene renders correctly with machine geometry visible
- [ ] Pusher plate animates continuously
- [ ] Coins spawn at tapped X position
- [ ] Coins fall through pegs with believable physics
- [ ] Coins stack on platform and are pushed by the plate
- [ ] Front-edge coins trigger earning events
- [ ] Side-edge coins return to drop queue
- [ ] HUD shows live queue count
- [ ] No coins clip through geometry
- [ ] Physics feel is satisfactory — if not, tune `PhysicsConstants` values before proceeding
- [ ] Project builds clean, committed and pushed

---

## Phase 6 — Salary Clock Integration

Connect the salary engine (Phase 3) to the live game (Phase 5).

---

### Task 6.1 — Connect Salary Clock to Drop Queue

**Goal:** Wire the `SalaryClock` flow to the `DropQueueViewModel` so coins
accrue in real-time during work hours.

**Behavior:**
- During work hours, the drop queue count increases at the player's salary rate
- Outside work hours, the count does not change
- When app is opened after being closed during work hours, the offline accrual
  is calculated and added to the queue immediately
- The salary clock is started in the Game screen's ViewModel, not in a background service

**Unit Tests:**
- Opening app after a 1-hour work gap with $40/hr and quarters adds 160 coins to queue
- Opening app outside work hours adds 0 coins

---

### Task 6.2 — Earnings Display Screen

**Goal:** Build the earnings display screen showing today, this week, and all-time totals.

**UI:**
- Three stat cards: Today / This Week / All Time
- Each shows dollar amount formatted as currency (e.g., "$47.25")
- Subtitle shows coin count (e.g., "189 quarters")
- Values update in real-time via `Flow` from `EarningsRepository`
- Accessible from the game screen via a bottom nav or icon button

**Compose UI Test:**
- Screen renders correct values given a seeded earnings state
- Values update when a new earning is recorded

---

### 🔍 Checkpoint 6 — Live Game Loop Verified

Before proceeding to Phase 7:
- [ ] Coins accrue during work hours at correct rate
- [ ] Offline accrual correctly calculated on app open
- [ ] Earnings screen shows correct totals
- [ ] Complete game loop works: salary → drop queue → tap → physics → earn
- [ ] All new tests pass
- [ ] Project builds clean, committed and pushed

---

## Phase 7 — Notifications

Build the notification system. This is a primary feature area and deserves
careful, thorough implementation.

---

### Task 7.1 — Notification Channel Setup

**Goal:** Define and register all notification channels on app startup.

**Channels to create:**

| Channel ID | Name | Importance | Description |
|---|---|---|---|
| `shift_start` | Shift Start | Default | Notifies when your work shift begins |
| `coins_accruing` | Coins Accruing | Low | Progress updates on coin accumulation |
| `board_status` | Board Status | High | Alerts when board is getting full |
| `earnings_summary` | Earnings Summary | Default | End of shift and weekly summaries |
| `lunch_nudge` | Lunch Nudge | Default | Midday reminder to check in |
| `weekly_payday` | Weekly Payday | Default | Friday end-of-week summary |

- Channels created in `Application.onCreate()`
- Creating channels is idempotent — safe to call on every launch
- Channel descriptions are user-visible — write them clearly

**Unit Tests:**
- All channel IDs are unique
- Channel importance levels match their purpose

---

### Task 7.2 — Notification Permission Handling

**Goal:** A centralized utility that manages notification permission state across the app.

**Behavior:**
- On Android 13+: check and request `POST_NOTIFICATIONS` at runtime
- Expose a `Flow<Boolean>` for current permission state
- Handle the case where the user revokes permission after granting it
- The debug screen works regardless of permission state (shows what would be sent)

**Unit Tests:**
- Permission state correctly reflects granted/denied/not-yet-asked

---

### Task 7.3 — Shift Start Notification

**Goal:** Build and schedule the shift start notification.

**Notification spec:**
- **Channel:** `shift_start`
- **Title:** "Your shift has started 💰"
- **Body:** "Coins are dropping. You're earning $X/hr."
- **Actions:** "Open App" button
- **Trigger:** WorkManager task scheduled at shift start time each work day
- **Scheduling:** Rescheduled every Sunday night for the coming week

**Unit Tests:**
- Notification is scheduled for each work day defined in WorkProfile
- Notification is not scheduled for non-work days
- Reschedule replaces previous schedule without duplicating

---

### Task 7.4 — Board Status Notification

**Goal:** Build the board-full alert notification.

**Notification spec:**
- **Channel:** `board_status`
- **Title:** "Your board is filling up 🪙"
- **Body:** "You have X coins waiting. Come push some!"
- **Actions:** "Drop Coins" (deep link opens app to game screen), "Dismiss"
- **Trigger:** When drop queue exceeds a threshold (configurable, default 50 coins)
- **Throttle:** Maximum once every 2 hours to avoid spam

**Unit Tests:**
- Notification fires when threshold is exceeded
- Notification does not fire again within the throttle window

---

### Task 7.5 — Earnings Summary Notification

**Goal:** Build the end-of-shift earnings summary notification with rich image.

**Notification spec:**
- **Channel:** `earnings_summary`
- **Title:** "Shift complete 🎉"
- **Body:** "You pushed $X today across Y coins."
- **Large icon / big picture:** A rendered summary card showing today's earnings
- **Trigger:** WorkManager task scheduled at shift end time
- **Style:** `NotificationCompat.BigPictureStyle`

**Implementation note:** The "big picture" is a programmatically drawn `Bitmap`
showing the earnings summary — not a static asset. This requires a Canvas drawing
step to generate the notification image.

**Unit Tests:**
- Notification content is correct given known earnings values
- Summary bitmap is non-null and non-empty

---

### Task 7.6 — Lunch Nudge Notification

**Goal:** A configurable midday notification reminding the player to check in.

**Notification spec:**
- **Channel:** `lunch_nudge`
- **Title:** "Lunch break! 🍕"
- **Body:** "You've earned $X since this morning. Your coins are waiting."
- **Actions:** "Check My Board" (opens app)
- **Trigger:** User-configurable time (default: 12:00 PM on work days)
- **Configurable:** On/off in notification settings

**Unit Tests:**
- Scheduled at correct time on work days
- Not scheduled on non-work days
- Respects on/off preference

---

### Task 7.7 — Weekly Payday Notification

**Goal:** A Friday end-of-week celebration notification.

**Notification spec:**
- **Channel:** `weekly_payday`
- **Title:** "It's payday! 💵"
- **Body:** "You pushed $X this week. Not bad for a day job."
- **Style:** `NotificationCompat.BigTextStyle` with weekly breakdown
- **Trigger:** Friday at shift end time
- **Inline reply action:** "Name this week" — user can type a label for the week
  (stored locally, displayed in all-time earnings history)

**Unit Tests:**
- Fires on Friday at correct time
- Does not fire on other days
- Inline reply stores the user's label correctly

---

### Task 7.8 — Notification Settings UI

**Goal:** A settings screen where the user can toggle each notification type on/off.

**UI:**
- List of notification types, each with a toggle switch
- Current permission status shown at top — if denied, shows a button to open system settings
- Changes saved immediately to DataStore and respected by schedulers

**Compose UI Test:**
- Toggling a notification type off updates the preference
- Permission denied state shows correct UI

---

### Task 7.9 — Debug Notification Screen

**Goal:** A developer screen to manually fire any notification type for testing.

**UI:**
- List of all notification types as buttons: "Fire [Type] Notification"
- Each button fires the notification immediately, bypassing scheduling
- Shows current permission state at top
- Accessible from Settings screen (bottom of settings, subtle "Debug" link)

**Deliverable:** Every notification type can be manually triggered from this screen.

**Tests:** Manual — verify each notification type fires and displays correctly
on device/emulator. Document expected behavior for each in a comment above
each test button.

---

### 🔍 Checkpoint 7 — Notifications Verified

Before proceeding to Phase 8:
- [ ] All 6 notification channels registered
- [ ] Permission handling works on Android 13+ emulator
- [ ] All notification types fire correctly from debug screen
- [ ] Shift start notification schedules correctly for work days only
- [ ] Board status throttle prevents spam
- [ ] Earnings summary generates correct bitmap
- [ ] Inline reply on weekly payday stores label correctly
- [ ] Notification settings toggles are respected by schedulers
- [ ] All notification unit tests pass
- [ ] Project builds clean, committed and pushed

---

## Phase 8 — Settings Screen

---

### Task 8.1 — Settings Screen

**Goal:** A settings screen where the player can edit their work profile.

**Sections:**
- **Salary** — edit hourly/annual rate (same UI as onboarding salary screen)
- **Work Schedule** — edit days and hours (same UI as onboarding schedule screen)
- **Coin Denomination** — change denomination (same UI as onboarding denomination screen)
- **Notifications** — link to notification settings screen (Task 7.8)
- **About** — app version, brief description

**Behavior:**
- Changes take effect immediately
- Salary clock recalculates based on new rate from time of change
- Notification schedules are rebuilt when schedule is changed

**Compose UI Test:**
- Changing denomination updates the active denomination in the game
- Changing schedule rebuilds notification schedule

---

### 🔍 Checkpoint 8 — Settings Verified

Before proceeding to Phase 9:
- [ ] All settings fields editable and persistent
- [ ] Denomination change reflected in game immediately
- [ ] Schedule change triggers notification reschedule
- [ ] Navigation between settings and sub-screens works correctly
- [ ] Project builds clean, committed and pushed

---

## Phase 9 — Integration and Polish

Final integration pass before the app is considered V1-complete.

---

### Task 9.1 — Full App Integration Test

**Goal:** Walk the complete user journey end-to-end and identify any rough edges.

**Journey to verify:**
1. Fresh install → onboarding → game screen
2. Coins accrue during work hours
3. Tap to drop coins through pegs
4. Coins pushed off front → earnings increase
5. Coins fall off sides → returned to queue
6. Open earnings screen → correct totals displayed
7. Receive shift start notification
8. Receive board status notification
9. Receive earnings summary at end of shift
10. Open settings → change denomination → return to game → new denomination active
11. Kill app during work hours → reopen → offline accrual credited

**Deliverable:** A written test report noting any issues found, with each issue
either fixed or documented as a known limitation.

---

### Task 9.2 — Physics Tuning Pass

**Goal:** Final review of physics feel. Tune constants if needed.

**Checklist:**
- Coins feel like coins (not bouncy balls, not lead weights)
- Pegs deflect coins naturally and unpredictably
- Coin stacks feel stable, not jittery
- Pusher plate moves at a satisfying speed
- No visible physics glitches (tunneling, spinning, stacking explosions)

**Deliverable:** Updated `PhysicsConstants` and `CoinPhysicsConstants` if any
values were changed. Brief comment next to each constant explaining what it
controls and how increasing/decreasing it affects feel.

---

### Task 9.3 — Performance Check

**Goal:** Verify the app runs smoothly on a mid-range device profile.

**Checks:**
- Frame rate stays at or near 60fps during active coin dropping
- Memory usage does not grow unboundedly (coins removed from scene on exit)
- Background WorkManager tasks do not drain battery excessively
- No memory leaks in ViewModel or SceneView lifecycle

**Deliverable:** Any performance issues found are fixed. Document any known
limitations with mitigation notes.

---

### Task 9.4 — Final Test Pass

**Goal:** Run all unit and Compose UI tests. All must pass.

- Run full test suite
- Fix any failing tests
- Do not modify tests to make them pass — fix the underlying code
- Document any tests marked as skipped and why

---

### 🔍 Final Checkpoint — V1 Ship Criteria

The app is V1-complete when:
- [ ] Full onboarding flow works on fresh install
- [ ] Salary clock accrues coins correctly during work hours only
- [ ] Physics feel is satisfactory (per Task 9.2 checklist)
- [ ] All three earnings views show correct totals
- [ ] All 6 notification types fire correctly
- [ ] All notification types fire from debug screen
- [ ] Settings screen edits persist and take effect
- [ ] App handles being killed and reopened gracefully
- [ ] All unit tests pass
- [ ] All Compose UI tests pass
- [ ] App builds in release mode without errors
- [ ] No crashes observed during the Task 9.1 journey
- [ ] `NON-NEGOTIABLES.md` has been reviewed and all constraints are satisfied

---

## Appendix: Agent Instructions

Every agent working on this project should follow these rules:

1. **Read `NON-NEGOTIABLES.md` before writing a single line of code.**
2. **Complete one task at a time.** Do not start the next task until the current
   one is complete and the project builds cleanly.
3. **Never skip a checkpoint.** If a checkpoint fails, fix it before moving on.
4. **Write tests as part of the task, not after.** Tests are part of the definition
   of done for every task that requires them.
5. **Use named constants for every physics, timing, and tuning value.**
   No magic numbers.
6. **Never hardcode salary, schedule, or denomination values** in anything other
   than test fixtures.
7. **All new files go in the correct package** as defined in Task 1.3.
8. **If a task feels too large to complete in one session**, split it further and
   document the split before starting.
9. **If you are unsure whether something violates a non-negotiable**, it probably does.
   Stop and flag it.
10. **Commit after every completed task.** Never leave the repo in a broken state.
