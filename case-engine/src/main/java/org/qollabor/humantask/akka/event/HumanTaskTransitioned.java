/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.humantask.akka.event;

import com.fasterxml.jackson.core.JsonGenerator;
import org.qollabor.akka.actor.serialization.Fields;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.instance.task.humantask.HumanTask;
import org.qollabor.humantask.instance.TaskAction;
import org.qollabor.humantask.instance.TaskState;

import java.io.IOException;

public abstract class HumanTaskTransitioned extends HumanTaskEvent {
    private final TaskState currentState; // current taskState [Unassigned, Assigned or Delegated]
    private final TaskState historyState; // previous taskState [Unassigned, Assigned or Delegated]
    private final TaskAction transition; // last action happened on the task

    protected HumanTaskTransitioned(HumanTask task, TaskState currentState, TaskState historyState, TaskAction transition) {
        super(task);
        this.currentState = currentState;
        this.historyState = historyState;
        this.transition = transition;
    }

    protected HumanTaskTransitioned(HumanTask task, TaskState currentState, TaskAction transition) {
        this(task, currentState, task.getImplementation().getCurrentState(), transition);
    }

    protected HumanTaskTransitioned(ValueMap json) {
        super(json);
        this.currentState = json.getEnum(Fields.currentState, TaskState.class);
        this.historyState = json.getEnum(Fields.historyState, TaskState.class);
        this.transition = json.getEnum(Fields.transition, TaskAction.class);
    }

    public void writeTransitionEvent(JsonGenerator generator) throws IOException {
        super.writeHumanTaskEvent(generator);
        writeField(generator, Fields.historyState, historyState);
        writeField(generator, Fields.transition, transition);
        writeField(generator, Fields.currentState, currentState);
    }

    @Override
    public String toString() {
        return "Task " + getTaskName() + "[" + getTaskId() + "]." + getTransition() + ", causing transition from " + getHistoryState() + " to " + getCurrentState();
    }


    @Override
    public void updateState(Case caseInstance) {
        getTask().getImplementation().updateState(this);
    }

    /**
     * Get the current task state
     *
     * @return current task state
     */
    public TaskState getCurrentState() {
        return currentState;
    }

    /**
     * Get the previous task state
     *
     * @return previous task state
     */
    public TaskState getHistoryState() {
        return historyState;
    }

    /**
     * Get the last action happened on task
     *
     * @return last action happened on task
     */
    public TaskAction getTransition() {
        return transition;
    }
}
