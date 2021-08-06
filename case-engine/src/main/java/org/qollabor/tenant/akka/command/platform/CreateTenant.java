package org.qollabor.tenant.akka.command.platform;

import com.fasterxml.jackson.core.JsonGenerator;
import org.qollabor.akka.actor.command.BootstrapCommand;
import org.qollabor.akka.actor.command.exception.InvalidCommandException;
import org.qollabor.akka.actor.identity.PlatformUser;
import org.qollabor.akka.actor.serialization.Fields;
import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.tenant.TenantActor;
import org.qollabor.tenant.akka.command.TenantUserInformation;
import org.qollabor.tenant.akka.command.exception.TenantException;
import org.qollabor.tenant.akka.command.response.TenantResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Manifest
public class CreateTenant extends PlatformTenantCommand implements BootstrapCommand {
    public final String name;
    private final List<TenantUserInformation> users;

    public CreateTenant(PlatformUser user, String tenantId, String name, List<TenantUserInformation> users) {
        super(user, tenantId);
        this.name = name;
        this.users = users;
        // Check whether after the filtering there are still owners left. Tenant must have owners.
        if (this.users.stream().filter(u -> u.isOwner() && u.isEnabled()).count() == 0) {
            throw new TenantException("Cannot create a tenant without providing tenant owners");
        }
    }

    public CreateTenant(ValueMap json) {
        super(json);
        this.name = readField(json, Fields.name);
        this.users = new ArrayList();
        json.withArray(Fields.users).forEach(user -> this.users.add(TenantUserInformation.from(user.asMap())));
    }

    @Override
    public String tenant() {
        return name;
    }

    @Override
    public void validate(TenantActor tenant) throws InvalidCommandException {
        super.validate(tenant);
        if (tenant.exists()) {
            throw new TenantException("Tenant already exists");
        }
    }

    @Override
    public TenantResponse process(TenantActor tenant) {
        tenant.createInstance(users);
        return new TenantResponse(this);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        writeField(generator, Fields.name, name);
        generator.writeArrayFieldStart(Fields.users.toString());
        for (TenantUserInformation user : users) {
            user.write(generator);
        }
        generator.writeEndArray();
    }
}

