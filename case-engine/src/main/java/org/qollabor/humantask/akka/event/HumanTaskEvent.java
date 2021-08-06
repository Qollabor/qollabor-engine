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
import org.qollabor.cmmn.akka.event.plan.task.TaskEvent;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.instance.task.humantask.HumanTask;

import java.io.IOException;

public abstract class HumanTaskEvent extends TaskEvent<HumanTask> {
    public static final String TAG = "qollabor:task";

    public final String taskId; // taskName is same as the planItem id
    private final String taskName; // taskName is same as the planItemName

    /**
     * Constructor used by HumanTaskCreated event, since at that moment the task name is not yet known
     * inside the task actor.
     * @param task
     */
    protected HumanTaskEvent(HumanTask task) {
        super(task);
        this.taskName = task.getName();
        this.taskId = task.getId();
    }

    protected HumanTaskEvent(ValueMap json) {
        super(json);
        this.taskName = readField(json, Fields.taskName);
        this.taskId = readField(json, Fields.taskId);
    }

    protected void writeHumanTaskEvent(JsonGenerator generator) throws IOException {
        super.writeModelEvent(generator);
        writeField(generator, Fields.taskName, taskName);
        writeField(generator, Fields.taskId, taskId);
    }

    /**
     * Get the task id
     * @return id of the task
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Get the name of the task
     * @return
     */
    public String getTaskName() {
        return taskName;
    }
}
