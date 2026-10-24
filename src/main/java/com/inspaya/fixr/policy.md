# Fixr: User Validity Score Policy

Policy specification · Version 1.0 · 24 October 2026

### Implementation compatibility

Fixr retains its current authenticated vote contract and `UPVOTE` / `DOWNVOTE` values. An `UPVOTE` is interpreted as support for the report; a `DOWNVOTE` is interpreted as dispute. Validation and reputation evaluation run asynchronously after the vote transaction commits. The durable outbox, validation snapshot, and score-event ledger are owned by Spring Boot; the embedding worker remains responsible only for embeddings and duplicate ranking.

## 1. Purpose and scope

Fixr allows users to report civic issues and participate in validating claims made by other users. Since users have different histories of reliable and unreliable assessments, their votes should not necessarily carry equal weight.

The User Validity Score Policy introduces a reputation mechanism that determines how much influence each user's vote has on a report's community validation outcome.

The policy has four objectives:

1. Encourage accurate, evidence-informed participation.
2. Make coordinated manipulation more difficult.
3. Prevent new or historically unreliable accounts from disproportionately influencing reports.
4. Maintain transparent, auditable decisions that can be recalculated when necessary.

### Core principles

- Every eligible user may support or dispute a report.
- A user's voting influence is directly proportional to their validity score.
- A user's score starts low and increases or decreases as their assessments are evaluated against eventual weighted voting consensus.
- Insufficient participation does not prevent a report from being published.
- Uncertain reports remain visible with a provisional status.
- A vote is not itself proof that a claim is true.

## 2. Domain terminology

Fixr should maintain separate concepts for the report, its validation outcome, and the reputation of the users participating in that validation.

| Concept             | Meaning                                                                                                |
| ------------------- | ------------------------------------------------------------------------------------------------------ |
| Report              | A user's submitted observation or claim about a civic issue.                                           |
| Report validation   | The community's assessment of the credibility of that report.                                          |
| User validity score | A numerical measure of how consistently a user's assessments align with qualifying consensus outcomes. |
| Vote weight         | The influence a user's vote contributes to report validation.                                          |
| Validation outcome  | The provisional, supported, disputed, or inconclusive state derived from voting.                       |
| Score event         | An auditable record of a change to a user's validity score.                                            |

The score measures demonstrated performance within Fixr's validation system. It is not a measure of a person's character, identity, or general trustworthiness.

The report's validation state must also remain distinct from its resolution state. A report may be credible even when the underlying issue has not been fixed.

## 3. User validity score rules

### 3.1 Score range and initialization

Use a numeric score in the range \\(0\\) to \\(100\\).

| Rule                  | Policy                                                                  |
| --------------------- | ----------------------------------------------------------------------- |
| Initial score         | 10                                                                      |
| Minimum score         | 0                                                                       |
| Maximum score         | 100                                                                     |
| Score increase        | Earned through qualifying correct assessments                           |
| Score decrease        | Applied for qualifying incorrect assessments                            |
| Insufficient evidence | No score change                                                         |
| Suspicious activity   | Flag for investigation; do not automatically assume a vote is incorrect |

A new account starts with a score of 10. This permits participation while limiting the influence of accounts with no established validation history.

The initial score is a bootstrap value, not an assertion that a new user has a 10% probability of being correct.

### 3.2 Vote weight

The vote weight is the user's validity score divided by 100.

\\[ w_u = \frac{S_u}{100} \\]

Where:

- \\(w_u\\) is the user's vote weight.
- \\(S_u\\) is the user's validity score at the time of voting.

Examples:

| User score | Vote weight |
| ---------- | ----------- |
| 10         | 0.10        |
| 25         | 0.25        |
| 50         | 0.50        |
| 75         | 0.75        |
| 100        | 1.00        |

A user with a score of 80 contributes eight times as much voting weight as a user with a score of 10.

This implements your chosen directly proportional model. There is no additional reputation-tier multiplier.

Important consequence: a score of zero produces zero voting influence. A zero-score user may still submit a vote, but it will not affect the aggregate result until their score changes through a legitimate, qualifying process. This needs to be an explicit policy decision because otherwise a score of zero can become a permanent exclusion mechanism.

### 3.3 Score update mechanism

Scores should not change immediately after a user votes. They change only when the corresponding validation outcome qualifies for scoring.

Use an auditable score-event ledger. The current score is a materialized value derived from the user's initial score and subsequent events.

For a qualifying assessment, define:

\\[ S\_{new} = \min(100,\max(0,S\_{old}+\Delta S)) \\]

The change \\(\Delta S\\) depends on whether the user's vote agrees with the qualifying eventual consensus and the confidence assigned to that outcome.

