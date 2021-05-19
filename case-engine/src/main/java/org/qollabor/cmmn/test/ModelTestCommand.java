package org.qollabor.cmmn.test;

import org.qollabor.akka.actor.command.ModelCommand;
import org.qollabor.akka.actor.command.response.CommandFailure;
import org.qollabor.akka.actor.command.response.ModelResponse;
import org.qollabor.cmmn.test.assertions.PublishedEventsAssertion;

public interface ModelTestCommand<C extends ModelCommand, R extends ModelResponse> {
    int getActionNumber();

    String getActorId();

    C getActualCommand();

    <RESPONSE extends R> RESPONSE getActualResponse();

    /**
     * Returns the list of events published for this test command, as published since by the actor id
     * @return
     */
    default PublishedEventsAssertion<?> getEvents() {
        return getEventListener().getNewEvents().filter(getActorId());
    }

    CommandFailure getActualFailure();

    void handleResponse(R response);

    void handleFailure(CommandFailure failure);

    CaseEventListener getEventListener();

    default String getCommandDescription() {
        return getActualCommand().getCommandDescription();
    }

    /**
     * Temporary hack to have some case printing after all for now.
     * @return
     */
    @Deprecated
    default String caseInstanceString() {
        return "No case available";
    }
}
