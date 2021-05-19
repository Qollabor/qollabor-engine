/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.instance;

import org.qollabor.cmmn.definition.casefile.CaseFileItemDefinition;
import org.qollabor.cmmn.definition.parameter.ParameterDefinition;
import org.qollabor.akka.actor.serialization.json.Value;
import org.qollabor.cmmn.instance.casefile.CaseFileItem;

import java.io.Serializable;

public class Parameter<T extends ParameterDefinition> extends CMMNElement<T> implements Serializable {
    private Value<?> value; // Default value is Null.
    private final String name;

    protected Parameter(T definition, Case caseInstance, Value value) {
        super(caseInstance, definition);
        this.name = definition.getName();
        this.value = value == null ? Value.NULL : value;
    }

    @Override
    public String toString() {
        return name + " : " + value;
    }

    /**
     * Returns the current value of the parameter. If the parameter is bound to a case file item, the value of the case file item is used.
     * Otherwise, the value is stored and retrieved from within the parameter itself.
     * @return
     */
    public Value<?> getValue() {
        return value;
    }

    /**
     * Binds the value of the parameter to the case file, if a binding is defined
     */
    protected void bindCaseFileToParameter() {
        if (getDefinition().getBinding() == null) { // No binding means no need to bind the case file to the value;
            return;
        }

        CaseFileItemDefinition cfid = getDefinition().getBinding();
        CaseFileItem item = cfid.getPath().resolve(getCaseInstance());//.getCaseFile().getItem(cfid.getPath());
        
        // Note: we're navigating to the CURRENT case file item. That is, for array type of case file item,
        //  this will lead to the item that is most recently modified.
        value = item.getCurrent().getValue();
    }

    /**
     * Binds the case file to the parameter value, if a binding is available
     */
    protected void bindParameterToCaseFile() {
        // Now do the binding to the case file, if it is defined
        CaseFileItemDefinition cfid = getDefinition().getBinding();
        if (cfid != null) {
            CaseFileItem item = cfid.getPath().resolve(getCaseInstance());
            item.bindParameter(this, value);
        }
    }
}