I recommend the following initial scoring parameters for a first implementation:

| Parameter                       | Proposed value                |
| ------------------------------- | ----------------------------- |
| Correct assessment adjustment   | +2 points                     |
| Incorrect assessment adjustment | −2 points                     |
| Minimum eligible voters         | 5 distinct users              |
| Minimum consensus share         | 75%                           |
| Minimum score change per report | One event per user per report |

These are initial product parameters, not empirically validated constants. They should be configurable and evaluated against observed voting behaviour before being treated as permanent policy.

The Spring implementation reads quorum, thresholds, score adjustment, scoring version, and abuse-signal windows from `fixr.validation` configuration. Defaults are five voters, 75%/25%, and a two-point adjustment; deployments can override them with the corresponding `FIXR_VALIDATION_*` environment variables.

## 4. Report validation algorithm

### 4.1 Weighted support ratio

Each report receives support and dispute votes. The system calculates the total weight on each side.

Let:

- \\(W_s\\) = sum of weights of users supporting the report.
- \\(W_d\\) = sum of weights of users disputing the report.

The weighted support ratio is:

\\[ R_s = \frac{W_s}{W_s+W_d} \\]

The result is calculated only when the denominator is greater than zero and the applicable participation requirements are satisfied.

Example:

| Vote    | Score | Weight |
| ------- | ----- | ------ |
| Support | 80    | 0.80   |
| Support | 60    | 0.60   |
| Dispute | 20    | 0.20   |
| Dispute | 10    | 0.10   |

\\[ R_s = \frac{1.40}{1.70} \approx 82.35\\% \\]

This example demonstrates the arithmetic only. Four votes do not meet the proposed minimum of five distinct voters, so this report would remain provisional.

### 4.2 Outcome thresholds

Apply symmetrical thresholds to the weighted support ratio.

| Condition                                              | Validation state |
| ------------------------------------------------------ | ---------------- |
| Fewer than five eligible votes                         | `PROVISIONAL`    |
| \\(R_s \geq 0.75\\), with sufficient participation     | `SUPPORTED`      |
| \\(R_s \leq 0.25\\), with sufficient participation     | `DISPUTED`       |
| \\(0.25 < R_s < 0.75\\), with sufficient participation | `INCONCLUSIVE`   |

A provisional report remains visible. It is not automatically hidden, rejected, or treated as false because few users have assessed it.

The system should recalculate the aggregate when votes change, subject to the voting rules. If the validation period expires without sufficient participation, the report remains provisional rather than being assigned an unsupported verdict.

### 4.3 Preventing a user's vote from validating itself

There is a subtle but important problem with the chosen policy: if consensus determines user validity, and user validity determines voting influence, the system can reinforce its own mistakes.

For example, a group of users may agree on a false claim. If their agreement is used to increase their scores, their future votes become more influential, making the same group's future consensus even harder to challenge.

The policy should therefore use a leave-one-voter-out evaluation when updating reputation.

For each user whose score is being evaluated:

1. Temporarily exclude that user's vote from the report's scoring consensus.
2. Recalculate the weighted consensus from the remaining eligible votes.
3. Confirm that the remaining votes satisfy the participation and confidence thresholds.
4. Compare the excluded user's vote with that independent-of-their-own-vote consensus.
5. Apply the appropriate score event only if the outcome qualifies.

This prevents a user's own vote from directly contributing to the consensus used to reward or penalize that same vote.

It does not eliminate collusion or prove that a consensus is factually correct. It is a safeguard against circular scoring, not a replacement for abuse detection.

### 4.4 User score update example

Suppose a user with a score of 30 supports a report. The vote is recorded with a weight of 0.30.

Later, the report reaches a qualifying consensus using the other eligible voters' votes. The resulting consensus supports the claim.

- If the user's vote agrees with that consensus, the proposed score changes from 30 to 32.
- If the user's vote contradicts the qualifying consensus, the proposed score changes from 30 to 28.
- If the other votes are insufficient or inconclusive, the user's score remains 30.

The user's score at the time of voting is preserved for the original calculation. A later score change does not retroactively change the historical vote weight.

The report's aggregate voting result may still be recalculated using the current eligible votes and the appropriate recorded weights. Historical results should be retained for auditing.

## 5. Anti-manipulation safeguards

The following rules form part of the policy rather than optional future enhancements.

Unique participation

Enforce one active vote per user per report. A database uniqueness constraint should prevent duplicate votes, including simultaneous requests. Users may change their vote only through an explicit, auditable update.

Score bounds and conservative initialization

Enforce the 0–100 range and initialize new accounts at 10. Never allow users to manufacture reputation simply by creating accounts or repeatedly voting on the same report.

Coordination detection

