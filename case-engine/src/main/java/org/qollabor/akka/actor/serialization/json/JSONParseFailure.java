package org.qollabor.akka.actor.serialization.json;

/**
 * Exception thrown when a JSON parsing ran into a failure.
 * Either due to an IOException or due to an invalid token or empty content.
 */
public class JSONParseFailure extends Exception {
    JSONParseFailure(String msg) {
        super(msg);
    }
}
