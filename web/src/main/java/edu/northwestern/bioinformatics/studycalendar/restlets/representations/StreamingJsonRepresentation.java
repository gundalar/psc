package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerationException;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Base class for potentially large JSON representations.
 *
 * @author Rhett Sutphin
 */
public abstract class StreamingJsonRepresentation extends OutputRepresentation {
    protected StreamingJsonRepresentation() {
        this(MediaType.APPLICATION_JSON);
    }

    public StreamingJsonRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    public void write(OutputStream out) throws IOException {
        JsonFactory jf = new JsonFactory();
        JsonGenerator gen = jf.createJsonGenerator(out, JsonEncoding.UTF8);
        generate(gen);
        gen.close();
    }

    public abstract void generate(JsonGenerator generator) throws IOException, JsonGenerationException;
}