Flag suspicious reciprocal voting, clusters of accounts that consistently support one another, bursts of coordinated activity, and repeated targeting of particular reporters. Do not equate disagreement or geographic proximity alone with collusion.

Immutable score history

Record every score adjustment, its reason, the related report, and the consensus snapshot used. Correct mistakes with compensating events rather than silently overwriting history.

Abuse is not the same as inaccuracy

A vote that disagrees with consensus is not automatically malicious. Suspicious activity should be flagged for investigation; score penalties should follow the published scoring rules and any applicable review process.

Idempotent processing

Ensure that a report's final scoring outcome can generate at most one effective score event per user and report for a given scoring version. Replayed messages or retried jobs must not award the same points repeatedly.

### Important limitation of consensus-based reputation

Under your chosen approach, the system learns from eventual weighted consensus rather than independent ground truth. That means the resulting score measures agreement with qualifying consensus, not verified factual accuracy.

This distinction should appear in the internal documentation and product language. If Fixr later gains reliable moderator verdicts or independently verified outcomes, those can provide stronger evidence for evaluating user assessments without silently changing the meaning of the existing score.

## 6. Implementation-ready domain model

Given Fixr's existing Spring Boot and PostgreSQL architecture, I would implement this as a dedicated validation and reputation domain in the core application.

The Python worker remains responsible for report-embedding generation and similarity ranking. It should not calculate user reputation or decide report validity. Spring Boot owns the authoritative voting rules, score updates, validation states, and persistence.

### 6.1 Entity relationships

This model intentionally separates the current user score from the events that explain how it was earned.

### 6.2 Entity responsibilities

| Entity                | Responsibility                                                                                   |
| --------------------- | ------------------------------------------------------------------------------------------------ |
| `UserValidityProfile` | Stores the current score and reputation statistics for a user.                                   |
| `ReportVote`          | Stores the user's current vote on a report and its recorded weight.                              |
| `ValidationSnapshot`  | Captures the weighted result used to determine a report's validation outcome and evaluate votes. |
| `ValidityScoreEvent`  | Records each reputation adjustment, including the reason and resulting score.                    |

Use your existing user and report entities. These are additions to the domain, not replacements for the existing Fixr data model.

### 6.3 Proposed Java enums

```
public enum ReportVoteType {
    UPVOTE,
    DOWNVOTE
}
```

For validation calculations, `UPVOTE` maps to support and `DOWNVOTE` maps to dispute. The existing public vote labels remain unchanged.

```
public enum ReportValidationStatus {
    PROVISIONAL,
    SUPPORTED,
    DISPUTED,
    INCONCLUSIVE
}
```

```
public enum ValidityScoreReason {
    CONSENSUS_AGREEMENT,
    CONSENSUS_DISAGREEMENT,
    SCORE_CORRECTION,
    ADMINISTRATIVE_ADJUSTMENT
}
```

The `RESOLVED` state should remain separate from `ReportValidationStatus`. A report can be supported without the underlying issue being resolved.

### 6.4 `UserValidityProfile`

```
@Entity
@Table(name = "user_validity_profile")
public class UserValidityProfile {

    @Id
    private UUID userId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal validityScore = BigDecimal.TEN;

    @Column(nullable = false)
    private int scoredAssessments;

    @Version
    private long version;

    // Getters and domain methods
}
```

Implementation notes:

- Use the user's existing ID as the profile's primary key and foreign key to the user table.
- Create the profile when the user becomes eligible to participate in validation.
- Use `BigDecimal` for persistent score values rather than floating-point types.
- Keep `scoredAssessments` limited to assessments that have actually qualified for scoring.
- Use optimistic locking or equivalent concurrency control to prevent concurrent score updates from being lost.

### 6.5 `ReportVote`

```
@Entity
@Table(
    name = "report_vote",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_report_vote_user_report",
        columnNames = {"report_id", "user_id"}
    )
)
public class ReportVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Report report;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private FixrUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportVoteType voteType;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal weightAtVote;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(length = 500)
    private String explanation;

    // Getters and domain methods
}
```

The unique constraint ensures that a user cannot create multiple vote records for the same report. Application-level checks alone are insufficient because concurrent requests can bypass them.

Vote changes: retain the same vote record and update the decision, weight, explanation, and timestamp. Record the previous decision and weight in an audit history if vote revisions are permitted. The current vote should count only once in the aggregate.

Optional explanations should never be required for a vote to count.

### 6.6 `ValidityScoreEvent`

```
@Entity
@Table(
    name = "validity_score_event",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_score_event_user_report_version",
        columnNames = {
            "user_id", "report_id", "scoring_version"
        }
    )
)
public class ValidityScoreEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Report report;

    private UUID validationSnapshotId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal scoreDelta;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal scoreAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidityScoreReason reason;

    @Column(nullable = false)
    private String scoringVersion;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
```

