package org.dash14.mqtt.topic;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.dash14.mqtt.topic.TopicDispatcher;
import org.dash14.mqtt.topic.TopicHandler;
import org.dash14.mqtt.topic.handler.TopicMatchHandler;
import org.junit.Test;

public class TopicMatchHandlerTest {

    @Test
    public void testThroughTopicMatchHandlerCompareTo() {
        TopicDispatcher<String> manager = new TopicDispatcher<>();
        TopicHandler<String> h1 = (topic, data) -> {};
        TopicHandler<String> h2 = (topic, data) -> {};

        TopicMatchHandler<String> mh1 = manager.newThroughTopicMatchHandler(h1);
        TopicMatchHandler<String> mh2 = manager.newThroughTopicMatchHandler(h1);
        TopicMatchHandler<String> mh3 = manager.newThroughTopicMatchHandler(h2);
        assertTrue(mh1.equals(mh2));
        assertFalse(mh1.equals(mh3));
    }

    @Test
    public void testTopicPrefixMatchHandlerCompareTo() {
        TopicDispatcher<String> manager = new TopicDispatcher<>();
        TopicHandler<String> h1 = (topic, data) -> {};
        TopicHandler<String> h2 = (topic, data) -> {};

        TopicMatchHandler<String> mh1 = manager.newTopicPrefixMatchHandler("", h1);
        TopicMatchHandler<String> mh2 = manager.newTopicPrefixMatchHandler("", h1);
        TopicMatchHandler<String> mh3 = manager.newTopicPrefixMatchHandler("/", h1);
        TopicMatchHandler<String> mh4 = manager.newTopicPrefixMatchHandler("/", h2);
        assertTrue(mh1.equals(mh2));
        assertFalse(mh1.equals(mh3));
        assertFalse(mh3.equals(mh4));
        assertFalse(mh1.equals(mh4));
    }

    @Test
    public void testTopicPatternMatchHandlerCompareTo() {
        TopicDispatcher<String> manager = new TopicDispatcher<>();
        TopicHandler<String> h1 = (topic, data) -> {};
        TopicHandler<String> h2 = (topic, data) -> {};

        TopicMatchHandler<String> mh1 = manager.newTopicPatternMatchHandler(Pattern.compile("abc"), h1);
        TopicMatchHandler<String> mh2 = manager.newTopicPatternMatchHandler(Pattern.compile("abc"), h1);
        TopicMatchHandler<String> mh3 = manager.newTopicPatternMatchHandler(Pattern.compile("def"), h1);
        TopicMatchHandler<String> mh4 = manager.newTopicPatternMatchHandler(Pattern.compile("def"), h2);
        assertTrue(mh1.equals(mh2));
        assertFalse(mh1.equals(mh3));
        assertFalse(mh3.equals(mh4));
        assertFalse(mh1.equals(mh4));
    }

    @Test
    public void testComplexHandlerCompareTo() {
        TopicDispatcher<String> manager = new TopicDispatcher<>();
        TopicHandler<String> h1 = (topic, data) -> {};

        TopicMatchHandler<String> mh1 = manager.newThroughTopicMatchHandler(h1);
        TopicMatchHandler<String> mh2 = manager.newTopicPrefixMatchHandler("abc", h1);
        TopicMatchHandler<String> mh3 = manager.newTopicPatternMatchHandler(Pattern.compile("def"), h1);
        assertFalse(mh1.equals(mh2));
        assertFalse(mh1.equals(mh3));
        assertFalse(mh2.equals(mh3));
    }
}
