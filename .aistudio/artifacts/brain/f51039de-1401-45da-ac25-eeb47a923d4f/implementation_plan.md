# Shayan Dhara Budget & Expense Tracker

A modern personal finance and daily budget tracking Android application featuring daily income and expense logging, side-by-side comparative bar charts for weekly and monthly periods, category tagging, and proactive budget threshold alerts.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following product specifications incorporate the confirmed preferences from Phase 1:
> - **Default Currency**: Indian Rupee (`₹`) formatted consistently across metrics, charts, and input fields.
> - **Visual Chart Style**: Side-by-side dual bar charts comparing Income vs. Expenses across both Weekly (7-day daily breakdown) and Monthly (weekly bucket and 30-day views) periods, accompanied by full total summaries.
> - **Data Persistence**: Offline-first local SQLite storage via Android Room with reactive Kotlin Coroutine flows, ensuring instant responsiveness without requiring account logins.

- **Confirmed Decision 1**: Welcoming splash presentation screen celebrating **SHAYAN DHARA** with an animated entry action to navigate into the primary dashboard.
- **Confirmed Decision 2**: Category tagging covering both expenses (Food, Rent, Bills, Transit, Shopping, Health, Entertainment, Others) and income (Salary, Freelance, Investments, Gifts, Others) with custom tag support.
- **Confirmed Decision 3**: Configurable category budget limits with visual progress meters and multi-tier warning alerts (e.g., 80% caution, 100% exceeded).

---

## 1. Overview & Core Concept

- **What It Does**: Enables users to seamlessly log their daily income and expenses, organize cash flow with category tags, monitor spending limits with custom threshold alerts, and visualize financial health through side-by-side comparative weekly and monthly charts.
- **Target Audience / Persona**: Individuals seeking a fast, elegant, privacy-respecting financial tracker to maintain financial discipline and understand their savings rate.
- **Key Value**: Eliminates manual spreadsheet friction with intuitive transaction capture, clear visual comparisons between inflow and outflow, and proactive budget alerts before overspending occurs.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Presentation / Welcome Screen (First Impression)**:
   - Displays a styled signature presentation honoring **SHAYAN DHARA** with an uplifting financial health tagline ("Master Your Cash Flow & Daily Wealth").
   - Features quick-stats highlights and a prominent, tactile `"Proceed to Dashboard"` button with smooth transition animation.
   - Includes an option to revisit via the top app bar or settings anytime.

2. **Main Dashboard**:
   - **Hero Financial Card**: Displays Current Net Balance, Total Monthly Income (emerald glow), and Total Monthly Expense (rose accent).
   - **Budget Alert Banners**: Highlighted alert cards appear if any category spending reaches ≥80% or exceeds 100% of its budget limit.
   - **Weekly Cash Flow Snapshot**: Mini dual-bar visual comparison comparing the current week's daily income and expenditure.
   - **Recent Transactions List**: Chronological feed with category badges, amounts, and quick actions to edit or delete.
   - **Quick Action FAB**: Instant modal bottom sheet to record daily expense or income in seconds.

3. **Analytics & Comparison Hub**:
   - **Timeframe Selector**: Segmented tabs for **Weekly** (Day-by-Day 7-day breakdown) and **Monthly** (Week-by-Week & 30-day aggregation).
   - **Side-by-Side Dual Bar Charts**: High-contrast, custom Jetpack Compose Canvas rendering paired emerald (Income) and coral/rose (Expense) bars for each time slot with interactive touch tooltips.
   - **Full Total Summary Cards**: Comprehensive totals comparing `Total Income`, `Total Expenses`, `Net Savings`, and `Savings Rate %` for the selected period.
   - **Category Distribution**: Ranked category breakdown showing percentage of total spending per tag.

4. **Budget Alerts & Limits Manager**:
   - Set monthly spending targets per category (e.g., Food: ₹8,000, Transit: ₹3,000).
   - Real-time progress bars: Green (<70%), Amber (70–99%), Red (≥100%).
   - Custom alert threshold selector (75%, 80%, 90%, 100%) with alert notifications banner.

5. **Transaction Entry & Tagging**:
   - Segmented toggle: **Expense** vs. **Income**.
   - Large numeric keypad / amount input with `₹` symbol.
   - Category selector chips with distinct icon & color combinations, plus custom tag entry.
   - Date picker (defaults to today) and optional notes.

### Visual Identity & Theme

- **Aesthetic Direction**: *Fintech Elegance* — deep slate background with high-contrast emerald green for positive cash flow, warm coral-rose for expenses, and brass gold accents for wealth metrics.
- **Color Palette**:
  - `Primary / Inflow`: Emerald `#00C853` / `#059669`
  - `Secondary / Outflow`: Coral Rose `#F43F5E` / `#E11D48`
  - `Accent / Brand`: Golden Amber `#F59E0B`
  - `Background & Surface`: Deep Slate `#0F172A` / `#1E293B` (Dark) with clean `#F8FAFC` / `#FFFFFF` (Light)
  - `Text Hierarchy`: Crisp White `#F8FAFC` (Dark mode headline) and Muted Slate `#94A3B8` (Subtext)
