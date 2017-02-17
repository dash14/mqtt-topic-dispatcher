package org.dash14.mqtt.topic.handler;

import org.dash14.mqtt.topic.TopicHandler;

public interface TopicMatchHandler<Data> {
    boolean match(String topic, String fullTopic);
    TopicHandler<Data> getTopicHandler();
}
