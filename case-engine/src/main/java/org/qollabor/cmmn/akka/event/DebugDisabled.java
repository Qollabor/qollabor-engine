/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.akka.event;

import com.fasterxml.jackson.core.JsonGenerator;
import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.cmmn.instance.Case;
import org.qollabor.akka.actor.serialization.json.ValueMap;

import java.io.IOException;

/**
 * DebugEvent
 */
@Manifest
public class DebugDisabled extends CaseEvent {
    public DebugDisabled(Case caseInstance) {
        super(caseInstance);
    }

    public DebugDisabled(ValueMap json) {
        super(json);
    }

    @Override
    public void updateState(Case actor) {
        actor.setDebugMode(false);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeCaseInstanceEvent(generator);
    }
}
