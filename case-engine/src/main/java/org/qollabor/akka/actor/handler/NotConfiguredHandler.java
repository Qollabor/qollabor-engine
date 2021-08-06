package org.qollabor.akka.actor.handler;

import org.qollabor.akka.actor.MessageHandler;
import org.qollabor.akka.actor.ModelActor;
import org.qollabor.akka.actor.TenantUserMessage;
import org.qollabor.akka.actor.command.ModelCommand;
import org.qollabor.akka.actor.command.response.CommandFailure;
import org.qollabor.akka.actor.event.ModelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for messages that are sent to a ModelActor before the ModelActor has received a {@link org.qollabor.akka.actor.command.BootstrapCommand}.
 * @param <C>
 * @param <E>
 * @param <A>
 */
public class NotConfiguredHandler<C extends ModelCommand, E extends ModelEvent, A extends ModelActor<C, E>> extends MessageHandler<Object, C, E, A> {
    private final static Logger logger = LoggerFactory.getLogger(NotConfiguredHandler.class);

    public NotConfiguredHandler(A actor, TenantUserMessage msg) {
        super(actor, msg, msg.getUser());
    }

    final protected void process() {
    }

    final protected void complete() {
        if (msg instanceof ModelCommand) {
            // Sent to the wrong type of actor
            ModelCommand invalidCommand = (ModelCommand) msg;
            // Still set the actor, so that it can create a proper failure response.
            invalidCommand.setActor(this.actor);
            Exception wrongCommandType = new Exception(this.actor.getClass().getSimpleName() + " cannot handle message '"+msg.getClass().getSimpleName()+"' because it has not been initialized properly");
            addDebugInfo(() -> wrongCommandType.getMessage(), logger);
            CommandFailure response = new CommandFailure((ModelCommand) msg, wrongCommandType);
            actor.reply(response);
        } else {
            logger.warn(actor.getClass().getSimpleName() + " " + actor.getId() + " received a message it cannot handle, of type " + msg.getClass().getName());
        }

        actor.persistEvents(events);
    }

}
