/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.instance.parameter;

import org.qollabor.cmmn.definition.parameter.ParameterDefinition;
import org.qollabor.cmmn.definition.parameter.TaskOutputParameterDefinition;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.cmmn.instance.Parameter;
import org.qollabor.akka.actor.serialization.json.Value;

/**
 * A TaskOutputParameter is created right before a task completes.
 * If its value is set (after it is mapped from the raw output of the task), it is bound to the case file.
 */
public class TaskOutputParameter extends Parameter<ParameterDefinition> {
    public TaskOutputParameter(ParameterDefinition definition, Case caseInstance, Value value) {
        super(definition, caseInstance, value);
    }

    @Override
    public TaskOutputParameterDefinition getDefinition() {
        return (TaskOutputParameterDefinition) super.getDefinition();
    }

    /**
     * Binding to case file must not be done upon task output validation, only upon task completion.
     */
    public void bind() {
        super.bindParameterToCaseFile();
    }
}
