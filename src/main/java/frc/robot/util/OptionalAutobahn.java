package frc.robot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import autobahn.client.AutobahnClient;
import autobahn.client.NamedCallback;

/*
 * This class is a wrapper around the AutobahnClient class that allows for the client to be optional.
 * This is useful for when the client is not yet connected to the network.
 * It also allows for the client to be set and replayed when the client is set.
 */
public class OptionalAutobahn extends AutobahnClient {
  private Optional<AutobahnClient> autobahnClient = Optional.empty();

  public OptionalAutobahn() {
    super(null); // set fake. we aren't actually using this instance
  }

  /**
   * Cached subscription / unsubscription operations to replay once a real client
   * is set.
   */
  private final List<CachedOperation> cachedOperations = new ArrayList<>();

  private enum OperationType {
    SUBSCRIBE_SINGLE,
    SUBSCRIBE_MULTI,
    UNSUBSCRIBE_TOPIC,
    UNSUBSCRIBE_CALLBACK
  }

  private static class CachedOperation {
    private final OperationType type;
    private final String topic;
    private final List<String> topics;
    private final NamedCallback callback;

    CachedOperation(OperationType type, String topic, NamedCallback callback) {
      this.type = type;
      this.topic = topic;
      this.callback = callback;
      this.topics = null;
    }

    CachedOperation(OperationType type, List<String> topics, NamedCallback callback) {
      this.type = type;
      this.topic = null;
      this.callback = callback;
      this.topics = new ArrayList<>(topics);
    }

    CachedOperation(OperationType type, String topic) {
      this.type = type;
      this.topic = topic;
      this.callback = null;
      this.topics = null;
    }
  }

  /** Returns the underlying real client, if one has been configured. */
  public Optional<AutobahnClient> getReal() {
    return autobahnClient;
  }

  @Override
  public boolean isConnected() {
    return autobahnClient.isPresent() && autobahnClient.get().isConnected();
  }

  /**
   * Sets the real Autobahn client and replays any cached subscription /
   * unsubscription
   * operations against it.
   */
  public void setAutobahnClient(AutobahnClient autobahnClient) {
    this.autobahnClient = Optional.of(autobahnClient);

    // Replay all cached operations in the order they were recorded.
    for (CachedOperation op : cachedOperations) {
      switch (op.type) {
        case SUBSCRIBE_SINGLE:
          this.autobahnClient.get().subscribe(op.topic, op.callback);
          break;
        case SUBSCRIBE_MULTI:
          this.autobahnClient.get().subscribe(op.topics, op.callback);
          break;
        case UNSUBSCRIBE_TOPIC:
          this.autobahnClient.get().unsubscribe(op.topic);
          break;
        case UNSUBSCRIBE_CALLBACK:
          this.autobahnClient.get().unsubscribe(op.topic, op.callback);
          break;
        default:
          break;
      }
    }

    cachedOperations.clear();
  }

  /**
   * Publishes a message. If the real client is not yet present, this is a no-op
   * and the
   * publish is dropped (nothing is cached or replayed later).
   */
  @Override
  public CompletableFuture<Void> publish(String topic, byte[] payload) {
    if (autobahnClient.isPresent()) {
      return autobahnClient.get().publish(topic, payload);
    }

    // Drop publishes when there is no real client yet.
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Subscribes to a topic. If the real client is not yet present, this call is
   * cached and
   * will be replayed when {@link #setAutobahnClient(AutobahnClient)} is called.
   */
  @Override
  public CompletableFuture<Void> subscribe(String topic, NamedCallback callback) {
    if (autobahnClient.isPresent()) {
      return autobahnClient.get().subscribe(topic, callback);
    }

    cachedOperations.add(new CachedOperation(OperationType.SUBSCRIBE_SINGLE, topic, callback));
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Subscribes to multiple topics with the same callback. If the real client is
   * not yet
   * present, this call is cached and will be replayed when the client is set.
   */
  @Override
  public CompletableFuture<Void> subscribe(List<String> topics, NamedCallback callback) {
    if (autobahnClient.isPresent()) {
      return autobahnClient.get().subscribe(topics, callback);
    }

    cachedOperations.add(new CachedOperation(OperationType.SUBSCRIBE_MULTI, topics, callback));
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Unsubscribes from a topic. If the real client is not yet present, this
   * operation is
   * cached and will be replayed when the client is set.
   */
  @Override
  public CompletableFuture<Void> unsubscribe(String topic) {
    if (autobahnClient.isPresent()) {
      return autobahnClient.get().unsubscribe(topic);
    }

    cachedOperations.add(new CachedOperation(OperationType.UNSUBSCRIBE_TOPIC, topic));
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Unsubscribes a specific callback from a topic. If the real client is not yet
   * present,
   * this operation is cached and will be replayed when the client is set.
   */
  @Override
  public CompletableFuture<Void> unsubscribe(String topic, NamedCallback callback) {
    if (autobahnClient.isPresent()) {
      return autobahnClient.get().unsubscribe(topic, callback);
    }

    cachedOperations.add(new CachedOperation(OperationType.UNSUBSCRIBE_CALLBACK, topic, callback));
    return CompletableFuture.completedFuture(null);
  }
}
