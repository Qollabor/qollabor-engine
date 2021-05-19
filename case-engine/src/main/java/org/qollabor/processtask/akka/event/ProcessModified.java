package org.qollabor.processtask.akka.event;

import com.fasterxml.jackson.core.JsonGenerator;
import org.qollabor.akka.actor.event.TransactionEvent;
import org.qollabor.akka.actor.serialization.Fields;
import org.qollabor.akka.actor.serialization.Manifest;
import org.qollabor.akka.actor.serialization.json.ValueMap;
import org.qollabor.processtask.instance.ProcessTaskActor;

import java.io.IOException;
import java.time.Instant;

/**
 * Event that is published after an {@link org.qollabor.processtask.akka.command.ProcessCommand} has been fully handled by a {@link ProcessTaskActor} instance.
 * Contains information about the last modified moment.
 *
 */
@Manifest
public class ProcessModified extends ProcessInstanceEvent implements TransactionEvent<ProcessTaskActor> {
    private final Instant lastModified;

    public ProcessModified(ProcessTaskActor actor, Instant lastModified) {
        super(actor);
        this.lastModified = lastModified;
    }

    public ProcessModified(ValueMap value) {
        super(value);
        this.lastModified = value.rawInstant(Fields.lastModified);
    }

    @Override
    public void updateState(ProcessTaskActor actor) {
        actor.setLastModified(lastModified);
    }

    /**
     * Returns the moment at which the case was last modified
     * @return
     */
    public Instant lastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return "Modified process task [" + getActorId() + "] at " + lastModified;
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeModelEvent(generator);
        writeField(generator, Fields.lastModified, lastModified);
    }
}
