# ReHome
ReHome — Deliverable 3: First Prototype
CS 2114 · Fall 2026
Team: Sajjad, Liam, Luc, Tayshaun

ReHome is a single-session Java console program where students post free items and join a separate FIFO waitlist for each item. This is a first prototype, not a finished application. The five-minute offer timer is an attempted stretch goal, not part of our original MVP.
How to Run
In Eclipse, open the ReHome Java project with application classes in src and JUnit 5 tests in test. Add JUnit 5 through Build Path → Add Libraries → JUnit → JUnit 5 if needed; no Maven is required.
- Model alone: Run ModelDemo.java as a Java Application. It adds an item, joins two students, simulates an expiration, and claims the item without a view.
- Thin view: Run ReHomeApp.java as a Java Application. Use the console menu to add/search items, join a waitlist, show the next student, pass, and claim.
- Tests: Run the test source folder as a JUnit Test. The four files contain 32 JUnit 5 test methods. Confirm the actual pass/fail results in Eclipse before submission.
Reflection
What changed from the specification, and why?
- Timer: Our original scope's MVP did not include a timer. We later added a five-minute timer to the specification and attempted it early as a stretch goal because an unresponsive student should not block the line. Item stores the deadline, ReHomeModel.expireOffers(long) enforces it, and ReHomeController.run() schedules checks. passStudent(...) and claimItem(...) check the expected front name so an expired student cannot act for the next person.
- Classes and state: We used the specified linked LinkedQueue<T> per item, plus ItemSnapshot to provide copies instead of exposing the actual queue. Item.snapshot() is a small helper. Item fields are package-private for simple model access rather than fully encapsulated; this is a first-prototype tradeoff.
- Validation and view: As specified, the model validates names, IDs, duplicates, lengths, and invalid characters; the view handles number parsing; the controller displays errors. However, a model-level error currently returns to the menu rather than re-prompting at the same field. We kept the view thin for this checkpoint.
- Tests: We implemented JUnit 5 tests using fixed timestamps rather than waiting five minutes. This was prompted by the need to test empty, one-person, and multiple-person queues and expired offers repeatably. The suite still needs to be run and checked in the team's Eclipse setup.
Did the model stand alone? Yes. ModelDemo and the queue/model tests use no ConsoleView or ReHomeController. The model has no Scanner or printing. The controller schedules timer checks; the model owns expiration rules. Nothing had to move into the view to make the model run.

What happened to our unknowns? Removing the last queue entry resets both head and tail correctly; duplicate checking uses an ordered copy from toList() without changing FIFO order; JUnit tests cover empty, one-person, and multi-person cases. Removing a student from the middle remains open for the cancellation feature in Deliverable 4.

What does not work yet? Input errors do not always re-prompt at the same field. There is no cancellation action, pickup/no-show tracking, wish-list matching, or pickup scheduling. The timer updates the queue but does not print an automatic expiration message during console input. We still need to verify the actual Eclipse JUnit 5 results and manually test repeated bad inputs and a real timeout.
Next Steps
Finish the MVP in this order: (1) Run all JUnit 5 tests and fix failures; (2) improve same-field re-prompting for invalid names and other model errors; (3) manually test the full add → search → join → pass → claim path and repeated bad inputs; (4) verify Javadocs and rerun tests. Separately, verify the already attempted timer stretch goal while the console waits for input.

