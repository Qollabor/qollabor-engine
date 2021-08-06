/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.instance;

import java.util.HashMap;
import java.util.Map;

import org.qollabor.akka.actor.command.exception.CommandException;
import org.qollabor.cmmn.akka.event.plan.PlanItemTransitioned;

/**
 * Simple state machine logic, with an indirection to figure out where we are and where we go
 */
class StateMachine {
    private final Map<State, Map<Transition, Target>> transitions = new HashMap();
    private final Map<State, Target> states = new HashMap();
    final Transition entryTransition;
    final Transition exitTransition;
    final Transition terminationTransition;

    private StateMachine(Transition entryTransition, Transition exitTransition, Transition terminationTransition) {
        this.entryTransition = entryTransition;
        this.exitTransition = exitTransition;
        this.terminationTransition = terminationTransition;
        // Register all states by default.
        for (State state : State.values()) {
            getTarget(state);
            getTransitions(state);
        }
    }

    /**
     * Configures a possible transition from a set of states to a target state.
     *  @param transition
     * @param targetState
     * @param fromStates
     */
    private void addTransition(Transition transition, State targetState, State... fromStates) {
        for (State fromState : fromStates) {
            Map<Transition, Target> stateTransitions = getTransitions(fromState);
            stateTransitions.put(transition, getTarget(targetState));
        }
    }

    /**
     * Configures the action that will be executed if an instance of type T enters the given state
     *
     * @param state
     * @param action
     */
    private void setAction(State state, Action action) {
        getTarget(state).action = action;
    }

    /**
     * Target state wrapper
     *
     * @param state
     * @return
     */
    private Target getTarget(State state) {
        Target target = states.get(state);
        if (target == null) {
            target = new Target(state);
            states.put(state, target);
        }
        return target;
    }

    private Map<Transition, Target> getTransitions(State state) {
        Map<Transition, Target> stateTransitions = transitions.get(state);
        if (stateTransitions == null) {
            stateTransitions = new HashMap();
            transitions.put(state, stateTransitions);
        }
        return stateTransitions;
    }

    /**
     * Make a transition on the instance.
     * Returns true if the transition resulted in a state change, false if the state remained the same.
     *
     * @param planItem
     * @param transition
     * @return
     */
    PlanItemTransitioned transition(PlanItem planItem, Transition transition) {
        State currentState = planItem.getState();

        Map<Transition, Target> stateTransitions = transitions.get(currentState);
        Target target = stateTransitions.get(transition);
        if (target != null) {
            if (target.state == null) {
                // If the target state is undefined, it means we to go back to the history state. Only instance knows it's history state.
                // We have to fetch the target again, based on the history state, as that will contain the proper action
                target = getTarget(planItem.getHistoryState());
            }
            // Evaluate the guard (if any)
            if (!planItem.isTransitionAllowed(transition)) {
                return null;
            }
            return new PlanItemTransitioned(planItem, target.state, currentState, transition);
        } else {
            return null; // no transition
        }
    }

    Action getAction(State state) {
        return getTarget(state).action;
    }

    private class Target {
        private final State state;
        private Action action;

        private Target(State targetState) {
            this.state = targetState;
            this.action = (PlanItem p, Transition t) -> {
            }; // By default an empty action.
        }
    }

    /**
     * Action to be executed when a plan item has entered a state
     */
    @FunctionalInterface
    interface Action // To be executed once a state is entered
    {
        /**
         * @param planItem The plan item that has made the state change
         * @param transition The transition that caused the state change
         */
        void execute(PlanItem planItem, Transition transition);
    }

    // State machine configuration for events and milestones
    static final StateMachine EventMilestone = new StateMachine(Transition.Occur, Transition.Exit, Transition.ParentTerminate);

    static {
        EventMilestone.addTransition(Transition.Create, State.Available, State.Null);
        EventMilestone.addTransition(Transition.Suspend, State.Suspended, State.Available);
        EventMilestone.addTransition(Transition.ParentSuspend, State.Suspended, State.Available);
        EventMilestone.addTransition(Transition.Terminate, State.Terminated, State.Available);
        EventMilestone.addTransition(Transition.Occur, State.Completed, State.Available);
        EventMilestone.addTransition(Transition.Resume, State.Available, State.Suspended);
        EventMilestone.addTransition(Transition.ParentResume, State.Available, State.Suspended);
        EventMilestone.addTransition(Transition.ParentTerminate, State.Terminated, new State[] { State.Available, State.Suspended });

        EventMilestone.setAction(State.Completed, (PlanItem p, Transition t) -> p.completeInstance());
        EventMilestone.setAction(State.Terminated, (PlanItem p, Transition t) -> p.terminateInstance());
        EventMilestone.setAction(State.Suspended, (PlanItem p, Transition t) -> p.suspendInstance());
        EventMilestone.setAction(State.Available, (PlanItem p, Transition t) -> {
            if (t == Transition.Create) {
                p.createInstance();
                if (p instanceof Milestone) {
                    p.evaluateRepetitionRule(true);
                    p.evaluateRequiredRule();
                    p.getEntryCriteria().beginLifeCycle(Transition.Occur);
                }
            } else if (t == Transition.Resume || t == Transition.ParentResume) {
                p.resumeInstance();
            }
        });
    }

