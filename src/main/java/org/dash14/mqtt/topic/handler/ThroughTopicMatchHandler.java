package org.dash14.mqtt.topic.handler;

import java.util.Objects;

import org.dash14.mqtt.topic.TopicHandler;

public class ThroughTopicMatchHandler<Data> implements TopicMatchHandler<Data> {
    private TopicHandler<Data> _handler;

    public ThroughTopicMatchHandler(TopicHandler<Data> handler) {
        _handler = handler;
    }

    @Override
    public TopicHandler<Data> getTopicHandler() {
        return _handler;
    }

    @Override
    public boolean match(String topic, String fullTopic) {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        @SuppressWarnings("unchecked")
        final ThroughTopicMatchHandler<Data> other = (ThroughTopicMatchHandler<Data>) obj;
        return Objects.equals(this._handler, other._handler);
    }

    @Override
    public int hashCode() {
        return _handler.hashCode();
    }

}
