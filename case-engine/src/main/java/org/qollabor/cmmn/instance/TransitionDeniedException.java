/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.instance;

import org.qollabor.akka.actor.command.exception.CommandException;

public class TransitionDeniedException extends CommandException {
    public TransitionDeniedException(String msg) {
        super(msg);
    }
}
