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
public class HumanTaskInputSaved extends HumanTaskEvent {
	private final ValueMap input;

	public HumanTaskInputSaved(HumanTask task, ValueMap input) {
		super(task);
		this.input = input;
	}

	public HumanTaskInputSaved(ValueMap json) {
		super(json);
		this.input = readMap(json, Fields.input);
	}

	public ValueMap getInput() {
		return input;
	}

	@Override
	public String toString() {
		return "HumanTask[" + getTaskId() + "] - Set input - " + input;
	}

	@Override
	public void write(JsonGenerator generator) throws IOException {
		super.writeHumanTaskEvent(generator);
		writeField(generator, Fields.input, input);
	}
}
