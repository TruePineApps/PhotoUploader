# Multiplatform Architecture Standard

This document defines the architectural standard for this project. The architecture is designed to
be **modular**, **testable**, and **reusable** (Template-based). It separates the "Generic Engine"
from "Specific Features" and "Project Identity."

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
│   └── preferences/                # Project-specific Settings (extends core/settings)
│
│   # Optional: one or more intermediate tiers may be introduced between feature/ and core/
│   # when features depend on other features. See Section 3 for the full explanation.
│
└── core/                           # THE ENGINE (Reusable Base - Copy-Pasteable)
    ├── domain/                     # Cross-cutting Contracts (no implementation, no UI)
    │   ├── repository/             # Base repository interfaces (e.g. DataLoadingRepository)
    │   └── state/                  # Shared state models (e.g. DataLoadingState)
    ├── presentation/               # Design System infrastructure
    │   ├── design/                 # Tokens: Dimensions, Opacity, Theme Wrapper
    │   ├── component/              # Base Widgets: Loading, Error, Dialogs, Buttons
    │   ├── navigation/             # Navigation contract: NavigationDestination interface
    │   └── base/                   # Base ViewModels (e.g. LoadingViewModel); depends on core/domain/
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

Each feature in any feature tier or `core/feature/` package is divided into four strictly
decoupled layers, plus two infrastructure packages that keep the feature self-contained:

```text
<feature>/
├── ui/                             # Presentation - View (Composables)
├── viewmodel/                      # Presentation - Logic (ViewModel, UiState)
├── domain/                         # The Contract - Business Logic
│   ├── model/                      # Business Data Models
│   ├── repository/                 # Repository Interfaces
│   └── usecase/                    # Business Actions
├── data/                           # The Implementation - Infrastructure
│   ├── source/                     # Data Sources (Ktor/Database)
│   ├── repository/                 # Repository Implementations
│   └── dto/                        # Data Transfer Objects (JSON Models)
├── di/                             # Dependency Injection - Koin module(s) for this feature
└── navigation/                     # Navigation - Destinations and NavGraphBuilder extensions
```

### Layer Responsibilities

* **ui:** Contains **only** Compose functions. Uses only Semantic Tokens and Core Dimensions.
* **viewmodel:** Orchestrates UI-specific logic (e.g. toggling loading state). Depends on `domain`
  to execute actions.
* **domain:** The "Pure" part of the feature. Contains business rules and defining interfaces.
  * **model:** Business data models used by the UI and ViewModel. Technical DTOs from the data
    layer are mapped into these classes.
  * **repository:** Interfaces (Contracts) that define what data is needed, not how to get it.
  * **usecase:** Logic that orchestrates complex business actions (optional).
* **data:** The "Dirty" work. Handles technical implementation and mapping.
  * **source:** Concrete implementations of external communication (e.g., Ktor, SQLite).
  * **repository:** Implementations of `domain/repository`. These coordinate one or more sources
    and map DTOs to Domain models.
  * **dto:** Data Transfer Objects. Models that exactly match external data formats (e.g., JSON).
* **di:** Contains one or more Koin `module { }` declarations that bind the feature's `domain`
  interfaces to their `data` implementations. This is the only place inside a feature that is
  allowed to import both `domain` and `data` simultaneously. `app/di/` includes this module but
  does not know the feature's internal bindings.
* **navigation:** Contains `NavigationDestination` objects and `NavGraphBuilder` extension
  functions (e.g. `fun NavGraphBuilder.uploaderGraph(...)`). This is the feature's public
  navigation API. `app/navigation/` imports only from this package, never from `ui/` directly.
  Features with a single screen may place their destination object here and keep the
  `NavGraphBuilder` extension trivial; multi-screen features define their full sub-graph here.

### Internal Feature Dependencies

Within a single feature, the dependency flow is strictly top-down:

```text
ui  →  viewmodel  →  domain  ←  data
↑                               ↑
navigation (depends on ui)      di (depends on data + domain)
```

* **`ui`** may import `viewmodel` and `domain/model` (for display). It must never import `data`.
* **`viewmodel`** may import `domain` (model, repository interfaces, use cases). It must never
  import `data` or `ui`.
* **`domain`** is the stable centre. It imports nothing from within the feature. It may import
  `core` utilities (e.g. `UiText`, `AppInfo`).
* **`data`** implements `domain/repository` interfaces and imports `domain/model` for mapping.
  It must never import `viewmodel` or `ui`.
* **`di`** is the only package that may import across the `domain`/`data` boundary. It must never
  import `ui` or `viewmodel`.
* **`navigation`** imports `ui` (to reference screen composables) and `domain/model` (for typed
  route arguments). It must never import `data` or `di`.

The arrow on `domain` is intentionally bidirectional: both `viewmodel` and `data` depend on
`domain`, but `domain` depends on neither. This is the **Dependency Inversion Principle** in
practice — the interface (`domain/repository`) is owned by the layer that needs it (`viewmodel`),
not the layer that implements it (`data`).

---

## 3. The Dependency Flow

To prevent circular dependencies and maintain reusability, imports must follow a strict one-way
flow:

1. **`app`** → depends on → **`feature`** AND **`core`**
2. **`feature`** → depends on → **`core`**
3. **`core`** → depends on → **nothing** (except external libraries)

### Inter-Feature Dependencies and Intermediate Tiers

A feature must never directly import another feature at the same tier. If `Feature A` depends on
logic from `Feature B`, that dependency must be made explicit by placing `Feature B` in a
**lower tier** that `Feature A` may legally import.

This situation is project-specific and must not be resolved by moving the shared feature into
`core/`, which is reserved for the generic, domain-agnostic engine. Instead, introduce one or
more **intermediate tiers** between `feature/` and `core/`. The number of tiers and their names
are decided per project, based on the dependency graph of the features involved.

**Example — a scheduling system:**

```text
app/
├── feature/            # Top-tier features — depend on foundation/
│   └── report/         # Uses 'job' concepts to build reports
├── foundation/         # Mid-tier features — shared between top-tier features
│   └── job/            # Defines Job domain models and repository interfaces
└── core/               # Generic engine — no domain knowledge
```

The full dependency chain becomes:

```text
app  →  feature  →  foundation  →  core
```

**Naming guidance:** Choose a tier name that reflects its role in your domain, not a structural
label. `foundation/` works as a neutral default. Other projects might use `platform/`, `domain/`,
or a domain-specific name. The key invariants are:

* Each tier may only import from tiers **below** it in the stack.
* `core/` remains domain-agnostic and copy-pasteable to new projects.
* Intermediate tiers are **project-specific** and travel with the project, not the template.

### Restrictions

* **No Circularity:** A feature at tier N cannot import a feature at the same tier N. Shared logic
  must move to a tier below.
* **Branding Independence:** Features must never import `app/theme`. They must use
  `core/presentation/design` tokens.
* **Dependency Inversion:** The `data` layer implementation must never be leaked. ViewModels must
  only interact with `domain/repository` interfaces.

---

## 4. Reusability Workflow

To start a new project using this template:

1. **Copy** the `core/` package.
2. **Define** new branding in `app/theme`.
3. **Implement** the specific capabilities in the `feature/` package.
4. **Introduce intermediate tiers** (e.g. `foundation/`) if features depend on other features.
5. **Connect** the flow in `app/navigation`.