package org.qollabor.cmmn.akka.command.response;

import com.fasterxml.jackson.core.JsonGenerator;
import org.qollabor.akka.actor.serialization.Fields;
import org.qollabor.cmmn.akka.command.CaseCommand;
import org.qollabor.akka.actor.serialization.json.ValueMap;

import java.io.IOException;

public class CaseResponseWithValueMap extends CaseResponse {
    private final ValueMap value;

    protected CaseResponseWithValueMap(CaseCommand command, ValueMap value) {
        super(command);
        this.value = value;
    }

    protected CaseResponseWithValueMap(ValueMap json) {
        super(json);
        this.value = readMap(json, Fields.response);
    }

    /**
     * Returns a JSON representation of the discretionary items that are currently applicable in the case
     * @return
     */
    public ValueMap getResponse() {
        return value;
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        writeField(generator, Fields.response, value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getResponse();
    }
}
