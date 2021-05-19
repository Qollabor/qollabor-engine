package org.qollabor.akka.actor.command.exception;

import org.qollabor.akka.actor.command.ModelCommand;
import org.qollabor.akka.actor.ModelActor;

/**
 * This exception is typically raised during the {@link ModelCommand#validate(ModelActor)} method.
 * The case instance checks for this exception around its invocation of the validate method.
 *
 */
public class InvalidCommandException extends CommandException {
    public InvalidCommandException(String msg) {
        super(msg);
    }
    
    public InvalidCommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
