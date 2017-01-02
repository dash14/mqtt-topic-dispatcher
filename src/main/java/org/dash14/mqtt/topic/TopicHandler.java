package org.dash14.mqtt.topic;

/**
 * Handler which calling when matched with topic filter.
 * @param <Data> Message object type passing to handlers
 */
public interface TopicHandler<Data> {
    void handleTopic(String topic, Data data);
}