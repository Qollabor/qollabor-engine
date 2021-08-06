/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.definition;

import org.qollabor.cmmn.definition.sentry.EntryCriterionDefinition;
import org.qollabor.cmmn.definition.sentry.ExitCriterionDefinition;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Interface to generalize across PlanItemDefinition and DiscretionaryItemDefinition (and CasePlanDefinition)
 */
public interface ItemDefinition {
    String getId();

    String getName();

    ItemControlDefinition getPlanItemControl();

    PlanItemDefinitionDefinition getPlanItemDefinition();

    default Collection<EntryCriterionDefinition> getEntryCriteria() {
        return new ArrayList();
    }

    Collection<ExitCriterionDefinition> getExitCriteria();

    default boolean isDiscretionary() {
        return false;
    }
}
