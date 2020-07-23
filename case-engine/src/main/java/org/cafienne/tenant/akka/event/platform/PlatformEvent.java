/*
 * Copyright 2014 - 2019 Cafienne B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cafienne.tenant.akka.event.platform;

import org.cafienne.akka.actor.serialization.json.ValueMap;
import org.cafienne.tenant.TenantActor;
import org.cafienne.tenant.akka.event.TenantEvent;

/**
 * Platform events are generated by platform owners that administer the tenants in {@link TenantActor}.
 */
public abstract class PlatformEvent extends TenantEvent {
    protected PlatformEvent(TenantActor tenant) {
        super(tenant);
    }

    protected PlatformEvent(ValueMap json) {
        super(json);
    }

    public String tenantName() {
        return getActorId();
    }
}
