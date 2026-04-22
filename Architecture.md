# Multiplatform Architecture Standard

This document defines the architectural standard for this project. The architecture is designed to be **modular**, **testable**, and **reusable** (Template-based). It separates the "Generic Engine" from "Specific Features" and "Project Identity."

## 1. Project Structure

```text
com.package.name/
├── app/                            # THE IDENTITY (Project-Specific "Glue")
│   ├── App.kt                      # Main App Entry & Scaffold
│   ├── navigation/                 # Orchestration: NavHost, Destinations, Flow
│   ├── theme/                      # Branding: Colors, Typography, Brand-to-Token mapping
│   └── di/                         # Composition Root: Koin wiring (e.g. AppInfo implementation)
│
├── feature/                        # THE CAPABILITIES (Swappable "Plugs")
│   ├── <feature_name>/             # (See "Feature Internal Layering" below)
│   └── preferences/                   # Project-specific Settings (extends core/settings)
│
└── core/                           # THE ENGINE (Reusable Base - Copy-Pasteable)
    ├── presentation/               # Design System infrastructure
    │   ├── design/                 # Tokens: Dimensions, Opacity, Theme Wrapper
    │   ├── components/             # Base Widgets: Loading, Error, Dialogs, Buttons
    │   └── viewmodel/              # Base ViewModels (e.g. LoadingViewModel)
    ├── feature/                    # THE CORE CAPABILITIES (Permanent features)
    │   ├── legal/                  # Generic Legal Logic (Consent, Gate, Repo)
    │   ├── settings/               # Base Settings logic (Language, etc.)
    │   └── about/                  # Generic About/License logic
    ├── network/                    # Networking: Ktor Client Factory
    ├── io/                         # System: FileSystem & KmpFile helpers
    ├── localization/               # Formatting logic (Date, Number)
    ├── log/                        # Monitoring: Logging configuration
    └── util/                       # Utilities: Interfaces (AppInfo), PlatformInfo, Utils
```

---

## 2. Feature Internal Layering

Each feature in the `feature/` or `core/feature/` package is divided into four strictly decoupled layers:

```text
<feature>/
├── ui/                             # Presentation - View (Composables)
├── viewmodel/                      # Presentation - Logic (ViewModel, UiState)
├── domain/                         # The Contract - Business Logic
│   ├── model/                      # Business Data Models
│   ├── repository/                 # Repository Interfaces
│   └── usecase/                    # Business Actions
└── data/                           # The Implementation - Infrastructure
    ├── api/                        # Data Sources (Ktor/Database)
    ├── repository/                 # Repository Implementations
    └── dto/                        # Data Transfer Objects (JSON Models)
```

### Layer Responsibilities
*   **ui:** Contains **only** Compose functions. Uses only Semantic Tokens and Core Dimensions.
*   **viewmodel:** Orchestrates UI-specific logic (e.g. toggling loading state). Depends on `domain` to execute actions.
*   **domain:** The "Pure" part of the feature. Contains business rules and defining interfaces.
*   **data:** The "Dirty" work. Handles specifics of where data comes from and maps DTOs to Domain models.

---

## 3. The Dependency Flow

To prevent circular dependencies and maintain reusability, imports must follow a strict one-way flow:

1.  **`app`** → depends on → **`feature`** AND **`core`**
2.  **`feature`** → depends on → **`core`**
3.  **`core`** → depends on → **nothing** (except external libraries)

### Restrictions
*   **No Circularity:** `Feature A` cannot import `Feature B`. Shared logic must be moved to `core`.
*   **Branding Independence:** Features must never import `app/theme`. They must use `core/presentation/design` tokens.
*   **Dependency Inversion:** The `data` layer implementation must never be leaked. ViewModels must only interact with `domain/repository` interfaces.

---

## 4. Reusability Workflow

To start a new project using this template:
1.  **Copy** the `core/` package.
2.  **Define** new branding in `app/theme`.
3.  **Implement** the specific capabilities in the `feature/` package.
4.  **Connect** the flow in `app/navigation`.
