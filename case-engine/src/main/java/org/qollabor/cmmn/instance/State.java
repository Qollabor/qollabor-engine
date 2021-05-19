/* 
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.instance;

/**
 * Enum describing possible states plan items and case file items
 * Note, case file item can only be in Null, Available and Discarded, whereas plan item cannot be in Discarded, as per the spec.
 * Nevertheless merged here for convenience.
 */
public enum State {
    Null,
    Active,
    Available,
    Closed,
    Completed,
    Disabled,
    Discarded, // Special case for case file item
    Enabled,
    Failed,
    Suspended,
    Terminated;

    boolean isSemiTerminal() {
        return this == Closed || this == Completed || this == Disabled || this == Failed || this == Terminated;
    }
}
