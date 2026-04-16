# PortfolioAdvisorySystem

Rule-Based Stock Allocation System
Precise Requirements
1. Objective
Build a rule-driven stock allocation platform that allocates funds into stocks for subscribers based on:
predefined risk profiles
configurable allocation rules
dynamic eligibility and liquidity conditions
controlled deallocation and reallocation when rules change or a stock becomes ineligible
The platform should behave like a managed allocation engine, where subscribers are mapped to portfolio strategies and stocks are assigned according to policy rules.
2. Scope
In Scope
Subscriber onboarding and profiling
Risk-based model allocation
Rule engine for stock eligibility
Initial allocation
Deallocation of existing positions
Reallocation/migration to replacement stocks
Threshold-based and event-based rebalancing
Out of Scope
Direct market execution
Broker settlement
Tax optimization logic
Personalized investment advisory recommendations
3. Core Functional Requirements
3.1 Subscriber Management
The system must maintain subscriber profiles with:
Subscriber ID
Investment amount / AUM mapped to subscriber
Selected plan or strategy
Risk profile
Status (active, paused, exited)
Risk profiles must support at least:
Conservative
Moderate
Aggressive
The system must allow bulk import and update of subscriber data.
3.2 Risk Profile Mapping
Each subscriber must be mapped to a model allocation strategy based on risk profile.
Each risk profile must define:
allowed stock universe
maximum stock concentration
sector exposure limits
cash or unallocated buffer rules (if applicable)
The system must prevent allocation outside the allowed risk policy.
3.3 Stock Universe Management
The system must maintain a master list of eligible stocks with attributes such as:
Stock ID / symbol
Sector / category
Liquidity / trading volume
Market capitalization bucket
Active / inactive status
Risk classification
The stock universe must support dynamic eligibility changes.
3.4 Rule Engine
The system must support configurable and dynamic rules that determine whether a stock can be allocated, retained, deallocated, or used as a replacement.
Example rule types
Minimum average trading volume
Minimum number of active subscribers in the scheme/strategy
Maximum allocation exposure per stock
Maximum allocation exposure per sector
Minimum market liquidity score
Risk-profile compatibility
Blacklist / exclusion rules
Hold / freeze rules for specific stocks
Concentration thresholds
Minimum holding size
Minimum investable amount per stock
Rule engine requirements
Rules must be configurable without code deployment.
Rules must support:
threshold-based logic
boolean conditions
weighted rule priority
effective date and expiry date
Rules must be versioned.
Rule evaluation results must be auditable.
3.5 Allocation Engine
The system must allocate subscriber funds to eligible stocks according to:
risk profile
strategy model
current active rules
Allocation must support:
initial allocation for new subscribers
incremental allocation for new inflows
rebalance allocation for existing subscribers
The engine must ensure:
no allocation to ineligible stocks
adherence to per-stock and per-sector caps
proportional allocation where applicable
minimum lot/investment amount checks if configured
3.6 Deallocation Engine
The system must support deallocation of stocks when a stock no longer meets allocation rules.
Deallocation triggers
Stock becomes ineligible under current rules
Volume falls below minimum threshold
Subscriber count rule is violated
Risk classification changes
Strategy model changes
Corporate or internal freeze/exclusion is applied
Concentration exceeds threshold
Scheduled periodic rebalance requires reduction
Deallocation requirements
The system must identify impacted subscribers and holdings.
Deallocation may be:
full exit
partial reduction
The deallocation reason must be recorded.
Deallocation must be policy-driven and traceable.
The system must support staged deallocation if immediate full removal is not allowed by policy.
3.7 Reallocation / Migration Engine
The system must support migration from one stock to another when deallocation occurs.
Reallocation requirements
When a stock is deallocated, the system must identify one or more replacement stocks based on:
same risk profile compatibility
same or acceptable sector/category
rule compliance
available allocation headroom
liquidity eligibility
Reallocation must support:
one-to-one migration
one-to-many migration
weighted redistribution across multiple replacement stocks
Migration must preserve strategy constraints after reallocation.
If no valid replacement exists, the system must:
keep amount unallocated, or
move to a default reserve bucket, depending on policy
The system must record:
source stock
target stock(s)
migration reason
migration timestamp
impacted subscriber set
3.8 Rebalancing
The platform must support:
scheduled rebalancing
event-driven rebalancing
Rebalancing triggers may include:
rule changes
stock eligibility change
subscriber inflow/outflow
drift from target allocation
Rebalancing must evaluate:
whether to retain
whether to deallocate
whether to reallocate
The system must support simulation before rebalance execution.
3.9 Eligibility & Threshold Conditions
The system must support business rules such as:
minimum number of subscribers in a strategy before enabling a stock
minimum stock trading volume
maximum allocation percentage in any single stock
maximum exposure per sector/theme
minimum stock score or ranking
minimum liquidity threshold
exclusion of suspended or restricted instruments
minimum subscriber holding size before migration is triggered
These conditions must be editable by authorized users.
3.10 Approval Workflow
High-impact rule changes must require approval before activation.
The system must support maker-checker workflow for:
new rule creation
rule update
stock deactivation
bulk deallocation/reallocation actions
Emergency override must be supported for authorized users with audit logging.
4. Non-Functional Requirements
4.1 Performance
The system must process allocation and rebalance runs within defined batch windows.
Rule evaluation must scale for large subscriber sets.
Allocation calculations must support bulk processing.
4.2 Scalability
The platform must support growth in:
number of subscribers
number of stocks
number of active rules
frequency of rebalance events
4.3 Reliability
Allocation runs must be resumable in case of failure.
Partial processing states must be recoverable.
Duplicate allocation or migration events must be prevented.
4.4 Security
Role-based access control must be enforced for:
rule administration
strategy configuration
approvals
reporting
All changes must be logged.
4.5 Maintainability
Rule configuration should not require code changes.
Allocation logic, rule management, and reporting should be modular.
5. Data Requirements
Subscriber Data
Subscriber ID
Risk profile
Plan/strategy
Investment amount
Current holdings
Status
Stock Data
Stock symbol
Sector
Liquidity metrics
Volume metrics
Risk category
Eligibility status
Rule Data
Rule ID
Rule type
Threshold/value
Effective date
Expiry date
Version
Status
Allocation Decision Data
Allocation run ID
Stock selected
Amount/weight allocated
Rule basis
Subscriber impacted
Migration Data
Deallocated stock
Replacement stock(s)
Amount shifted
Migration reason
Rule trigger
Approval reference
6. Key Business Rules
The system must support rules such as:
Risk suitability rule
A subscriber can only be mapped to stocks allowed under the subscriber’s risk profile.
Minimum subscriber rule
A stock or allocation model may only remain active if the minimum number of subscribers configured for that strategy is met.
Minimum volume rule
A stock must meet minimum trading volume criteria to remain eligible.
Maximum concentration rule
No stock may exceed a configured maximum percentage of the strategy allocation.
Sector cap rule
Total allocation to a sector must not exceed a defined limit.
Migration rule
If a stock becomes ineligible, holdings must be deallocated and reallocated to the best eligible replacement(s) as per current policy.
Fallback rule
If no replacement stock is available, the amount must remain unallocated or move to a predefined reserve bucket.
7. Deallocation and Reallocation Flow
7.1 Deallocation Flow
Detect rule breach or eligibility change
Identify impacted stock and subscriber holdings
Determine whether full or partial deallocation is required
Record deallocation reason
Move holdings to transition state
7.2 Reallocation / Migration Flow
Find eligible replacement stocks
Rank replacements based on rule match and allocation capacity
Reassign deallocated amount
Validate new allocation against all strategy rules
Record migration details and final allocation state
8. Reporting Requirements
The system must provide reports for:
Current allocation by subscriber / strategy / stock
Stocks marked for deallocation
Migration history
Rule breach summary
Rebalance impact summary
Subscriber-level before/after allocation comparison
Unallocated pool due to no replacement availability
