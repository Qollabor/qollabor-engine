/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.humantask.akka.command;

import org.qollabor.akka.actor.command.response.CommandFailure;
import org.qollabor.akka.actor.command.response.ModelResponse;
import org.qollabor.akka.actor.identity.TenantUser;
import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.instance.task.validation.ValidationResponse;
import org.qollabor.humantask.akka.command.response.HumanTaskValidationResponse;
import org.qollabor.humantask.instance.WorkflowTask;

/**
 * Saves the output in the task. This output is not yet stored back in the case file, since that happens only when the task is completed.
 */
@Manifest
public class ValidateTaskOutput extends TaskOutputCommand {
	public ValidateTaskOutput(TenantUser tenantUser, String caseInstanceId, String taskId, ValueMap taskOutput) {
		super(tenantUser, caseInstanceId, taskId, taskOutput);
	}

	public ValidateTaskOutput(ValueMap json) {
		super(json);
	}

	@Override
	public ModelResponse process(WorkflowTask workflowTask) {
		ValidationResponse response = workflowTask.getTask().validateOutput(taskOutput);
		if (response.isValid()) {
			return new HumanTaskValidationResponse(this, response.getContent());
		} else {
			// HTD
//        	return null;
			return new CommandFailure(this, response.getException());
		}
	}
}
