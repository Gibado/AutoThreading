# AutoThreading
Multithreading can be confusing to add to a project.  This framework encapsulates that logic so you don't have to worry about it.

This is done by using 3 objects:

**A WorkUnit (WU):**
This is an object that knows how to accomplish a single unit of work. A WU can be dependent on zero to many other WU.  The WU will have one inherited method for accomplishing this work.
A WU will keep track of it's state, processing time, and have a reference to it's parent WU and any WUs it's dependent on
* State
    * READY
        * Represents a WU that is ready to begin it's work.  If it's dependent on other WUs then those WUs are in a DONE state.
    * IN_PROGRESS
        * Represents a WU that is currently performing work.
    * DONE
        * Represents a WU that has accomplished its work.  This is more for doing analytics on the work that was done or debugging.
    * WAITING
        * Represents a WU that is dependent on other WUs that are not yet in a DONE state.
    * ERROR
        * Represents a WU that had a problem while performing its work and could not complete the task.  If a dependent WU has reached an ERROR state then this WU will also move to an ERROR state.
* Process time
    * This is how fast this WU accomplished its task
* Parent WU
    * This is a reference to a WU that is dependent on this WU.  Once this WU has accomplished it's task then the parent WU will be notified in case it can begin its task.
* Children WU
    * This is a reference to a list of WU that need to complete before this WU can perform its task.


**A ProcessPlant:**
This is a singleton object that will take a WorkUnit, queue up the work, and return the results.  It will do this by searching the possible WU tree for leaf WUs (WU that don't depend on other WU) and process them 1st.


**A Sharable:**
Some objects need to be shared between WorkUnits.  This is a wrapper to protect those object from deadlock and collisions.

#How to Implement this Framework in your Code
* Extend the `WorkUnit` for simple tasks that are to be completed.
    * This will have the best results if you do this around tasks that can be done in parallel
    * Be sure to pass variables into your `WorkUnit` by using the `Sharable` wrapper object.
        * A `ReadOnlySharable` wrapper has also been included for passing in objects won't be altered while performing the task.
* After `WorkUnits` have been defined make sure they are linked together using `WorkUnit.setDependents()` where the dependents are `WorkUnits` that need to be completed before processing this `WorkUnit`.
    * I recommend thinking about the last `WorkUnit` to be processed as the *outcome `WorkUnit`* and assigning `WorkUnits` to it that are required to be completed beforehand.
        * Example: (Check in at work) *outcome `WorkUnit`* < (Drive to work) < (Get dressed)
* Now that your `WorkUnits` are organized correctly you can queue the *outcome `WorkUnit`* to be processed by the `ProcessPlant` via the `ProcessPlant.queueWorkUnit()`.