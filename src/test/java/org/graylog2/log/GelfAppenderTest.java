package org.graylog2.log;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.graylog2.GelfMessage;
import org.graylog2.GelfSender;
import org.junit.Test;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * (c) Copyright: Anton Yakimov
 */
public class GelfAppenderTest {

    @Test
    public void ensureHostnameForMessage() throws UnknownHostException, SocketException {

        final TestGelfSender gelfSender = new TestGelfSender("localhost");

        GelfAppender gelfAppender = new GelfAppender() {

            @Override
            public GelfSender getGelfSender() {
                return gelfSender;
            }

            @Override
            public void append(LoggingEvent event) {
                super.append(event);
            }
        };

        LoggingEvent event = new LoggingEvent("a.b.c.DasClass", Category.getInstance(this.getClass()), 123L, Priority.INFO, "Das Auto",
                                              new RuntimeException("LOL"));
        gelfAppender.append(event);

        assertThat("Message hostname", gelfSender.getLastMessage().getHost(), notNullValue());

        gelfAppender.setOriginHost("example.com");
        gelfAppender.append(event);
        assertThat(gelfSender.getLastMessage().getHost(), is("example.com"));
    }

    @Test
    public void checkExtendedInformation() throws UnknownHostException, SocketException {

        final TestGelfSender gelfSender = new TestGelfSender("localhost");

        GelfAppender gelfAppender = new GelfAppender() {

            @Override
            public GelfSender getGelfSender() {
                return gelfSender;
            }

            @Override
            public void append(LoggingEvent event) {
                super.append(event);
            }
        };
        gelfAppender.setAddExtendedInformation(true);

        LoggingEvent event = new LoggingEvent(this.getClass().getCanonicalName(), Category.getInstance(this.getClass()), 123L, Priority.INFO,
                                              "Das Auto", new RuntimeException("LOL"));

        gelfAppender.append(event);

        assertThat("Message logger", (String) gelfSender.getLastMessage().getAdditonalFields().get("logger"),
                   is(this.getClass().getCanonicalName()));
        // We can't really test the NDC without having proper log4j context.
        assertThat("Message NDC", (String) gelfSender.getLastMessage().getAdditonalFields().get("logger_ndc"), nullValue());

    }

    private class TestGelfSender extends GelfSender {

        private GelfMessage lastMessage;

        public TestGelfSender(String host) throws UnknownHostException, SocketException {
            super(host);
        }

        @Override
        public boolean sendMessage(GelfMessage message) {
            this.lastMessage = message;
            return true;
        }

        public GelfMessage getLastMessage() {
            return lastMessage;
        }
    }

}
