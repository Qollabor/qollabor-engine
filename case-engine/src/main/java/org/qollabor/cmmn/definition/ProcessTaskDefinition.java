/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.definition;

import org.qollabor.cmmn.instance.Case;
import org.qollabor.cmmn.instance.Stage;
import org.qollabor.processtask.definition.ProcessDefinition;
import org.qollabor.cmmn.instance.task.process.ProcessTask;
import org.w3c.dom.Element;

public class ProcessTaskDefinition extends TaskDefinition<ProcessDefinition> {
    private final String processRef;
    private ProcessDefinition processDefinition;

    public ProcessTaskDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement);
        this.processRef = parseAttribute("processRef", true);
    }

    @Override
    protected void resolveReferences() {
        super.resolveReferences();
        this.processDefinition = getCaseDefinition().getDefinitionsDocument().getProcessDefinition(this.processRef);
        if (this.processDefinition == null) {
            getModelDefinition().addReferenceError("The process task '" + this.getName() + "' refers to a process named " + processRef + ", but that definition is not found");
            return; // Avoid further checking on this element
        }
    }

    @Override
    public ProcessTask createInstance(String id, int index, ItemDefinition itemDefinition, Stage stage, Case caseInstance) {
        return new ProcessTask(id, index, itemDefinition, this, stage);
    }

	@Override
	public ProcessDefinition getImplementationDefinition() {
		return processDefinition;
	}
}
