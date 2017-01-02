# mqtt-topic-handler

Implementation for message handling (callback) for MQTT-subscribe topic-filter.

## Usage

A example for registering handlers, with topic-filters.

```java
// Instantiate TopicHandleManager. <Type> is message type.
TopicHandleManager<MqttMessage> topicManager = new TopicHandleManager<>();

topicManager.addTopicHandler("example/topic1", (topic, message) -> {
    // callback when received a MQTT message which topic is 'example/topic1'
    // ...
});

topicManager.addTopicHandler("example/test/#", (topic, message) -> {
    // callback when received a MQTT message which topic starts with 'example/test/'
    // ...
});

topicManager.addTopicHandler("example/+/name", (topic, message) -> {
    // callback when received a MQTT message which topic starts with 'example/' and ends with '/name'
    // ...
});
```

A example for message handling with paho-mqtt library.

```java
// ...

@Override
public void messageArrived(String topic, MqttMessage message) throws Exception {
    topicManager.handleTopic(topic, message); // Note: callback on same thread
}

// ...
```

## Contributing

1. Fork it ( http://github.com/dash14/mqtt-topic-handler/fork )
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## License

MIT License

