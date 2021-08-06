/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.instance.sentry;

import org.qollabor.cmmn.definition.sentry.OnPartDefinition;
import org.qollabor.cmmn.instance.CMMNElement;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;

public abstract class OnPart<T extends OnPartDefinition, I extends CMMNElement<?>> extends CMMNElement<T> {
    protected final Criterion criterion;
    protected Collection<I> connectedItems = new ArrayList();

    protected OnPart(Criterion criterion, T definition) {
        super(criterion, definition);
        this.criterion = criterion;
    }

    public Criterion getCriterion() {
        return criterion;
    }

    abstract void connectToCase();

    abstract void releaseFromCase();

    public abstract void inform(I item, StandardEvent event);

    abstract ValueMap toJson();

    abstract Element dumpMemoryStateToXML(Element sentryXML, boolean showConnectedPlanItems);
}