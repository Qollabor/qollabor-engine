package org.cafienne.humantask.akka.command.response;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.akka.actor.serialization.Fields;
import org.cafienne.akka.actor.serialization.Manifest;
import org.cafienne.akka.actor.serialization.json.ValueMap;
import org.cafienne.humantask.akka.command.WorkflowCommand;

import java.io.IOException;

@Manifest
public class HumanTaskValidationResponse extends HumanTaskResponse {
    private final ValueMap value;

    public HumanTaskValidationResponse(WorkflowCommand command, ValueMap value) {
        super(command);
        this.value = value;
    }

    public HumanTaskValidationResponse(ValueMap json) {
        super(json);
        this.value = readMap(json, Fields.value);
    }

    @Override
    public ValueMap getResponse() {
        return value;
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        writeField(generator, Fields.value, value);
    }
}