    // State machine configuration for tasks and stages
    static final StateMachine TaskStage = new StateMachine(Transition.Start, Transition.Exit, Transition.Exit);

    static {
        TaskStage.addTransition(Transition.Create, State.Available, State.Null);
        TaskStage.addTransition(Transition.Enable, State.Enabled, State.Available);
        TaskStage.addTransition(Transition.Start, State.Active, State.Available);
        TaskStage.addTransition(Transition.Disable, State.Disabled, State.Enabled);
        TaskStage.addTransition(Transition.ManualStart, State.Active, State.Enabled);
        TaskStage.addTransition(Transition.Suspend, State.Suspended, State.Active);
        TaskStage.addTransition(Transition.Fault, State.Failed, State.Active);
        TaskStage.addTransition(Transition.Complete, State.Completed, State.Active);
        TaskStage.addTransition(Transition.Terminate, State.Terminated, State.Active);
        TaskStage.addTransition(Transition.Exit, State.Terminated, new State[] { State.Available, State.Active, State.Enabled, State.Disabled, State.Suspended, State.Failed });
        TaskStage.addTransition(Transition.Resume, State.Active, State.Suspended);
        TaskStage.addTransition(Transition.Reactivate, State.Active, State.Failed);
        TaskStage.addTransition(Transition.Reenable, State.Enabled, State.Disabled);
        TaskStage.addTransition(Transition.ParentSuspend, State.Suspended, new State[] { State.Available, State.Active, State.Enabled, State.Disabled });
        TaskStage.addTransition(Transition.ParentResume, null, State.Suspended);

        TaskStage.setAction(State.Available, (PlanItem p, Transition t) -> {
            p.createInstance();
            p.evaluateRepetitionRule(true);
            p.evaluateRequiredRule();

            // Now evaluate manual activation and trigger the associated transition on the plan item
            Transition transition = p.evaluateManualActivationRule();
            p.getEntryCriteria().beginLifeCycle(transition);
        });
        TaskStage.setAction(State.Active, (PlanItem p, Transition t) -> {
            if (t == Transition.Start || t == Transition.ManualStart) {
                p.startInstance();
            } else if (t == Transition.Resume || t == Transition.ParentResume) {
                p.resumeInstance();
            } else if (t == Transition.Reactivate) {
                p.reactivateInstance();
            } else {
                // Ignoring it...; but for now throw an exception to see if we ever run into this code.
                throw new CommandException("FIRST TIME EXCEPTION: I am an unexpected transition on this stage/task");
            }
        });
        TaskStage.setAction(State.Enabled, (PlanItem p, Transition t) -> p.makeTransition(Transition.Start));
        TaskStage.setAction(State.Suspended, (PlanItem p, Transition t) -> p.suspendInstance());
        TaskStage.setAction(State.Completed, (PlanItem p, Transition t) -> {
            p.completeInstance();
            if (p.getEntryCriteria().isEmpty()) {
                p.repeat();
            }
        });
        TaskStage.setAction(State.Terminated, (PlanItem p, Transition t) -> {
            p.terminateInstance();
            if (p.getEntryCriteria().isEmpty()) {
                p.repeat();
            }
        });
        TaskStage.setAction(State.Failed, (PlanItem p, Transition t) -> p.failInstance());
    }

    // State machine configuration for the case plan
    static final StateMachine CasePlan = new StateMachine(Transition.Start, Transition.Terminate, Transition.Exit);

    static {
        CasePlan.addTransition(Transition.Create, State.Active, State.Null);
        CasePlan.addTransition(Transition.Suspend, State.Suspended, State.Active);
        CasePlan.addTransition(Transition.Terminate, State.Terminated, State.Active);
        CasePlan.addTransition(Transition.Complete, State.Completed, State.Active);
        CasePlan.addTransition(Transition.Fault, State.Failed, State.Active);
        CasePlan.addTransition(Transition.Reactivate, State.Active, new State[] { State.Completed, State.Terminated, State.Failed, State.Suspended });
        CasePlan.addTransition(Transition.Close, State.Closed, new State[] { State.Completed, State.Terminated, State.Failed, State.Suspended });

        CasePlan.setAction(State.Suspended, (PlanItem p, Transition t) -> p.suspendInstance());
        CasePlan.setAction(State.Completed, (PlanItem p, Transition t) -> p.completeInstance());
        CasePlan.setAction(State.Terminated, (PlanItem p, Transition t) -> p.terminateInstance());
        CasePlan.setAction(State.Failed, (PlanItem p, Transition t) -> p.failInstance());
        CasePlan.setAction(State.Active, (PlanItem p, Transition t) -> {
            if (t == Transition.Create) {
                // Create plan items
                p.startInstance();
            } else {
                p.resumeInstance();
            }
        });
    }
}
