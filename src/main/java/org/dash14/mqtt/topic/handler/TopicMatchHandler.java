package org.dash14.mqtt.topic.handler;

import org.dash14.mqtt.topic.TopicHandler;

public interface TopicMatchHandler<Data> {
    boolean matchAndHandle(String topic, String fullTopic, Data data);
    TopicHandler<Data> getTopicHandler();
}
