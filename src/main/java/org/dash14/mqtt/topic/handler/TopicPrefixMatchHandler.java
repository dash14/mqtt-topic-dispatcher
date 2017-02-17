package org.dash14.mqtt.topic.handler;

import java.util.Objects;

import org.dash14.mqtt.topic.TopicHandler;

public class TopicPrefixMatchHandler<Data> implements TopicMatchHandler<Data> {
    private TopicHandler<Data> _handler;
    private String _prefix;

    public TopicPrefixMatchHandler(String prefix, TopicHandler<Data> handler) {
        _prefix = prefix;
        _handler = handler;
    }

    @Override
    public TopicHandler<Data> getTopicHandler() {
        return _handler;
    }

    @Override
    public boolean match(String topic, String fullTopic) {
        return topic.startsWith(_prefix);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        @SuppressWarnings("unchecked")
        final TopicPrefixMatchHandler<Data> other = (TopicPrefixMatchHandler<Data>) obj;
        return Objects.equals(this._prefix, other._prefix)
                && Objects.equals(this._handler, other._handler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this._prefix, this._handler);
    }

}
