package org.qollabor.akka.actor.command.exception;

import org.qollabor.cmmn.akka.command.CaseCommand;
import org.qollabor.cmmn.instance.Case;

/**
 * This exception is typically raised during the {@link CaseCommand#process(Case)} method.
 * The case instance checks for this exception around its invocation of the process method.
 *
 */
public class CommandException extends RuntimeException {
    public CommandException(String msg) {
        super(msg);
    }
    
    public CommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
