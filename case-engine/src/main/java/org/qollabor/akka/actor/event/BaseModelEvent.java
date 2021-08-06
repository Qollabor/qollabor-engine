package org.qollabor.akka.actor.event;

import com.fasterxml.jackson.core.JsonGenerator;
import org.qollabor.akka.actor.ModelActor;
import org.qollabor.akka.actor.identity.TenantUser;
import org.qollabor.akka.actor.serialization.Fields;
import org.qollabor.akka.actor.serialization.json.ValueMap;

import java.io.IOException;
import java.time.Instant;

public abstract class BaseModelEvent<M extends ModelActor> implements ModelEvent<M> {
    private final ValueMap json;

    // Serializable fields
    private final String actorId;
    public final String tenant;
    private final TenantUser tenantUser;
    private final Instant timestamp;

    /**
     * During recovery, Actor is set in call to {@link ModelEvent#recover(ModelActor)}
     * So during {@link ModelEvent#updateState(ModelActor)} it can be used.
     */
    protected transient M actor;

    protected BaseModelEvent(M actor) {
        this.json = new ValueMap();
        this.actorId = actor.getId();
        this.tenant = actor.getTenant();
        this.tenantUser = actor.getCurrentUser();
        this.actor = actor;
        this.timestamp = actor.getTransactionTimestamp();
    }

    protected BaseModelEvent(ValueMap json) {
        this.json = json;
        ValueMap modelEventJson = json.with(Fields.modelEvent);
        this.actorId = readField(modelEventJson, Fields.actorId);
        this.tenant = readField(modelEventJson, Fields.tenant);
        this.timestamp = readInstant(modelEventJson, Fields.timestamp);
        this.tenantUser = TenantUser.from(modelEventJson.with(Fields.user));
    }

    @Override
    public String getTenant() {
        return tenant;
    }

    /**
     * Returns the raw json used to (de)serialize this event
     * This method cannot be invoked upon first event creation.
     *
     * @return
     */
    public final ValueMap rawJson() {
        return this.json;
    }

    /**
     * Returns the identifier of the ModelActor that generated this event.
     * Is the same as the persistence id of the underlying Akka Actor.
     *
     * @return
     */
    public final String getActorId() {
        return this.actorId;
    }

    /**
     * Returns the complete context of the user that caused the event to happen
     *
     * @return
     */
    public final TenantUser getUser() {
        return tenantUser;
    }

    /**
     * UpdateState will be invoked when an event is added or recovered.
     * @param actor
     */
    public abstract void updateState(M actor);

    /**
     * Events can implement behavior to be run after the event has updated it's state.
     * This method will only be invoked when the model actor is not running in recovery mode.
     * This method will be invoked immediately after the event has updated it's state.
     * ImmediateBehavior in itself may also generate new events. If behavior must be executed
     * after the immediate behavior of those new events is generated, then the method
     * runDelayedBehavior can be implemented with it.
     */
    public void runImmediateBehavior() {
        // Default behavior is none
    }

    /**
     * Override this method and return true if the ModelEvent subclass has
     * particular implementations for {@link ModelEvent#runImmediateBehavior()} or {@link ModelEvent#runDelayedBehavior()}
     *
     * @return
     */
    public boolean hasBehavior() {
        return false;
    }

    /**
     * Events can implement behavior to be run after the event has updated it's state.
     * This method will only be invoked when the model actor is not running in recovery mode.
     * DelayedBehavior is executed after any events generated during immediate behavior have executed
     * their immediate behavior
     */
    public void runDelayedBehavior() {
        // Default behavior is none
    }

    /**
     * Internal framework method
     * @param actor
     */
    public final void recover(M actor) {
        this.actor = actor;
        this.updateState(actor);
    }

    protected void writeModelEvent(JsonGenerator generator) throws IOException {
        generator.writeFieldName(Fields.modelEvent.toString());
        generator.writeStartObject();
        writeField(generator, Fields.actorId, this.getActorId());
        writeField(generator, Fields.tenant, this.tenant);
        writeField(generator, Fields.timestamp, this.timestamp);
        generator.writeFieldName(Fields.user.toString());
        tenantUser.write(generator);
        generator.writeEndObject();
    }

    /**
     * Override this method to make a description of the event that is based on it's content.
     * This method is invoked from toString().
     * @return
     */
    public String getDescription() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
