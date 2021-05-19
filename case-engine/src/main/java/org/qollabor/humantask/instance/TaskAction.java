/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.humantask.instance;

public enum TaskAction {
    Null("null"),
    Create("create"),
    Claim("claim"),
    Assign("assign"),
    Delegate("delegate"),
    Revoke("revoke"),
    Suspend("suspend"),
    Resume("resume"),
    Complete("complete"),
    Terminate("terminate");

    private final String value;

    TaskAction(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }

    public static TaskAction getEnum(String value) {
        if (value == null) return null;
        for (TaskAction action : values())
            if (action.getValue().equalsIgnoreCase(value)) return action;
        return null;
    }
}