The database uniqueness rule should include a stable scoring version or equivalent idempotency key. This permits an explicitly authorized recalculation under a new policy version without accidentally awarding the same adjustment repeatedly under the original version.

For production, enforce foreign keys to the validation snapshot, appropriate indexes, and database constraints for score bounds and vote-weight bounds. Treat score events as append-only records.

## 7. Application services

Keep business rules in domain services rather than controllers or repository methods.

`ReportVotingService`

Accepts votes and vote changes, checks eligibility, prevents duplicate participation, snapshots the applicable weight, and requests validation recalculation.

`ReportValidationService`

Calculates weighted support, checks the quorum and consensus thresholds, updates the report's validation status, and records validation snapshots.

`UserValidityService`

Evaluates eligible votes against qualifying consensus, creates score events, and updates user validity profiles.

`VotingAbuseDetectionService`

Flags suspicious patterns and exposes them for moderation. It should not automatically equate consensus disagreement with abuse.

### Processing flow

Use a transactional boundary around the vote and its outbox event. A scheduled Spring worker claims outbox records and calculates weighted validation and score events in a separate transaction. Failed work is retried with backoff. Score-event uniqueness by user, report, and scoring version makes replay safe. The Python worker remains responsible for embeddings and duplicate ranking only.

## 8. API contract

The existing API remains:

| Method   | Endpoint                                               | Purpose |
| -------- | ------------------------------------------------------ | ------- |
| `PUT`    | `/api/v1/fixr/feed/reports/{reportId}/vote`             | Cast or change the current user's vote. |
| `DELETE` | `/api/v1/fixr/feed/reports/{reportId}/vote`             | Remove the current user's vote. |
| `GET`    | `/api/v1/fixr/feed/reports/{reportId}/validation`       | Retrieve the latest asynchronously calculated validation result. |
| `GET`    | `/api/v1/fixr/user/validity`                            | Retrieve the authenticated user's score and score-event history. |
| `GET`    | `/api/v1/fixr/moderation/voting-abuse-flags`            | Retrieve open abuse flags; moderator authority required. |
| `PATCH`  | `/api/v1/fixr/moderation/voting-abuse-flags/{flagId}`    | Record a moderator's review outcome and notes. |

Example request:

```
{
  "vote": "UPVOTE"
}
```

Use `"DOWNVOTE"` to dispute a report. Vote changes retain the prior vote and weight in revision history. The vote response remains the existing vote summary; clients can poll the validation endpoint for the new weighted status because evaluation is asynchronous.

The server must derive the voter identity from the authenticated principal. It must not accept a user ID or vote weight supplied by the client as authoritative.

The validation response can expose the current status, eligible vote count, weighted support ratio, and whether the result is provisional. Whether individual votes and voter identities are publicly visible should be governed by Fixr's privacy policy.

## 9. Acceptance criteria

The policy is ready for an initial implementation when these behaviours are tested:

- A new user receives a validity score of 10.
- Vote weight equals the score divided by 100 at the applicable voting snapshot.
- Each user can contribute at most one active vote per report.
- Fewer than five eligible voters leave the report provisional.
- A qualifying weighted support ratio of at least 75% produces `SUPPORTED`; at most 25% produces `DISPUTED`.
- Intermediate qualifying results produce `INCONCLUSIVE`.
- Votes that do not have a qualifying consensus outcome do not change user scores.
- A user's own vote is excluded when evaluating that user's agreement with consensus.
- Qualifying agreement adds two points; qualifying disagreement subtracts two points, subject to score bounds.
- Replayed events cannot apply the same score adjustment twice.
- Concurrent votes and score updates cannot silently overwrite one another.
- Vote changes and score adjustments remain auditable.
- A report's validation state is not mistaken for its resolution state.

Test boundary conditions explicitly, including scores of 0 and 100, tied weighted results, a single high-weight voter, simultaneous duplicate requests, changed votes, and reports that never reach a qualifying consensus.

## 10. Final policy decision

This is a coherent first version of Fixr's reputation-weighted validation system. It is implementable with Spring Boot, JPA, and PostgreSQL, and it leaves room for future moderation and evidence-based verification without requiring a redesign of the scoring domain.

There is one limitation to accept consciously: the score will measure agreement with a qualifying weighted consensus, not objective truth. Leave-one-voter-out evaluation reduces direct self-reinforcement, but it cannot eliminate coordinated groups that manufacture consensus.

For that reason, the policy should be versioned, audited, and evaluated against real platform behaviour before reputation is allowed to determine high-impact decisions automatically.
