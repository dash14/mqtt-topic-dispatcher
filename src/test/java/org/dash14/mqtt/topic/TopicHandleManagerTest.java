package org.dash14.mqtt.topic;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dash14.mqtt.topic.TopicHandleManager;
import org.dash14.mqtt.topic.TopicHandler;
import org.junit.Test;

public class TopicHandleManagerTest {

    // Exact match (doesn't have wildcard)
    @Test
    public void testHandleTopicExactly() {
        TopicHandleManager<String> manager = new TopicHandleManager<>();
        final List<String> test = new ArrayList<>();

        // '/' only
        {
            subscribe("/", manager, (topic, message) -> {
                test.add(message + "-1");
            });
            assertPublished("/", "A", manager);
            assertNotPublished("/a", "B", manager);
            assertEquals(test, Arrays.asList("A-1"));
        }

        manager = new TopicHandleManager<>();
        test.clear();

        // starts with '/'
        {
            subscribe("/abc", manager, (topic, message) -> {
                test.add(message + "-2");
            });
            assertPublished("/abc", "A", manager);
            assertNotPublished("abc", "B", manager);
            assertEquals(test, Arrays.asList("A-2"));
        }

        manager = new TopicHandleManager<>();
        test.clear();

        // ends with '/'
        {
            subscribe("abc/", manager, (topic, message) -> {
                test.add(message + "-3");
            });
            assertPublished("abc/", "A", manager);
            assertNotPublished("abc", "B", manager);
            assertEquals(test, Arrays.asList("A-3"));
        }

        manager = new TopicHandleManager<>();
        test.clear();

        // a series of '/'
        {
            subscribe("abc/abc", manager, (topic, message) -> {
                test.add(message + "-4");
            });
            assertPublished("abc/abc", "A", manager);
            assertNotPublished("abc//abc", "B", manager);
            assertEquals(test, Arrays.asList("A-4"));
            test.clear();

            subscribe("abc//abc", manager, (topic, message) -> {
                test.add(message + "-5");
            });
            assertPublished("abc//abc", "A", manager);
            assertPublished("abc/abc", "B", manager);
            assertEquals(test, Arrays.asList("A-5", "B-4"));
        }

        manager = new TopicHandleManager<>();
        test.clear();

        // exact match; 2 levels
        {
            subscribe("/abc/def", manager, (topic, message) -> {
                test.add(message + "-2");
            });
            test.add("one");
            assertPublished("/abc/def", "two", manager);
            assertNotPublished("/abc", "three", manager);
            assertEquals(test, Arrays.asList("one", "two-2"));
        }

        test.clear();

        // exact match; 3 levels
        {
            subscribe("/abc/def/ghi", manager, (topic, message) -> {
                test.add(message + "-4");
            });
            test.add("three");
            assertPublished("/abc/def/ghi", "four", manager);
            assertEquals(test, Arrays.asList("three", "four-4"));
        }

        test.clear();

        // ends with '/' with multi levels
        {
            subscribe("/abc/def/", manager, (topic, message) -> {
                test.add(message + "-5");
            });
            assertPublished("/abc/def/", "five", manager);
            assertEquals(test, Arrays.asList("five-5"));
        }

        test.clear();

        // Multi handlers with a same topic.
        {
            test.add("A-1");
            subscribe("/abc/def", manager, (topic, message) -> {
                test.add(message + "-3");
            });
            assertPublished("/abc/def", "B", manager);
            assertEquals(test, Arrays.asList("A-1", "B-2", "B-3"));
        }

        // #subscribe() with empty topic
        try {
            manager.addTopicHandler("", (topic, message) -> {});
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expected.getMessage(), "invalid topic format: (empty)");
        }
    }

    // Pattern match (wildcard '#')
    @Test
    public void testHandleTopicSharpWilcard() {
        TopicHandleManager<String> manager = new TopicHandleManager<>();
        final List<String> test = new ArrayList<>();

        // '#' only
        {
            subscribe("#", manager, (topic, message) -> {
                test.add(message + "-s");
            });
            test.add("A");
            assertPublished("/",    "B", manager);
            assertPublished("/abc", "C", manager);
            assertPublished("abc",  "D", manager);
            assertEquals(test, Arrays.asList("A", "B-s", "C-s", "D-s"));
        }

        manager = new TopicHandleManager<>();
        test.clear();

        // /#
        {
            subscribe("/#", manager, (topic, message) -> {
                test.add(message + "-as");
            });
            test.add("A");
            assertPublished("/",    "B", manager);
            assertPublished("/abc", "C", manager);
            assertNotPublished("abc", "D", manager);
            assertEquals(test, Arrays.asList("A", "B-as", "C-as"));
        }

        manager = new TopicHandleManager<>();
        test.clear();

        // /abc/#
        {
            subscribe("/abc/#", manager, (topic, message) -> {
                test.add(message + "-s1");
            });
            test.add("A");
            assertPublished("/abc",  "B", manager);
            assertPublished("/abc/", "C", manager);
            assertPublished("/abc/def", "D", manager);
            assertNotPublished("/ab/def", "E", manager);
            assertNotPublished("abc/def", "F", manager);
            assertEquals(test, Arrays.asList("A", "B-s1", "C-s1", "D-s1"));
        }

        // abnormally: starts with '#'
        try {
            manager.addTopicHandler("#/abc", (topic, message) -> {});
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expected.getMessage(), "invalid topic format: #/abc");
        }

        // abnormally: characters after '#'
        try {
            manager.addTopicHandler("/abc/#/abc", (topic, message) -> {});
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expected.getMessage(), "invalid topic format: /abc/#/abc");
        }

        // abnormally: '##'
        try {
            manager.addTopicHandler("/abc/##", (topic, message) -> {});
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expected.getMessage(), "invalid topic format: /abc/##");
        }
    }

    // Pattern match (wildcard '+')
    @Test
    public void testHandleTopicPlusWilcard() {
        TopicHandleManager<String> manager = new TopicHandleManager<>();
        final List<String> test = new ArrayList<>();

        // starts with '+'
        {
            subscribe("+/abc", manager, (topic, message) -> {
                test.add(message + "-p1");
            });
            assertPublished("def/abc", "A", manager);
            assertNotPublished("def/abc/ghi", "B", manager);
            assertNotPublished("/def/abc",    "C", manager);
            assertEquals(test, Arrays.asList("A-p1"));
        }

        manager = new TopicHandleManager<>();
        test.clear();

        // one '+'
        {
            subscribe("abc/+/", manager, (topic, message) -> {
                test.add(message + "-p2");
            });
            assertNotPublished("abc/abc", "A", manager);
            assertPublished("abc/abc/", "B", manager);
            assertPublished("abc//",    "C", manager);
            assertEquals(test, Arrays.asList("B-p2", "C-p2"));
        }

        manager = new TopicHandleManager<>();
        test.clear();

        // multiple '+'
        {
            subscribe("abc/+/def/+", manager, (topic, message) -> {
                test.add(message + "-p3");
            });
            assertPublished("abc/aaa/def/bbb", "A", manager);
            assertNotPublished("abc/aaa/def/bbb/", "B", manager);
            assertNotPublished("abc/aaa/def", "C", manager);
            assertNotPublished("abc/aaa/",    "D", manager);
            assertEquals(test, Arrays.asList("A-p3"));
        }

        manager = new TopicHandleManager<>();
        test.clear();

        // abc/+
        {
            subscribe("abc/+", manager, (topic, message) -> {
                test.add(message + "-p4");
            });
            assertNotPublished("abc", "A", manager);
            assertPublished("abc/",    "B", manager);
            assertPublished("abc/abc", "C", manager);
            assertNotPublished("abc/abc/abc", "D", manager);
            assertEquals(test, Arrays.asList("B-p4", "C-p4"));
        }

        manager = new TopicHandleManager<>();
        test.clear();

        // '+' only
        {
            subscribe("+", manager, (topic, message) -> {
                test.add(message + "-p5");
            });
            assertNotPublished("abc/abc", "A", manager);
            assertNotPublished("abc/", "B", manager);
            assertNotPublished("/abc", "C", manager);
            assertPublished("abc", "D", manager);
            assertEquals(test, Arrays.asList("D-p5"));
        }

        manager = new TopicHandleManager<>();
        test.clear();

        // abnormally: '++'
        try {
            manager.addTopicHandler("/abc/++/def", (topic, message) -> {});
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expected.getMessage(), "invalid topic format: /abc/++/def");
        }

        // abnormally: /abc+
        try {
            manager.addTopicHandler("/abc+", (topic, message) -> {});
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(expected.getMessage(), "invalid topic format: /abc+");
        }
    }

    @Test
    public void testRemoveTopicHandler() {
        TopicHandleManager<String> manager = new TopicHandleManager<>();
        final List<String> test = new ArrayList<>();

        TopicHandler<String> h1 = (topic, message) -> {
            test.add(message + "-r1");
        };

        TopicHandler<String> h2 = (topic, message) -> {
            test.add(message + "-r2");
        };

        // /
        {
            subscribe("/", manager, h1);
            assertPublished("/", "A", manager);

            boolean result = manager.removeTopicHandler("/", h1);
            assertTrue(result);

            assertNotPublished("/", "B", manager);
            assertEquals(test, Arrays.asList("A-r1"));
        }
        test.clear();

        // /abc/def
        {
            subscribe("/abc/def", manager, h1);
            assertPublished("/abc/def", "A2", manager);

            boolean result = manager.removeTopicHandler("/abc/def", h1);
            assertTrue(result);

            assertNotPublished("/abc/def", "B2", manager);
            assertEquals(test, Arrays.asList("A2-r1"));
        }
        test.clear();

        // #
        {
            subscribe("#", manager, h1);
            assertPublished("#", "C", manager);

            boolean result = manager.removeTopicHandler("#", h1);
            assertTrue(result);

            assertNotPublished("#", "D", manager);
            assertEquals(test, Arrays.asList("C-r1"));
        }
        test.clear();

        // /#
        {
            subscribe("/#", manager, h1);
            assertPublished("/#", "E", manager);

            boolean result = manager.removeTopicHandler("/#", h1);
            assertTrue(result);

            assertNotPublished("/#", "F", manager);
            assertEquals(test, Arrays.asList("E-r1"));
        }
        test.clear();

        // /abc/+/def
        {
            subscribe("/abc/+/def", manager, h1);
            assertPublished("/abc/aaa/def", "F", manager);

            boolean result = manager.removeTopicHandler("/abc/+/def", h1);
            assertTrue(result);

            assertNotPublished("/abc/aaa/def", "G", manager);
            assertEquals(test, Arrays.asList("F-r1"));
        }
        test.clear();

        // Remove multiple handlers
        {
            subscribe("/abc/#", manager, h1);
            assertPublished("/abc/def", "H", manager);

            subscribe("/abc/+/def", manager, h2);
            assertPublished("/abc/aaa/def", "I", manager);

            assertEquals(test, Arrays.asList("H-r1", "I-r1", "I-r2"));
            test.clear();

            boolean result = manager.removeTopicHandler("/abc/+/def", h2);
            assertTrue(result);

            assertPublished("/abc/aaa/def", "J", manager);
            assertEquals(test, Arrays.asList("J-r1"));

            result = manager.removeTopicHandler("/abc/#", h1);
            assertTrue(result);

            assertNotPublished("/abc/aaa/def", "K", manager);
        }
        test.clear();

        // Remove multiple handlers 2
        {
            subscribe("/#", manager, h1);
            assertPublished("/abc/def", "I", manager);

            subscribe("+", manager, h2);
            assertPublished("abc", "J", manager);

            assertEquals(test, Arrays.asList("I-r1", "J-r2"));
            test.clear();

            boolean result = manager.removeTopicHandler("/#", h1);
            assertTrue(result);

            assertNotPublished("/abc/def", "I", manager);
            assertPublished("abc", "J", manager);
            assertEquals(test, Arrays.asList("J-r2"));

            result = manager.removeTopicHandler("+", h2);
            assertTrue(result);

            assertNotPublished("/abc/def", "I", manager);
            assertNotPublished("abc", "J", manager);
        }

        // Attempt to remove non-registered topic.
        {
            subscribe("/#", manager, h1);
            assertPublished("/abc/def", "I", manager);

            // invalid topic-filter
            boolean result = manager.removeTopicHandler("/test/#", h1);
            assertFalse(result);

            TopicHandler<String> h3 = (topic, message) -> {
                test.add(message + "-r1");
            };

            // invalid handler
            result = manager.removeTopicHandler("/#", h3);
            assertFalse(result);
        }
    }

    private static void subscribe(String topic, TopicHandleManager<String> m, TopicHandler<String> h) {
        m.addTopicHandler(topic, h);
    }

    private static void assertPublished(String topic, String message, TopicHandleManager<String> m) {
        boolean result = m.handleTopic(topic, message);
        assertTrue(result);
    }

    private static void assertNotPublished(String topic, String message, TopicHandleManager<String> m) {
        boolean result = m.handleTopic(topic, message);
        assertFalse(result);
    }
}
