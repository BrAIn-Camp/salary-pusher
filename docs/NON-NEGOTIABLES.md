# Salary Pusher — Non-Negotiables

These are the core constraints and principles that define the identity of this application.
No agent, developer, or refactor should ever compromise these. If a decision conflicts with
anything on this list, stop and flag it before proceeding.

---

## 1. The Salary Simulation Is Sacred

The rate at which coins accrue **must always reflect the player's real-world salary.**
No mechanic, feature, or upgrade may increase the coin accrual rate beyond what the
player actually earns per hour at their defined salary.

- ✅ Coins accrue at exactly the player's hourly rate during work hours
- ✅ Coin denomination choices affect the *feel* of play (more coins vs. fewer, larger coins)
- ❌ No power-ups, bonuses, or upgrades that multiply or boost earn rate
- ❌ No "offline catch-up" that awards coins for time outside defined work hours

The moment the game awards coins the player didn't earn, the core concept is broken.

---

## 2. Work Hours Only

Coins accrue **only during the player's defined work schedule.**

- ✅ Coins drop during defined work days and hours
- ✅ The machine sits idle outside work hours — this is intentional and thematic
- ❌ No coin accrual on weekends, holidays, or outside shift times
- ❌ No passive trickle when off the clock

This game is explicitly a **work hours game.** It lives on your phone during the workday.
It goes quiet when you clock out. That is a feature, not a bug.

---

## 3. No Coins Are Ever Destroyed

A coin that leaves the play field must go somewhere. The two valid destinations are:

| Destination | Condition |
|---|---|
| **Earned Balance** | Coin exits the front edge into the collection chute |
| **Drop Queue** | Coin exits either side edge — recycled, not lost |

- ✅ Front edge exits are earnings — permanent, tracked, displayed
- ✅ Side edge exits are recycled back to the drop queue
- ❌ No mechanic that permanently removes a coin from the game without it being earned

This keeps the experience purely rewarding. A poor drop isn't a punishment — it's just a
delayed opportunity.

---

## 4. Earned Balance Is Read-Only

The Earned Balance represents the player's actual real-world salary earned during play.
It is a **tracker, not a currency.**

- ✅ Displayed as today / this week / all time
- ✅ Grows only when coins exit the front edge
- ❌ Never spent on upgrades, unlocks, or any in-game mechanic
- ❌ Never reset except by explicit user action (if ever implemented)

Collected coins are the player's real money made visible. They don't buy anything.
They just accumulate — a satisfying, honest ledger.

---

## 5. Data Never Leaves the Device

The player's salary and earnings data is sensitive. It is stored locally and only locally.

- ✅ Room database on-device
- ✅ No user accounts, no login
- ❌ No cloud sync, no analytics that include salary figures, no server communication
  of any financial data
- ❌ No third-party SDKs that could harvest salary or earnings information

If a future feature requires network access (e.g., leaderboards), salary and earnings
data must be explicitly excluded and anonymized before any transmission.

---

## 6. No Ads, No Monetization

This application is free, for fun, and contains no advertising or monetization of any kind.

- ❌ No AdMob or any ad SDK
- ❌ No in-app purchases
- ❌ No premium tiers or paywalled content
- ❌ No "remove ads" because there are never ads to remove

Do not add any monetization dependency to the project under any circumstances.

---

## 7. Kotlin and Jetpack Compose Are the Only UI Layer

This project is written entirely in Kotlin. The UI is built entirely in Jetpack Compose.
No XML layouts. No View-based UI. No Flutter, no React Native, no Unity, no LibGDX.

- ✅ Kotlin throughout
- ✅ Jetpack Compose for all UI including overlays on top of the 3D scene
- ✅ SceneView (Filament) for 3D rendering and physics
- ❌ No XML layout files
- ❌ No mixing of View-based and Compose UI systems
- ❌ No additional rendering frameworks alongside SceneView

---

## 8. The Notification System Is First-Class

Notifications are not an afterthought. They are a primary feature and a technical
exploration goal of this project. They must be treated with the same care as the
game itself.

- ✅ Each notification type gets its own Android notification channel
- ✅ Notification permissions are requested thoughtfully during onboarding with clear
  explanation of value
- ✅ Users have per-channel control via Android system settings
- ✅ A debug screen exists to manually fire every notification type for testing
- ❌ No notification type may be added without a corresponding channel and a debug
  trigger on the debug screen
- ❌ Notifications must never fire outside the player's defined work hours unless
  they are explicitly "end of shift" or "weekly summary" type events

---

## 9. Physics Must Feel Good

The coin pusher experience lives or dies on physics feel. This is non-negotiable.

- ✅ Coins must have realistic mass, friction, and restitution (bounciness)
- ✅ Peg collisions must feel satisfying — coins should deflect believably
- ✅ Coin stacking on the platform must feel physical — no coins clipping through each other
- ✅ The pusher plate must move continuously and interact with coins naturally
- ❌ No placeholder or "good enough for now" physics shipped in a named release
- ❌ Physics tuning values (mass, friction, restitution) must be defined as named
  constants, never magic numbers inline

---

## 10. The Prototype Scope Is Fixed

Version 1 ships exactly this and nothing more. Scope creep is the enemy of shipping.

**V1 must include:**
- Working 3D coin pusher scene
- Tap-to-drop with x-position entry point
- Peg field with realistic physics
- Continuously moving pusher plate
- Front-edge earn detection
- Side-edge recycle detection
- Salary clock running during work hours
- At least 3 fully working notification types
- Debug notification firing screen
- Earnings display (today / this week / all time)
- Onboarding flow (salary, schedule, denomination, notification permission)
- Settings screen (edit schedule, denomination)

**Explicitly out of scope for V1:**
- Multiple machine sizes / progression unlocks
- Special or decorative coins
- Machine visual themes or cosmetics
- Any network features
- Any social features

---

## 11. Every Task Must Leave the Codebase Buildable

At no point should a committed state of the codebase fail to build.
Each agent task ends with a clean build. If a task is partially complete,
stub implementations are acceptable — broken imports are not.

---

## 12. Test Coverage Is Required for Business Logic

The following must always have unit tests:
- Salary clock accrual calculations
- Coin denomination conversion logic
- Work schedule evaluation (is it currently a work hour?)
- Earned balance accumulation
- Edge detection event logic (front vs. side)
- Notification scheduling logic

Compose UI tests are required for:
- Onboarding flow completion
- Earnings display rendering
- Settings screen editing

No business logic ships without a corresponding test.
