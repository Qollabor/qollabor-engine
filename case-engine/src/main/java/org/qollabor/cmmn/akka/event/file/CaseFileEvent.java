/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.akka.event.file;

import com.fasterxml.jackson.core.JsonGenerator;
import org.qollabor.akka.actor.serialization.Fields;
import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.cmmn.akka.event.CaseEvent;
import org.qollabor.cmmn.instance.*;
import org.qollabor.akka.actor.serialization.json.Value;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.instance.casefile.*;
import org.qollabor.cmmn.instance.sentry.StandardEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Event caused by a transition on a CaseFileItem
 */
@Manifest
public class CaseFileEvent extends CaseEvent implements StandardEvent<CaseFileItemTransition> {
    protected final static Logger logger = LoggerFactory.getLogger(CaseFileEvent.class);

    private final CaseFileItemTransition transition;
    private final Value<?> value;
    protected final Path path;
    private final State state;

    public CaseFileEvent(CaseFileItemCollection<?> item, State newState, CaseFileItemTransition transition, Value<?> newValue) {
        super(item.getCaseInstance());
        this.transition = transition;
        this.value = newValue;
        this.path = item.getPath();
        this.state = newState;
    }

    public CaseFileEvent(ValueMap json) {
        super(json);
        this.transition = json.getEnum(Fields.transition, CaseFileItemTransition.class);
        this.value = json.get(Fields.value.toString());
        this.path = readPath(json, Fields.path);
        this.state = readEnum(json, Fields.state, State.class);
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public String getDescription() {
        return this.getClass().getSimpleName() + "['" + path + "']." + getTransition().toString().toLowerCase() + "() ===> " + getState();
    }

    /**
     * Returns the transition that the case file item went through.
     *
     * @return
     */
    public CaseFileItemTransition getTransition() {
        return transition;
    }

    /**
     * Returns the state of the case file item
     * @return
     */
    public State getState() {
        return state;
    }

    /**
     * Returns the index of the case file item within it's parent (or -1 if it is not an iterable case file item)
     * @return
     */
    public int getIndex() {
        return path.getIndex();
    }

    /**
     * Returns the new value of the case file item.
     *
     * @return
     */
    public Value<?> getValue() {
        return value;
    }

    /**
     * Return the case file item's path through which the change was made (e.g., Order/Line)
     *
     * @return
     */
    public Path getPath() {
        return path;
    }

    protected transient CaseFileItem caseFileItem;

    @Override
    public void updateState(Case caseInstance) {
        try {
            // Resolve the path on the case file
            caseFileItem = path.resolve(caseInstance);
            caseFileItem.updateState(this);
        } catch (InvalidPathException shouldNotHappen) {
            logger.error("Could not recover path on case instance?!", shouldNotHappen);
        }
    }

    @Override
    public boolean hasBehavior() {
        return true;
    }

    @Override
    public void runImmediateBehavior() {
        caseFileItem.informConnectedEntryCriteria(this);
    }

    @Override
    public void runDelayedBehavior() {
        caseFileItem.informConnectedExitCriteria(this);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeCaseInstanceEvent(generator);
        writeField(generator, Fields.transition, transition);
        writeField(generator, Fields.path, path);
        writeField(generator, Fields.value, value);
        writeField(generator, Fields.state, state);
    }
}
