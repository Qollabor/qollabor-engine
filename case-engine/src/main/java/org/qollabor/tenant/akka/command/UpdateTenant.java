package org.qollabor.tenant.akka.command;

import com.fasterxml.jackson.core.JsonGenerator;
import org.qollabor.akka.actor.command.exception.InvalidCommandException;
import org.qollabor.akka.actor.identity.TenantUser;
import org.qollabor.akka.actor.serialization.Fields;
import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.tenant.TenantActor;
import org.qollabor.tenant.akka.command.exception.TenantException;
import org.qollabor.tenant.akka.command.response.TenantResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Manifest
public class UpdateTenant extends TenantCommand {
    private final List<TenantUserInformation> users;

    public UpdateTenant(TenantUser tenantOwner, List<TenantUserInformation> users) {
        super(tenantOwner);
        this.users = users;
    }

    public UpdateTenant(ValueMap json) {
        super(json);
        this.users = new ArrayList();
        json.withArray(Fields.users).forEach(user -> this.users.add(TenantUserInformation.from(user.asMap())));
    }

    @Override
    public void validate(TenantActor tenant) throws InvalidCommandException {
        super.validate(tenant);
        // Check whether after the filtering there are still owners left. Tenant must have owners.
        Stream<String> currentOwners = tenant.getOwnerList().stream();
        List<String> userIdsThatWillBeUpdated = this.users.stream().map(user -> user.id()).collect(Collectors.toList());
        long untouchedOwners = currentOwners.filter(currentOwner -> !userIdsThatWillBeUpdated.contains(currentOwner)).count();
        if (untouchedOwners == 0 && this.users.stream().filter(u -> u.isOwner() && u.isEnabled()).count() == 0) {
            throw new TenantException("Cannot update the tenant and remove all tenant owners or disable their accounts");
        }
    }

    @Override
    public TenantResponse process(TenantActor tenant) {
        tenant.updateInstance(users);
        return new TenantResponse(this);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        generator.writeArrayFieldStart(Fields.users.toString());
        for (TenantUserInformation user : users) {
            user.write(generator);
        }
        generator.writeEndArray();
    }
}
