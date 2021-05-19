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
import org.qollabor.akka.actor.serialization.json.Value;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.cmmn.instance.State;
import org.qollabor.cmmn.instance.casefile.*;

import java.io.IOException;

/**
 * Event caused by replacement of a CaseFileItem for children of that item that have to be removed
 */
@Manifest
public class CaseFileItemChildRemoved extends CaseFileEvent {
    private final Path childPath;

    public CaseFileItemChildRemoved(CaseFileItemCollection item, Path childPath) {
        super(item, State.Available, CaseFileItemTransition.RemoveChild, Value.NULL);
        this.childPath = childPath;
    }

    public CaseFileItemChildRemoved(ValueMap json) {
        super(json);
        this.childPath = readPath(json, Fields.childPath);
    }

    public Path getChildPath() {
        return childPath;
    }

    @Override
    public boolean hasBehavior() {
        return false;
    }

    @Override
    public void updateState(Case caseInstance) {
        try {
            // Resolve the path on the case file
            CaseFileItemCollection<?> caseFileItem = path.resolve(caseInstance);
            caseFileItem.updateState(this);
        } catch (InvalidPathException shouldNotHappen) {
            logger.error("Could not recover path on case instance?!", shouldNotHappen);
        }
    }

    @Override
    public String getDescription() {
        return this.getClass().getSimpleName() + "['" + path.getPart() + "']. child: [" + childPath + "]";
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        writeField(generator, Fields.childPath, childPath);
    }
}
