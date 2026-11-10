[README (2).md](https://github.com/user-attachments/files/34650419/README.2.md)
# ReHome — Deliverable 3: First Prototype

**CS 2114 · Fall 2026**  
**Team:** Sajjad, Liam, Luc, Tayshaun

ReHome is a Java console program for posting free items and joining a FIFO waitlist for each item. This first prototype demonstrates the queue-backed model, basic menu actions, and a five-minute offer timer. **The timer is a stretch goal we attempted early; it was not part of our original MVP.**

## How to Run

Open the `ReHome` Java project in Eclipse. The application files are in `src` and the tests are in `test`. Add the **JUnit 5** library through **Build Path → Add Libraries → JUnit → JUnit 5**. No Maven is needed.

- **Model without a view:** Run `ModelDemo.java` as a **Java Application**. It creates an item, queues two students, simulates an expiration, and records a claim without using the console view or controller.
- **Console prototype:** Run `ReHomeApp.java` as a **Java Application**. Use the menu to add items, browse/search, join a waitlist, show the next student, pass, and claim. Each item's timer runs while the console waits for input; the updated front appears the next time it is displayed.
- **JUnit 5 tests:** Right-click the `test` source folder and select **Run As → JUnit Test**. The four test classes contain **32 test methods**, including tests for core queue operations and model bad-input cases.

A simple MVP path is: add a desk lamp → search for it → join Liam and Luc → pass Liam → claim the item for Luc → confirm it no longer appears under available items.

## Reflection

### What changed from the specification, and why?

| Specification or earlier plan | First-prototype implementation | What prompted it |
| --- | --- | --- |
| The original MVP had no timer; our later specification included timed offers. | We attempted the **five-minute timer as a stretch goal**. `Item.offerDeadlineMillis` stores the deadline, `ReHomeModel.expireOffers(long)` applies expirations, and `ReHomeController.run()` schedules checks. | We wanted a student who does not respond to stop holding up everyone behind them. |
| The model needed a linked FIFO queue for each listing. | `LinkedQueue<T>` has private linked nodes and head/tail references; each `Item` owns one queue. `Item.snapshot()` creates a copy for display. | We needed to prove FIFO behavior separately from the menu and prevent the view from changing queue order. |
| The specification separated model, controller, and view. | We kept `ReHomeModel` free of `Scanner` and console output. `ReHomeController` handles actions and timer scheduling, while `ConsoleView` handles input/output. `Item` fields are package-private instead of fully private. | The first-prototype checkpoint prioritizes a working independent model; direct access inside the model package kept this version simple. |
| Validation belongs to the model; keyboard parsing belongs to the view. | `ReHomeModel` checks names, duplicate requests, lengths, invalid characters, IDs, and item availability. `ConsoleView` retries nonnumeric entries. Model-level errors currently return to the menu instead of retrying the same field. | We implemented the core rules first and kept the view thin. Same-field retry is remaining MVP work. |
| Passes and claims must not act on an expired offer. | `passStudent(int, String, long)` and `claimItem(int, String, long)` use a timestamp and expected-front name; the model checks expiration before accepting either action. | Adding the timer created the possibility that the front student changes between viewing an offer and recording a decision. |
| The specification called for model and queue tests. | We used **JUnit 5** and fixed timestamps in `ReHomeModelTest` instead of waiting for actual time to pass. The test files cover empty, one-person, and multi-person queues, validation, snapshots, and timer behavior. | Fixed-time tests are faster and repeatable, especially for timeout boundaries and stale claims. |

### Did the model stand alone?

Yes. `ModelDemo.main()` exercises `ReHomeModel`, `Item`, and `LinkedQueue` without constructing the view or controller. The queue does not know about items or menus, and the model does not know about `ConsoleView`. The timer's scheduling is handled by the controller, but deciding which offer expires is model logic. We did not move queue or validation rules into the view to make the program work.

### What happened to our unknowns?

- **Head and tail:** When `dequeue()` removes the last entry, both references become `null`; a later `enqueue()` works normally.
- **Duplicate names:** `joinWaitlist()` checks an ordered copy from `toList()` without rearranging the queue.
- **JUnit coverage:** The tests address empty, single-entry, and multiple-entry FIFO behavior, including removing the only entry and adding again.
- **Middle-of-queue removal:** Still open. We plan to solve it with the Deliverable 4 cancellation feature, using previous/current node references and careful tail updates.

### What does not work yet?

The console is intentionally basic. Invalid model-level text currently returns to the main menu instead of re-prompting at the same field. The timer advances queues in the background but does not print a live countdown or automatic timeout announcement during typing. Waitlist cancellation, no-show pickup handling, wish-list matching, and pickup scheduling are not implemented.

## Next Steps

**Finish the MVP in this order:** (1) Finish same-field retries for invalid names and other model-level inputs; (2) run and fix any JUnit 5 or end-to-end failures; (3) test repeated bad input and the complete add → search → join → pass → claim path; (4) review Javadocs and run the full tests again. We will also test the timer stretch goal while the console waits for input.

### Deliverable 4 change feature: Cancel a Waitlist Request

A student will be able to leave an item's waitlist from the front, middle, or back. This **changes the model's queue state** and will be available through the console, so it is a non-trivial feature beyond the MVP.

| File | Predicted methods and changes |
| --- | --- |
| `LinkedQueue.java` | **Add** `boolean remove(T entry)` to remove the matching node while preserving order and updating head/tail. Existing `enqueue()`, `dequeue()`, `peek()`, `isEmpty()`, and `toList()` should stay unchanged. |
| `ReHomeModel.java` | **Add** `synchronized boolean cancelWaitlist(int itemId, String studentName, long nowMillis)`; reuse `findItem()`, `checkText()`, `requireAvailable()`, and `expireItem()`. Reset the deadline only when the current front cancels. |
| `ReHomeController.java` | **Add** `onCancel(int itemId, String studentName)` and **change** `run()` to handle a new menu option. |
| `ConsoleView.java` | **Change** `readMenuChoice()` to display the option; reuse `readItemId()`, `readStudentName()`, `showMessage()`, and `showError()`. |
| `Item.java`, `ItemSnapshot.java`, `ReHomeApp.java`, `ModelDemo.java` | **No changes expected** because the new behavior should fit the existing model/controller/view boundary. |

**New tests:** `LinkedQueueTest` will cover removing the only node, head, middle, tail, missing name, and an empty queue. `ReHomeModelTest` will cover case-insensitive cancellation, front deadline reset, unchanged deadline for middle/tail removals, invalid IDs, and claimed items. `ReHomeControllerTest` will cover the new menu path and errors. `ItemSnapshotTest` should not change.

**Stretch goals:** We attempted the **five-minute timer** in this prototype and will attempt **waitlist cancellation** in Deliverable 4. We are dropping no-show pickup tracking, wish-list matching, and pickup scheduling from the Deliverable 4 plan.

**Who does what:** **Liam** will implement linked-queue removal and queue tests. **Sajjad** will implement model cancellation/validation, model tests, and integration. **Luc** will add the controller action and controller tests and check timer behavior. **Tayshaun** will update the menu/input handling and test bad-input recovery.

## Attribution

| Class or test file | Team member(s) and contribution |
| --- | --- |
| `LinkedQueue.java` | **Liam:** linked FIFO operations and head/tail design. **Sajjad:** FIFO behavior and its use in ReHome. |
| `Node<T>` (nested in `LinkedQueue.java`) | **Liam:** node/link design and head/tail edge cases. **Sajjad:** last-node removal scenario. |
| `Item.java` | **Sajjad:** item fields, claim state, waitlist, and deadline design. |
| `ItemSnapshot.java` | **Sajjad:** snapshot API and defensive-copy design. |
| `ReHomeModel.java` | **Sajjad:** model method design, validation, expiration, and stale-offer rules. **Tayshaun:** bad-input and edge-case review. |
| `ConsoleView.java` | **Tayshaun:** menu/input design and error-message behavior. |
| `ReHomeController.java` | **Luc:** controller actions and timer scheduling design. **Sajjad:** model/controller boundary and stale-offer behavior. |
| `ReHomeApp.java` | **Luc:** startup wiring and integration plan. |
| `ModelDemo.java` | **Sajjad:** standalone-model and fixed-time demonstration cases. **Luc:** integration flow. |
| `LinkedQueueTest.java` | **Liam:** queue edge-case tests. **Sajjad:** empty, one-node, and multi-node test planning. |
| `ReHomeModelTest.java` | **Sajjad:** model, deadline, and stale-claim test design. **Tayshaun:** invalid-input cases. |
| `ItemSnapshotTest.java` | **Sajjad:** getter and defensive-copy test design. |
| `ReHomeControllerTest.java` | **Luc:** controller interaction scenarios. **Tayshaun:** input-recovery cases. **Sajjad:** integration/timeout test planning. |

### GenAI Note

We used ChatGPT to help organize our original scope and specification, clarify the responsibilities of our model, controller, and view, and think through queue edge cases and tests. ChatGPT also helped draft the first version of the Java code, Javadocs, and JUnit 5 tests. We used those drafts alongside our team's design decisions.

We relied on ChatGPT **a decent amount for the five-minute timer stretch goal**. We understood the behavior we wanted—when the first student does not respond, their turn expires and the next student moves up—but we had not learned how to implement an automatic timer while a Java console waits for input. The timer required concepts such as `Timer`, `TimerTask`, millisecond deadlines, and synchronized model methods, including code beyond what we had covered in CS 2114 so far. ChatGPT helped us understand how to keep the timer in the controller while leaving expiration decisions in the model.

We also asked ChatGPT which classes and exact methods the planned cancellation feature would affect; that prediction appears in **Next Steps**. Finally, we used ChatGPT to make this README more concise, readable, and organized so that our reflection, future work, and attribution clearly fit together.
