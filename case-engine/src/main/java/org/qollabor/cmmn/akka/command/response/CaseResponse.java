/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.akka.command.response;

import org.qollabor.akka.actor.command.response.CommandFailure;
import org.qollabor.akka.actor.command.response.ModelResponse;
import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.cmmn.akka.command.CaseCommand;
import org.qollabor.cmmn.akka.command.GetDiscretionaryItems;
import org.qollabor.akka.actor.serialization.json.ValueMap;

/**
 * If the case instance has handled an {@link CaseCommand}, it will return a CaseResponse to the sender of the command. This can be used to communicate back
 * e.g. a http message code 200 to a web client.
 * A command response usually contains little content. However, there are some exceptions:
 * <ul>
 * <li>The command actually returns a value that cannot be derived from the event stream, e.g. the list of discretionary items, see {@link GetDiscretionaryItems}</li>
 * <li>The command was erroneous and an exception needs to be returned, see {@link CommandFailure}</li>
 * </ul>
 */
@Manifest
public class CaseResponse extends ModelResponse {
    public CaseResponse(CaseCommand command) {
        super(command);
    }

    public CaseResponse(ValueMap json) {
        super(json);
    }

    @Override
    public String toString() {
        return "CaseResponse for "+getActorId()+": last modified is "+getLastModified();
    }
}
