# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

StudyMate is an Android Native Java app that takes user-entered study content, generates AI-powered summaries and quizzes, and stores results in Cloud Firestore. The current codebase is a **UI skeleton (MVP scaffold)** — screens and navigation work with dummy data; Firebase Auth, Firestore, and the AI backend are not yet wired up.

## Build & Run

Open in Android Studio. The project uses Gradle with Kotlin DSL (`build.gradle.kts`).

```
# Build debug APK from command line
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

**Required before building:** Create `local.properties` in the project root and add:
```
ai.base.url=<your-backend-url>
```
This value is injected as `BuildConfig.AI_BASE_URL` in `AiService`. Without it the field defaults to an empty string.

Also add `google-services.json` (from Firebase Console) to `app/` before using Firebase features.

## Architecture

```
Activity (UI + events)
    └─> Service layer (business logic, network, DB)
            ├─> AuthService       — Firebase Authentication
            ├─> FirestoreService  — Firestore read/write
            ├─> AiService         — AI backend HTTP calls + JSON parsing
            └─> QuizService       — answer checking, score calc, wrong-answer extraction
    └─> Model layer (data objects mirroring Firestore documents)
            UserModel, StudyNoteModel, QuizModel, QuizResultModel, WrongAnswerModel
    └─> Util
            AppConstants.java     — collection names, Intent extra keys, limits
            Validators.java       — email, password, content-length checks
```

**Rule:** Activities handle only screen state and user events. Firebase SDK and AI HTTP calls must go through the service layer — never call them directly from an Activity.

**BaseActivity** (`BaseActivity.java`) provides shared helpers used by all screens:
- `goTo()` / `goToAndClear()` — screen navigation with optional back-stack clearing
- `bindClick()` — shorthand for `findViewById` + `setOnClickListener`
- `isLoggedIn()` / `setLoggedIn()` — temporary `SharedPreferences` auth flag (replace with Firebase Auth check)
- `showShortToast()` — one-line Toast

## Screen → File Map

| Screen | Activity | Layout |
|---|---|---|
| Splash | `SplashActivity.java` | `activity_splash.xml` |
| Login | `LoginActivity.java` | `activity_login.xml` |
| Sign Up | `SignUpActivity.java` | `activity_signup.xml` |
| Home | `HomeActivity.java` | `activity_home.xml` |
| Study Input | `StudyInputActivity.java` | `activity_study_input.xml` |
| Summary Result | `SummaryResultActivity.java` | `activity_summary_result.xml` |
| Quiz | `QuizActivity.java` | `activity_quiz.xml` |
| Quiz Result | `QuizResultActivity.java` | `activity_quiz_result.xml` |
| Wrong Answer | `WrongAnswerActivity.java` | `activity_wrong_answer.xml` |
| My Page | `MyPageActivity.java` | `activity_my_page.xml` |

## Firestore Schema (5 collections)

`users` · `study_notes` · `quizzes` · `quiz_results` · `wrong_answers`

All user-data collections include a `userId` field. Query `study_notes` by `userId`, ordered by `createdAt` descending. `wrong_answers` stores a snapshot of question/options/explanation so it displays correctly even if the source quiz changes. Full schema: `docs/04_database/firestore_schema.md`.

## AI Integration

`AiService` calls a **separate backend server** (not the AI provider directly). Endpoints:
- `POST /summary` — body `{"text": "..."}`, returns `{"summary": [...], "keywords": [...]}`
- `POST /quiz` — body `{"text": "..."}`, returns `[{question, options[4], answerIndex (0–3), explanation}, ...]`

All HTTP calls run on a background `ExecutorService`; callbacks fire on the main thread via `Handler`. `extractJson()` strips prose wrapping from AI responses before parsing.

## What's Dummy vs. What's Real

Most screens currently display hardcoded data. Before wiring up a screen:

| Dummy | Replace with |
|---|---|
| `SharedPreferences` login flag | Firebase Auth current user |
| Fixed stats/records on Home | Firestore queries |
| `Handler` delay in StudyInput | `AiService.generateSummary()` call |
| Hardcoded quiz arrays in QuizActivity | `QuizModel` list from Firestore/AI |
| Fixed wrong-answer card | `WrongAnswerModel` list from Firestore |
| Fixed email/stats on MyPage | Firebase Auth user + Firestore stats |

Intent extra keys and Firestore field names must be defined as constants in `AppConstants.java` — not as inline strings.

## API Key Security

- `ai.base.url` belongs in `local.properties` (git-ignored), never hardcoded.
- `google-services.json` is git-ignored; each developer adds their own from Firebase Console.
- For production, proxy AI calls through Cloud Functions so the API key stays server-side.

## Team Ownership

| Owner | Area |
|---|---|
| 우지훈 | Screens, navigation, common UI, `BaseActivity`, drawables |
| 최백도 | Firebase Auth, Firestore, all model classes, `AuthService`, `FirestoreService` |
| 윤재이 | `AiService`, `StudyInputActivity`, `SummaryResultActivity` |
| 강도현 | `QuizService`, Quiz/Result/WrongAnswer screens, integration testing |

When modifying files outside your area, coordinate with the primary owner first.
