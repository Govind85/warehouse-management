# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Below are my considerations:**

- Main challenge is shared resources - how to fairly split costs when one truck serves multiple stores or warehouses share staff? Need clear allocation rules (by distance, weight, or transaction volume)
- Indirect costs like utilities and management salaries are tricky - should we allocate by square footage, revenue percentage, or transaction count? Start simple with revenue-based allocation, then refine
- Automation is critical - manual cost entry creates errors and delays. Tag every transaction with cost center codes and implement automated reconciliation reports to catch discrepancies early

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Below are my considerations:**

- Start with data analysis to find where 70% of costs are (usually transportation, labor, and warehouse space). Quick wins: route optimization (15-20% savings) and shipment consolidation (15-25% savings) - both low effort, high impact
- Use impact vs effort matrix for prioritization - tackle quick wins first to build momentum, then major projects. Always pilot on one warehouse before full rollout
- Key risk: don't cut costs that hurt quality (packaging, delivery speed). Monitor customer satisfaction alongside cost metrics and have rollback plans ready

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Below are my considerations:**

- Main benefit is single source of truth - eliminates manual reconciliation, reduces errors, and speeds up month-end close from 10 days to 2-3 days. Finance and Operations see the same numbers in real-time
- Use event-driven architecture: warehouse transaction happens → publish event → financial system subscribes. Keeps systems decoupled and doesn't block operations. Must ensure idempotency (same transaction sent twice = same result)
- Need daily automated reconciliation reports to flag discrepancies. If financial system goes down, queue transactions locally (circuit breaker pattern) and sync when it's back up

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Below are my considerations:**

- Need at least 2 years of historical data to identify seasonal patterns (Q4 spike) and trends. Combine with business drivers (sales forecast, new store openings, marketing campaigns) and external factors (fuel prices, wage changes)
- Use hybrid approach: bottom-up for controllable costs (labor, supplies) and top-down for variable costs (utilities). Implement rolling forecasts (update monthly) and scenario planning (best/base/worst case) for flexibility
- Start simple with Excel to prove value, then automate. Always include 5-10% buffer for unknowns and track forecast accuracy over time (aim for ±5% variance)

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Below are my considerations:**

- Preserve cost history for ROI justification (prove new warehouse saves money vs old), audit compliance (tax authorities need 7+ years of records), and continuous business unit performance tracking. Reusing business unit code maintains unbroken cost timeline for the market/region
- Track transition costs separately (moving, training, downtime) - these are one-time expenses that shouldn't be mixed with ongoing operational costs. Budget for 3-month ramp-up inefficiency period
- Set clear targets using old warehouse as baseline (e.g., 15% cost reduction). Monitor weekly: actual vs budget, cost per order, labor productivity. New warehouse should hit targets within 6 months or investigate root causes

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
