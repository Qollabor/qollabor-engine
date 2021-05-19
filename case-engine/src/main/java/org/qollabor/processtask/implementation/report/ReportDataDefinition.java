package org.qollabor.processtask.implementation.report;

import org.qollabor.cmmn.definition.CMMNElementDefinition;
import org.qollabor.cmmn.definition.ModelDefinition;
import org.qollabor.akka.actor.serialization.json.Value;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ReportDataDefinition extends CMMNElementDefinition {
    private final String name;

    public ReportDataDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement);
        this.name = parseAttribute("name", false);
    }

    /**
     * Sets the report data
     */
    public InputStream createDataStream(PDFReport report) {
        // ReportData is taken from an input parameter with the name 'reportData', or, alternatively,
        //  from an <reportData name="reference-to-data-parameter"> tag inside the definition.

        if (this.name.isEmpty()) {
            // Just return an empty data stream; apparently data is not needed for this report.
            return EMPTY_STREAM;
        }

        if (! report.getInputParameters().has(name)) {
            throw new MissingParameterException("Report data '"+name+"' cannot be found in the task input parameters");
        }

        // Take the parameter value, flatten it to string and then return it as a stream. Can probably be done
        //  more efficiently...
        Value<?> jsonData = report.getInputParameters().get(name);
        return new ByteArrayInputStream(jsonData.toString().getBytes(StandardCharsets.UTF_8));
    }

    static InputStream EMPTY_STREAM = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
}