- **Typography & Rhythm**:
  - Prominent tabular numerals for financial figures ensuring alignment and readability.
  - Generous 16dp / 20dp padding, subtle 1dp borders, and rounded card geometry (16dp corner radius).

---

## 3. Key Product Decisions & Trade-Offs

- **Local Room Database with Kotlin Flow vs. Cloud Auth**:
  - *Chosen Approach*: Android Room (SQLite) with reactive StateFlows.
  - *Why*: Budget logging requires sub-second startup, zero sign-in friction, offline functionality, and complete privacy for personal expense data. Room is already pre-configured in the project.
- **Custom Canvas Bar Charts vs. Heavy External Chart Libraries**:
  - *Chosen Approach*: Clean, fluid Jetpack Compose `Canvas` dual-bar chart implementation.
  - *Why*: Delivers crisp 60fps animations, exact color token adherence, touch tooltip inspection, zero external dependency conflicts, and complete control over side-by-side bars and axis markings.
- **Two-Screen Architecture (Presentation + Main Hub)**:
  - *Chosen Approach*: A presentation hero state transitioning smoothly into an app scaffold with BottomNavigation tabs (`Dashboard`, `Charts & Analytics`, `Budgets & Alerts`).
  - *Why*: Satisfies the prompt requirement for the dedicated "SHAYAN DHARA" presentation screen while ensuring effortless daily utility.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                              UI LAYER                                  │
│                                                                        │
│   ┌───────────────────────────┐    ┌───────────────────────────────┐   │
│   │   PresentationScreen      │───▶│         MainScaffold          │   │
│   │   ("SHAYAN DHARA" Intro)  │    │ (Bottom Nav & Floating Action)│   │
│   └───────────────────────────┘    └───────────────┬───────────────┘   │
│                                                    │                   │
│         ┌──────────────────────┬───────────────────┴───────────────┐   │
│         ▼                      ▼                                   ▼   │
│   ┌───────────────┐     ┌────────────────┐               ┌───────────┐ │
│   │ DashboardView │     │ AnalyticsView  │               │ BudgetView│ │
│   │ (Cards/Alerts)│     │(Dual Bar Chart)│               │ (Targets) │ │
│   └───────────────┘     └────────────────┘               └───────────┘ │
│         │                      │                                   │   │
│         └──────────────────────┼───────────────────────────────────┘   │
│                                ▼                                       │
│                     ┌────────────────────┐                             │
│                     │  BudgetViewModel   │                             │
│                     └─────────┬──────────┘                             │
└───────────────────────────────┼────────────────────────────────────────┘
                                │ StateFlow / Coroutines
┌───────────────────────────────┼────────────────────────────────────────┐
│                               ▼                                        │
│                      DATA & REPOSITORY LAYER                           │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                       BudgetRepository                         │   │
│   └───────────────────────────────┬────────────────────────────────┘   │
│                                   │                                    │
│         ┌─────────────────────────┴────────────────────────┐           │
│         ▼                                                  ▼           │
│   ┌───────────────┐                                  ┌───────────────┐ │
│   │TransactionDao │                                  │   BudgetDao   │ │
│   └───────┬───────┘                                  └───────┬───────┘ │
│           │                                                  │         │
│           └───────────────────────┬──────────────────────────┘         │
│                                   ▼                                    │
│                     ┌──────────────────────────┐                       │
│                     │   AppDatabase (Room)     │                       │
│                     │  SQLite Local Storage    │                       │
│                     └──────────────────────────┘                       │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Models

```kotlin
// Transaction Entity
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String,
    val tag: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// Budget Limit Entity
@Entity(tableName = "budget_limits")
data class BudgetLimitEntity(
    @PrimaryKey val category: String,
    val monthlyLimit: Double,
    val alertThresholdPercent: Int = 80 // Alert at 80%
)
```

### Interactive State & Component Mapping

1. **Presentation Transition**:
   - `showPresentation: Boolean` in ViewModel/Preferences. Tap `"Proceed to Main Dashboard"` smoothly toggles state and displays the main dashboard.
2. **Transaction Creation & Editing**:
   - Modal bottom sheet with type selector, quick category pills (Food, Shopping, Salary, etc.), currency input, and date picker. Inserting updates `Flow<List<TransactionEntity>>` and triggers budget re-calculation.
3. **Analytics Period Switching**:
   - `PeriodType.WEEKLY` computes 7-day daily sums of income vs expense.
   - `PeriodType.MONTHLY` computes 4-week bucket sums and full 30-day totals.
   - Side-by-side bar chart renders with animated height transitions and value labels.
4. **Budget Alerts Computation**:
   - Computes current month expense sum per category vs `monthlyLimit`.
   - Generates alert cards for any category exceeding threshold with progress indicators.
