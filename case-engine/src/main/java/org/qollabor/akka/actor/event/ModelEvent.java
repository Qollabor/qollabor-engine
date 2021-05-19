/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.akka.actor.event;

import org.qollabor.akka.actor.ModelActor;
import org.qollabor.akka.actor.TenantUserMessage;
import org.qollabor.akka.actor.serialization.QollaborSerializable;
import org.qollabor.akka.actor.serialization.json.ValueMap;

public interface ModelEvent<M extends ModelActor> extends QollaborSerializable, TenantUserMessage {
    String TAG = "qollabor";

    /**
     * Return true if, after the event has been added, and it's updateState method is invoked,
     * additional behavior must be executed (potentially leading to new events).
     * Such behavior is executed in two phases: first phase immediately after adding the event,
     * second phase only after immediate behaviors of newly generated events is also done.
     * Note that behavior is not executed upon recovery of an actor.
     */
    default boolean hasBehavior() { return false; }

    /**
     * Implement this to run behavior immediately after event is created and updateState is invoked.
     */
    default void runImmediateBehavior() {}

    /**
     * Behavior to run immediately after event is created and updateState is invoked.
     */
    void runDelayedBehavior();

    void updateState(M actor);

    void recover(M actor);

    String getTenant();

    default String tenant() {
        return getTenant();
    }

    String getActorId();

    String getDescription();

    ValueMap rawJson();
}