Deliverable 4 non-trivial change feature: Cancel a Waitlist Request. A student will be able to leave from the front, middle, or back of a queue. This changes model state and will be available through the menu. We predict these specific changes:
- LinkedQueue.java: Add boolean remove(T entry); correctly update head/tail and preserve order. Existing queue methods should not need changes.
- ReHomeModel.java: Add synchronized boolean cancelWaitlist(int itemId, String studentName, long nowMillis); reuse findItem(), checkText(), requireAvailable(), and expireItem(). If the front cancels, start a fresh deadline for the new front; otherwise keep the current deadline.
- ReHomeController.java: Add onCancel(int itemId, String studentName) and update run() to dispatch a new menu choice.
- ConsoleView.java: Update readMenuChoice(); reuse readItemId(), readStudentName(), showMessage(), and showError().
- Expected not to change: Item.java, ItemSnapshot.java, ReHomeApp.java, and ModelDemo.java.
New tests: In LinkedQueueTest, cover removing from empty, head, middle, tail, only-node, and absent-name cases. In ReHomeModelTest, cover front cancellation resetting the deadline, middle/tail cancellation preserving it, case-insensitive names, bad IDs, and claimed items. In ReHomeControllerTest, test the new menu path and error message. ItemSnapshotTest should not need changes.

Stretch goals: We have attempted the five-minute timer in this prototype and plan to attempt waitlist cancellation as Deliverable 4's change feature. We are dropping pickup/no-show tracking, wish-list matching, and pickup scheduling from our Deliverable 4 plan.

Proposed ownership for remaining work: Liam — queue removal and queue tests; Sajjad — model cancellation, validation, model tests, and integration; Luc — controller menu action, timer verification, and controller tests; Tayshaun — view/menu updates, input re-prompting, manual bad-input tests, and Javadoc review. Assignments must be confirmed by the team.
Attribution
The contributions below reflect the roles and design work documented in our scope and Deliverable 2 specification. They describe design contributions, not independently verified authorship of the Java implementation. Our team will ensure the final attribution accurately reflects each person's substantive work on the source or test files.
Class or test file	Team member(s)	Documented design contribution
LinkedQueue.java	Liam, Sajjad	Liam: planned linked-node operations and head/tail behavior. Sajjad: developed FIFO reasoning and the queue's role in ReHome.
Node<T> (nested in LinkedQueue.java)	Liam, Sajjad	Liam: worked on head/tail invariants. Sajjad: helped specify correct removal of the last node.
Item.java	Sajjad	Specified item fields, availability state, individual waitlists, and offer deadlines.
ItemSnapshot.java	Sajjad	Developed the snapshot class contract and defensive-copy requirements to preserve the model/view boundary.
ReHomeModel.java	Sajjad, Tayshaun	Sajjad: led model method signatures, validation and timeout rules, and integration design. Tayshaun: reviewed bad-input and edge-case requirements.
ConsoleView.java	Tayshaun, Sajjad	Tayshaun: led console input and error-message design. Sajjad: specified boundary requirements for what the view may display and access.
ReHomeController.java	Luc, Sajjad	Luc: owned controller flow and timer integration planning. Sajjad: specified timer/model separation and stale-offer safeguards.
ReHomeApp.java	Luc, Sajjad	Luc: planned application startup and MVC wiring. Sajjad: coordinated the integration plan and reviewed class responsibilities.
ModelDemo.java	Sajjad, Luc	Sajjad: developed the independent-model demonstration and fixed-time test scenarios. Luc: contributed integration workflow requirements.
LinkedQueueTest.java	Liam, Sajjad	Liam: contributed queue edge-case requirements. Sajjad: developed the test matrix for empty, single-node, and multi-node behavior.
ReHomeModelTest.java	Sajjad, Tayshaun	Sajjad: led model and timer test planning, including exact deadlines and stale claims. Tayshaun: reviewed invalid-input scenarios.
ItemSnapshotTest.java	Sajjad	Specified getter, snapshot immutability, and defensive-copy test cases.
ReHomeControllerTest.java	Luc, Tayshaun, Sajjad	Luc: defined end-to-end controller paths. Tayshaun: developed input-recovery expectations. Sajjad: expanded integration and timeout test scenarios.


GenAI note: ChatGPT helped organize the scope and specification and generated initial Java code, Javadocs, JUnit 5 tests, and README wording. We also asked it to predict the exact classes and methods affected by the proposed waitlist-cancellation feature (see Next Steps). The team must review the code, run the tests, and report
