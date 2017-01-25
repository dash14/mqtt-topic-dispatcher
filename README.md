# mqtt-topic-dispatcher

A implementation for dispatching message with topic-filter for a MQTT subscriber.

## Usage

Examples for registering handlers with topic-filters.

```java
// Instantiate TopicDispatcher. <Type> is message type.
TopicDispatcher<MqttMessage> topicDispatcher = new TopicDispatcher<>();

topicDispatcher.addHandler("example/topic1", (topic, message) -> {
    // callback when received a MQTT message which topic is 'example/topic1'
    // ...
});

topicDispatcher.addHandler("example/test/#", (topic, message) -> {
    // callback when received a MQTT message which topic starts with 'example/test/'
    // ...
});

topicDispatcher.addHandler("example/+/name", (topic, message) -> {
    // callback when received a MQTT message which topic starts with 'example/' and ends with '/name'
    // ...
});
```

A example for message dispatching using paho-mqtt library.

```java
// ...

@Override
public void messageArrived(String topic, MqttMessage message) throws Exception {
    topicDispatcher.dispatch(topic, message); // Note: callback on same thread
}

// ...
```

## Contributing

1. Fork it ( http://github.com/dash14/mqtt-topic-dispatcher/fork )
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## License

MIT License

