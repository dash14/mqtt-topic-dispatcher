package org.dash14.mqtt.topic;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.dash14.mqtt.topic.handler.ThroughTopicMatchHandler;
import org.dash14.mqtt.topic.handler.TopicMatchHandler;
import org.dash14.mqtt.topic.handler.TopicPatternMatchHandler;
import org.dash14.mqtt.topic.handler.TopicPrefixMatchHandler;

/**
 * A manager for message handling for MQTT-subscribe topic filter.
 */
public class TopicHandleManager<Data> {

    /** Handlers for exact match */
    private ListMultimap<String, TopicHandler<Data>> _exactMatchHanders;

    /** Handlers for pattern(wildcard) match */
    private HierarchicallyTopicMatcher<Data> _hierarchicallyMatcher;

    /** Pattern for a invalid topic */
    private static final Pattern INVALID_TOPIC_PATTERN = Pattern.compile("(#.+|[^/]\\+|\\+[^/])");

    /** Constructor */
    public TopicHandleManager() {
        _exactMatchHanders = ArrayListMultimap.create(16, 2);
        _hierarchicallyMatcher = new HierarchicallyTopicMatcher<>();
    }

    /**
     * Add topic-filter and handler pair.
     * @param topicFilter A topic-filter; It can be used wildcards ('+', '#')
     * @param handler A handler for {@link #handleTopic(String, Data)} called with specified topic-filter
     */
    public synchronized void addTopicHandler(@Nonnull String topicFilter, @Nonnull TopicHandler<Data> handler) {
        topicFilter = Objects.requireNonNull(topicFilter);
        handler = Objects.requireNonNull(handler);

        validateTopicFilter(topicFilter);

        if (topicFilter.indexOf('#') < 0 && topicFilter.indexOf('+') < 0) {
            // not has wildcard
            _exactMatchHanders.put(topicFilter, handler);
        } else {
            // has wildcard
            updateHierarchicallyMatchers(topicFilter, _hierarchicallyMatcher, handler);
        }
    }

    /**
     * Remove topic-filter and handler pair.
     * @param topicFilter A registered topic-filter
     * @param handler A registered handler
     * @return {@code true} if removed, {@code false} otherwise
     */
    public synchronized boolean removeTopicHandler(@Nonnull String topicFilter, @Nonnull TopicHandler<Data> handler) {
        topicFilter = Objects.requireNonNull(topicFilter);
        handler = Objects.requireNonNull(handler);

        validateTopicFilter(topicFilter);

        boolean removed = false;
        if (topicFilter.indexOf('#') < 0 && topicFilter.indexOf('+') < 0) {
            // not has wildcard
            removed = _exactMatchHanders.remove(topicFilter, handler);
        } else {
            // has wildcard
            HierarchicallyTopicMatcher<Data> matchers = new HierarchicallyTopicMatcher<>();
            updateHierarchicallyMatchers(topicFilter, matchers, handler);

            // subtract: _hierarchicallyMatcher - matchers
            removed = _hierarchicallyMatcher.subtract(matchers);
        }

        return removed;
    }

    /**
     * Call handlers which is matched topic.
     * @param topic A topic
     * @param data A message data passing to handlers
     * @return {@code true} if called least one handler, {@code false} if not called handlers
     */
    public synchronized boolean handleTopic(@Nullable String topic, @Nullable Data data) {
        if (Strings.isNullOrEmpty(topic)) {
            return false;
        }

        boolean handled = false;

        // exactly match
        for (TopicHandler<Data> handler : _exactMatchHanders.get(topic)) {
            handler.handleTopic(topic, data);
            handled = true;
        }

        // wildcard match
        handled |= _hierarchicallyMatcher.matchAndHandleHierarchically(topic, topic, data);

        return handled;
    }

    private synchronized void updateHierarchicallyMatchers(String topicFilter,
            HierarchicallyTopicMatcher<Data> currentMatcher, TopicHandler<Data> handler) {
        /* subscribe: /abc/def/ghi/#
         * -> /abc -> /def -> /ghi : through
         */
        /* subscribe: /abc/+/def
         * -> /abc -> / : pattern
         */
        /* subscribe: abc/def/+
         * -> abc -> /def -> /
         */

        String[] parts = topicFilter.split("/", -1 /* 値のない部分も省略しない */);

        outside:
        for (int i = 0; i < parts.length; i++) {
            if (i == 0 && "".equals(parts[i])) continue;
            switch (parts[i]) {
            case "#": // 以降すべてマッチ
                // 最上階層の場合、/は必須、最上以外の場合は/は任意
                if (i == 1 && "".equals(parts[0])) {
                    currentMatcher.appendHandler(newTopicPrefixMatchHandler("/", handler));
                } else {
                    currentMatcher.appendHandler(newThroughTopicMatchHandler(handler));
                }
                break outside; // switchの外側のループを抜ける
            case "+": // 以降はパターンマッチ
                StringJoiner joiner = new StringJoiner("/", (i > 0) ? "^/" : "^", "$");
                Arrays.stream(parts, i, parts.length)
                      .map(s -> {
                          switch (s) {
                          case "#": return ".*";
                          case "+": return "[^/]*";
                          case "" : return "";
                          default : return Pattern.quote(s);
                          }
                      }).forEach(joiner::add);
                Pattern pattern = Pattern.compile(joiner.toString());
                currentMatcher.appendHandler(newTopicPatternMatchHandler(pattern, handler));
                break outside;
            default:
                String part = (i > 0 ? "/" : "") + parts[i];
                currentMatcher = currentMatcher.findOrCreateChild(part);
                // 必ず # or + が指定されたトピックが渡されるため、階層化するだけでいい
                break;
            }
        }
    }

    private static void validateTopicFilter(String topicFilter) {
        if ("".equals(topicFilter)) {
            throw new IllegalArgumentException("invalid topic format: (empty)");
        }
        Matcher m = INVALID_TOPIC_PATTERN.matcher(topicFilter);
        if (m.find()) {
            throw new IllegalArgumentException("invalid topic format: " + topicFilter);
        }
    }

    /* package */ TopicMatchHandler<Data> newThroughTopicMatchHandler(TopicHandler<Data> handler) {
        return new ThroughTopicMatchHandler<>(handler);
    }

    /* package */ TopicMatchHandler<Data> newTopicPrefixMatchHandler(String prefix, TopicHandler<Data> handler) {
        return new TopicPrefixMatchHandler<>(prefix, handler);
    }

    /* package */ TopicMatchHandler<Data> newTopicPatternMatchHandler(Pattern pattern, TopicHandler<Data> handler) {
        return new TopicPatternMatchHandler<>(pattern, handler);
    }
}
