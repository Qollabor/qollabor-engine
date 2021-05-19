/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.expression;

import org.qollabor.akka.actor.command.exception.CommandException;

public class InvalidExpressionException extends CommandException {
    public InvalidExpressionException(String message, Throwable t) {
        super(message, t);
    }

    public InvalidExpressionException(String message) {
        super(message);
    }
}
