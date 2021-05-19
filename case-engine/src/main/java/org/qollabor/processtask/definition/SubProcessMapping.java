/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.processtask.definition;

import org.qollabor.akka.actor.ModelActor;
import org.qollabor.cmmn.definition.ModelDefinition;
import org.qollabor.cmmn.definition.parameter.ParameterDefinition;
import org.qollabor.cmmn.definition.CMMNElementDefinition;
import org.qollabor.cmmn.definition.ExpressionDefinition;
import org.qollabor.cmmn.definition.ParameterMappingDefinition;
import org.qollabor.akka.actor.serialization.json.Value;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.w3c.dom.Element;

/**
 * This class represents the mapping between Process to its implementation.
 * It is similar to {@link ParameterMappingDefinition}, but it is proprietary for the engine Process implementations.
 * Currently it only supports mapping output parameters of the implementation to the Process.
 */
public class SubProcessMapping extends CMMNElementDefinition {
    private final String sourceRef;
    private final String targetRef;
    private final ExpressionDefinition transformation;
    private ParameterDefinition target;
    private ParameterDefinition source;
    
	public SubProcessMapping(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement);
        this.sourceRef = parseAttribute("sourceRef", true);
        this.targetRef = parseAttribute("targetRef", true);
        this.transformation = parse("transformation", ExpressionDefinition.class, false);
	}

	@Override
	protected void resolveReferences() {
		super.resolveReferences();

		target = getProcessDefinition().getOutputParameters().get(targetRef);
		if (target == null) {
			getModelDefinition().addReferenceError("Invalid mapping " + getId() + ": target parameter " + targetRef + " is missing");
		}
		
		SubProcessDefinition spd = getParentElement();
        source = spd.getRawOutputParameters().get(sourceRef);
		if (source == null) {
            getModelDefinition().addReferenceError("Invalid mapping in process definition " + spd.getParentElement().getId() + ": source parameter " + sourceRef + " cannot be used; use one of "+spd.getRawOutputParameterNames());
		}
	}

    @Override
    public String getContextDescription() {
        SubProcessDefinition spd = getParentElement();
        String parentId = spd.getParentElement().getId();
        return "The mapping in process with id '" + parentId + "'";
    }

    /**
     * This method to transform the raw output values of a SubProcess implementation to Process output parameters
     * @param caseInstance
     * @param rawOutputParameters
     * @return transformed output value
     */
    public Value<?> transformOutput(ModelActor caseInstance, ValueMap rawOutputParameters) {
        Value<?> targetValue = rawOutputParameters.get(sourceRef); // By default, the target value is the same as the source value
        if (transformation != null && !transformation.getBody().isEmpty()) {
            // But if there is a transformation defined, then we will evaluate the expression on the source value
            targetValue = transformation.getEvaluator().evaluateOutputParameterTransformation(caseInstance, targetValue, source, target, null);
        }
        return targetValue;
    }

    public ParameterDefinition getTarget() {
        return target;
    }

    public ParameterDefinition getSource() {
        return source;
    }
}
