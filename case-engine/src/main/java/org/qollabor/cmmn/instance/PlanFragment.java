/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.instance;

import org.qollabor.cmmn.definition.ItemDefinition;
import org.qollabor.cmmn.definition.PlanFragmentDefinition;

public abstract class PlanFragment<T extends PlanFragmentDefinition> extends PlanItem<T> {
    protected PlanFragment(String id, ItemDefinition itemDefinition, T definition, Case caseInstance, Stage parent, int index, StateMachine stateMachine) {
        super(id, index, itemDefinition, definition, caseInstance, parent, stateMachine);
    }
}
