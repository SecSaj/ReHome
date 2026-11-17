# ReHome — Deliverable 4: Second Prototype + Tests

**CS 2114 — Software Design & Data Structures — Fall 2026**

**Team:** Sajjad, Liam, Luc, Tayshaun

## Project Overview

ReHome is a Java console application that allows college students to give away usable items instead of throwing them away. Students can create listings, search for available items, and join individual FIFO waitlists. Donors can pass an item to the next student or mark it as claimed.

For Deliverable 4, we expanded our first prototype by implementing **waitlist cancellation**. Students can now remove their requests from any position in a singly linked queue without changing the order of the remaining students.

The application also includes an automatic five-minute offer timer, input validation, MVC separation, and JUnit 5 tests.

## How to Run

### Requirements

- Java JDK installed
- Eclipse IDE
- JUnit 5 (Jupiter)

No Maven, internet connection, database, or external account is required.


Compiling the Project

Eclipse automatically compiles the Java files when Project → Build Automatically is enabled. To compile manually, select Project → Build Project. Any compilation errors will appear in the Problems view.

### Import into Eclipse

1. Download the repository as a ZIP from GitHub.
2. Extract the ZIP.
3. Open Eclipse.
4. Select **File → Import → General → Existing Projects into Workspace**.
5. Select the extracted ReHome project folder.
6. Complete the import.
7. If necessary, add **JUnit 5** through **Build Path → Add Libraries → JUnit → JUnit 5**.

### Run the Application

1. Open `src/ReHomeApp.java`.
2. Right-click the file.
3. Select **Run As → Java Application**.
4. Follow the text-menu instructions.

The application supports:

1. Add Item
2. View Available Items
3. Search Items
4. Join Waitlist
5. Show Next Student
6. Pass Student
7. Claim Item
8. Cancel Waitlist Request
0. Quit

### Run the Model Independently

Run `ModelDemo.java` as a Java application.

This demonstrates model operations without constructing the controller or console view, helping verify that the model is independent of the interface.

### Run JUnit 5 Tests

1. Right-click the `test` source folder.
2. Select **Run As → JUnit Test**.
3. The JUnit results panel displays the test results.

The test classes are:

- `LinkedQueueTest.java`
- `ReHomeModelTest.java`
- `ItemSnapshotTest.java`
- `ReHomeControllerTest.java`

The tests cover FIFO operations, waitlist cancellation, model validation, timer behavior, item snapshots, and controller interactions.

## Reflection

### How did our design absorb the change?

Our Deliverable 3 Next Steps identified **Cancel Waitlist Request** as our planned feature.

We implemented cancellation by extending the existing linked queue and model logic instead of creating a separate waitlist system.

The new `remove(T entry)` method in `LinkedQueue` traverses the singly linked queue and removes the matching node. It handles removal from the front, middle, tail, and single-node queue while preserving FIFO order.

The model's `cancelWaitlist(int itemId, String studentName, long nowMillis)` method validates the request, finds the matching student, updates the queue, and adjusts the offer deadline when necessary.

If the front student cancels, the next student receives a new full offer window. If a middle or tail student cancels, the front student's existing deadline remains unchanged.

### Actual Changes Compared With Our D3 Prediction

| Class | D3 Prediction | Actual D4 Change |
|---|---|---|
| `LinkedQueue.java` | Add `remove(T entry)` | Added removal supporting head, middle, tail, and single-node cases |
| `ReHomeModel.java` | Add `cancelWaitlist()` | Added cancellation validation, queue updates, and deadline handling |
| `ReHomeController.java` | Add `onCancel()` and menu handling | Added cancellation handling and updated waitlist display behavior |
| `ConsoleView.java` | Add cancellation menu option | Added option 8 and a method for displaying the updated waitlist |
| `Item.java` | No changes | No changes required |
| `ItemSnapshot.java` | No changes | No changes required |
| `ReHomeApp.java` | No changes | No changes required |
| `ModelDemo.java` | No changes | No changes required |

Our prediction correctly identified the four classes that needed modification.

