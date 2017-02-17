package org.dash14.mqtt.topic.handler;

import java.util.Objects;
import java.util.regex.Pattern;

import org.dash14.mqtt.topic.TopicHandler;

public class TopicPatternMatchHandler<Data> implements TopicMatchHandler<Data> {
    private TopicHandler<Data> _handler;
    private Pattern _pattern;

    public TopicPatternMatchHandler(Pattern pattern, TopicHandler<Data> handler) {
        _pattern = pattern;
        _handler = handler;
    }

    @Override
    public TopicHandler<Data> getTopicHandler() {
        return _handler;
    }

    @Override
    public boolean match(String topic, String fullTopic) {
        return _pattern.matcher(topic).find();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        @SuppressWarnings("unchecked")
        final TopicPatternMatchHandler<Data> other = (TopicPatternMatchHandler<Data>) obj;
        return Objects.equals(this._pattern.pattern(), other._pattern.pattern())
                && Objects.equals(this._handler, other._handler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this._pattern.pattern(), this._handler);
    }

}
