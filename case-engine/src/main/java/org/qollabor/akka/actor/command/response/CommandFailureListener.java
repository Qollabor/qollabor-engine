package org.qollabor.akka.actor.command.response;

import org.qollabor.cmmn.akka.command.CaseCommand;
import org.qollabor.cmmn.instance.Case;

/**
 * When sending a message to another model instance from within a model,
 * the method {@link Case#askCase(CaseCommand, CommandFailureListener, CommandResponseListener...)} can be used.
 * The case instance will respond to "this" case and this case will invoke the registered response listener.
 * This basically supports a simplified ask pattern between cases.
 *
 */
@FunctionalInterface
public interface CommandFailureListener {
    /**
     * The handleFailure method can be implemented to handle {@link CommandFailure} coming back as a result from sending a command to the other model that could not be handled.
     * @param failure
     */
    void handleFailure(CommandFailure failure);
}