One additional detail was the need to display the complete waitlist after cancellation. We added `showWaitlist()` in the view and updated `onShowNext()` in the controller to retrieve a fresh snapshot and display the remaining students.

The four application classes predicted to remain unchanged did not require modification for this feature.

### Did the Change Cross the Model/View Boundary?

The change affected the model, controller, and view, but each layer retained its original responsibility.

- **Model:** Validates cancellation requests, changes the linked queue, and updates deadlines.
- **Controller:** Receives cancellation input, calls the model, and retrieves updated information.
- **View:** Displays the cancellation option, receives input, and prints the updated waitlist.

The view does not store its own copy of the application's item or waitlist state.

Instead, the controller requests a fresh `ItemSnapshot` from the model whenever the waitlist needs to be displayed.

The view never receives or modifies the linked queue's head, tail, or nodes.

Therefore, the change crossed the layers through their existing public interfaces without requiring the view to understand the queue's implementation.

### What Changed From Our D3 Plan?

The main change feature remained the same: waitlist cancellation.

We followed the planned `remove(T entry)` and `cancelWaitlist()` method signatures.

However, the implementation required additional handling for:

- Case-insensitive student-name matching.
- Canceling a request after an offer has already expired.
- Preserving the current deadline when a non-front student cancels.
- Starting a new deadline when the front student cancels.
- Displaying the updated FIFO order after cancellation.
- Testing multiple cancellation positions and invalid requests.

We also added an interleaved enqueue/dequeue test to demonstrate that the linked queue maintains FIFO order through repeated modifications.

These changes strengthened the feature without requiring a redesign of the existing application.

### What Would Make the Next Change Cheaper?

One future improvement would be to centralize item-state transitions into a smaller set of model operations.

Currently, claiming, passing, cancellation, and expiration each interact with the item's availability, queue, or deadline.

If we later added pickup confirmation or no-show recovery, introducing a clearer item-status representation and reusable transition methods would reduce duplicated state-management logic.

This would make it easier to add new item states without repeatedly changing unrelated code.

We would keep these rules in the model rather than moving them into the controller or view.

### What Doesn't Work Yet?

The core listing, search, waitlist, pass, claim, timer, and cancellation features are implemented.

The remaining work includes:

- Completing and verifying same-field input retries for all invalid text entries.
- Running the entire JUnit 5 suite in Eclipse and correcting any failures.
- Checking additional sequences of invalid menu inputs.
- Performing more integration testing of the background timer.
- Improving the console's presentation and user messages.
- Reviewing final Javadocs and documentation for consistency.

The console does not continuously redraw a live countdown while a student types. Background expiration occurs, and the updated waitlist appears when the console next displays the item.

Waitlist cancellation is implemented. No-show pickup confirmation, wish-list matching, and pickup scheduling remain outside the current build.

## Next Steps

### Remaining Work for the Final Build

We plan to complete the remaining tasks in this order:

1. Verify that all JUnit 5 tests execute successfully in Eclipse.
2. Improve input retries for invalid names, descriptions, and other model-level inputs.
3. Test repeated invalid inputs and ensure the application continues running.
4. Check the five-minute timer across multiple item waitlists.
5. Test complete add, search, join, cancel, pass, and claim workflows.
6. Improve console messages and formatting where necessary.
7. Review Javadocs, README instructions, and final work attribution.
8. Perform a final team demonstration and fix any discovered issues.

### Stretch Goals: Committed vs. Dropped

**Committed:**

- Waitlist cancellation — implemented in the second prototype.
- Automatic five-minute offers — retained and included in the working application.

**Not committed for the final build:**

- No-show pickup tracking.
- Wish-list matching.
- Pickup scheduling.

We decided to prioritize correctness, input handling, testing, and reliable queue behavior instead of expanding the application's scope unnecessarily.

### Three-Minute Demonstration Plan

