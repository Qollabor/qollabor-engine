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
import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.instance.task.humantask.HumanTask;

import java.io.IOException;

@Manifest
public class HumanTaskOutputSaved extends HumanTaskEvent {
	private final ValueMap taskOutput; // taskOutput - task saved output

	public HumanTaskOutputSaved(HumanTask task, ValueMap output) {
		super(task);
		this.taskOutput = output;
	}

	public HumanTaskOutputSaved(ValueMap json) {
		super(json);
		this.taskOutput = readMap(json, Fields.taskOutput);
	}

	public ValueMap getTaskOutput() {
		return taskOutput;
	}

	@Override
	public String toString() {
		return "HumanTask[" + getTaskId() + "] - Saved output - " + taskOutput;
	}

	@Override
	public void write(JsonGenerator generator) throws IOException {
		super.writeHumanTaskEvent(generator);
		writeField(generator, Fields.taskOutput, taskOutput);
	}
}
