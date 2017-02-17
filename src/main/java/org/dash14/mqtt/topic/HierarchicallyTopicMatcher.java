package org.dash14.mqtt.topic;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.dash14.mqtt.topic.handler.TopicMatchHandler;

/**
 * A class that is responsible for matching one hierarchy.
 *
 * <p>
 * A topic filter is managed by different objects for each hierarchy delimited by '/'.
 * Since matching specifications will change depending on the presence of '/',
 * delimiters are kept at the beginning of each hierarchy.
 * </p>
 * @param <Data> Message object type passing to handlers
 */
@NotThreadSafe
/* package */ class HierarchicallyTopicMatcher<Data> {

    private Set<TopicMatchHandler<Data>> _matchers = new LinkedHashSet<>();

    private Map<String, HierarchicallyTopicMatcher<Data>> _children = new HashMap<>();

    /* package */
    boolean matchHierarchically(String topic, String fullTopic, List<TopicHandler<Data>> matchedHandlers) {
        boolean matched = false;

        // Match current hierarchy
        for (TopicMatchHandler<Data> matcher : _matchers) {
            if (matcher.match(topic, fullTopic)) {
                matched = true;
                matchedHandlers.add(matcher.getTopicHandler());
            }
        }

        if ("".equals(topic)) {
            return matched;
        }

        // Match next hierarchy
        String nextLevelTopic;
        int nextLevelPos;
        if (topic.charAt(0) == '/') {
            if ("/".equals(topic)) return matched;
            nextLevelPos = topic.indexOf('/', 1);
        } else {
            nextLevelPos = topic.indexOf('/');
        }
        if (nextLevelPos < 0) {
            nextLevelTopic = topic;
            topic = "";
        } else {
            nextLevelTopic = topic.substring(0, nextLevelPos);
            topic = topic.substring(nextLevelPos);
        }

        HierarchicallyTopicMatcher<Data> m = _children.get(nextLevelTopic);
        if (m != null) {
            matched |= m.matchHierarchically(topic, fullTopic, matchedHandlers);
        }
        return matched;
    }

    /* package */
    HierarchicallyTopicMatcher<Data> findOrCreateChild(String partialTopic) {
        HierarchicallyTopicMatcher<Data> matcher = _children.get(partialTopic);
        if (matcher == null) {
            matcher = new HierarchicallyTopicMatcher<>();
            _children.put(partialTopic, matcher);
        }
        return matcher;
    }

    /* package */
    void appendHandler(TopicMatchHandler<Data> handler) {
        _matchers.add(handler);
    }

    /* package */
    boolean subtract(HierarchicallyTopicMatcher<Data> matchers) {
        boolean changed = false;

        changed = _matchers.removeAll(matchers._matchers);

        for (String partialTopic : matchers._children.keySet()) {
            HierarchicallyTopicMatcher<Data> org = _children.get(partialTopic);
            if (org != null) {
                HierarchicallyTopicMatcher<Data> htm = matchers._children.get(partialTopic);
                changed |= org.subtract(htm);
                if (org._matchers.isEmpty() && org._children.isEmpty()) {
                    _children.remove(partialTopic);
                }
            }
        }

        return changed;
    }
}