| Time | Demonstration |
|---|---|
| 0:00–0:20 | Launch ReHome and show the console menu |
| 0:20–0:45 | Add a desk lamp and a textbook |
| 0:45–1:10 | Search for the lamp and join Liam, Luc, and Tayshaun to its waitlist |
| 1:10–1:40 | Cancel Luc's middle request and show that Liam and Tayshaun remain in order |
| 1:40–2:05 | Cancel Liam's front request and show that Tayshaun becomes first with a new deadline |
| 2:05–2:25 | Attempt an invalid cancellation and show that the program continues running |
| 2:25–2:45 | Claim the lamp for Tayshaun and show that it is no longer available |
| 2:45–3:00 | Show the JUnit 5 test results and briefly explain the queue's independent tests |

**Total planned duration: 3 minutes.**

The demonstration focuses on the feature introduced in Deliverable 4 while also showing that the existing functionality remains operational.

### Remaining Team Responsibilities

| Team member | Planned final-build responsibilities |
|---|---|
| Sajjad | Model integration, cancellation validation, timer integration and testing, model tests, documentation |
| Liam | Linked queue verification, removal edge cases, queue JUnit tests |
| Luc | Controller integration, cancellation handling, controller tests |
| Tayshaun | Console input recovery, menu usability, invalid-input testing |

All team members will participate in verifying the application and preparing the final demonstration.

## Attribution

| Class or Test File | Contributions |
|---|---|
| `LinkedQueue.java` | **Liam:** Linked FIFO operations, node removal, and head/tail handling. **Sajjad:** FIFO behavior and integration with the ReHome model. |
| `Node<T>` (nested in `LinkedQueue.java`) | **Liam:** Node and link design, head/tail updates, and removal edge cases. **Sajjad:** Last-node removal and queue behavior review. |
| `Item.java` | **Sajjad:** Item fields, waitlist ownership, claim state, and deadline design. |
| `ItemSnapshot.java` | **Sajjad:** Snapshot methods, getters, and defensive-copy design. |
| `ReHomeModel.java` | **Sajjad:** Model methods, cancellation logic, validation, offer expiration, and timer-related model behavior. **Tayshaun:** Bad-input and edge-case review. |
| `ConsoleView.java` | **Tayshaun:** Console menu, input handling, error messages, and cancellation display updates. |
| `ReHomeController.java` | **Luc:** Controller actions, cancellation menu handling, and model/view communication. **Sajjad:** Automatic timer integration, deadline coordination, and model/controller interactions. |
| `ReHomeApp.java` | **Luc:** Application startup and controller/view/model integration. |
| `ModelDemo.java` | **Sajjad:** Standalone model demonstration and fixed-time test scenarios. **Luc:** Integration-flow review. |
| `LinkedQueueTest.java` | **Liam:** Queue tests, cancellation cases, and head/tail verification. **Sajjad:** FIFO and interleaved-operation test planning. |
| `ReHomeModelTest.java` | **Sajjad:** Model tests, cancellation validation, deadline behavior, and stale-offer cases. **Tayshaun:** Invalid-input test cases and review. |
| `ItemSnapshotTest.java` | **Sajjad:** Snapshot getter tests and defensive-copy testing. |
| `ReHomeControllerTest.java` | **Luc:** Controller interaction tests and cancellation behavior. **Tayshaun:** Input-recovery cases. **Sajjad:** Timer and integration test planning. |

### GenAI Note

We used ChatGPT throughout the development of ReHome to help brainstorm ideas, organize our scope and specification, and clarify how different parts of the application should work together.

For the first prototype, we used it to help draft and revise some of our Java methods, Javadocs, and JUnit 5 tests. We also used it to better understand how the model, controller, and view should be separated.

We used ChatGPT more heavily when working on the automatic five-minute timer because we were unfamiliar with some of the Java concepts involved, especially `Timer`, `TimerTask`, deadlines in milliseconds, and synchronized methods. It helped us work through the timer implementation and how it would interact with the existing waitlist logic.

For Deliverable 4, we used ChatGPT to help draft and revise the methods for waitlist cancellation, including `remove(T entry)` and `cancelWaitlist()`. It also helped us expand our existing JUnit 5 tests to cover removal from different positions, invalid inputs, timer behavior, and FIFO order after multiple operations.

We also used it to help organize and revise the README, including our reflection, next steps, attribution, and demonstration plan. We used the suggestions while developing and improving the methods and features we had planned for ReHome.
