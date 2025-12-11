package frc.robot.util;

import autobahn.client.AutobahnClient;
import autobahn.client.rpc.AutobahnRPC;

/**
 * Thin wrapper around {@link AutobahnRPC} that exposes a single, lazily created
 * RPC service proxy for the robot code.
 * <p>
 * Usage:
 * <ol>
 * <li>Call {@link #SetClient(AutobahnClient)} once during robot startup to
 * provide the underlying Autobahn client.</li>
 * <li>Call {@link #Services()} anywhere else to obtain the strongly typed
 * RPC proxy and invoke remote procedures.</li>
 * </ol>
 *
 * This class enforces that a client is configured before use and hides the
 * details of how the Autobahn RPC proxy is constructed and cached.
 */
public class RPC {

    /** Cached instance of the RPC proxy returned by {@link AutobahnRPC}. */
    private static RPCServices services = null;

    /**
     * Tracks whether {@link #SetClient(AutobahnClient)} has been called so that we
     * can fail fast if code attempts to use RPC before configuration is complete.
     */
    private static boolean isAutobahnSet = false;

    /**
     * Marker/extension interface for all RPC endpoints exposed to robot code.
     * <p>
     * Methods defined on this interface (or its sub-interfaces) are turned into
     * remote procedures by {@link AutobahnRPC#createRPCClient(Class)}. The
     * {@link #Services()} method returns an implementation of this interface that
     * forwards calls over the Autobahn transport.
     */
    public interface RPCServices {
        // Define RPC methods here, only with protobuf defined messages.
        // void exampleCall(proto.ExampleMessage message);
        // proto.ExampleResponse exampleCall(proto.ExampleRequest request);
    }

    /**
     * Configures the underlying Autobahn client used for all RPC calls.
     * <p>
     * This MUST be called once during startup (before any call to
     * {@link #Services()}); typically from robot initialization code.
     *
     * @param autobahnClient already constructed Autobahn client instance that
     *                       knows how to talk to the remote server.
     */
    public static void SetClient(AutobahnClient autobahnClient) {
        isAutobahnSet = true;
        AutobahnRPC.setAutobahnClient(autobahnClient);
    }

    /**
     * Returns the singleton RPC proxy used by the robot to invoke remote
     * procedures.
     * <p>
     * The first call will lazily allocate the proxy using
     * {@link AutobahnRPC#createRPCClient(Class)}; subsequent calls reuse the same
     * instance.
     *
     * @return configured {@link RPCServices} proxy ready to make RPC calls.
     * @throws IllegalStateException if {@link #SetClient(AutobahnClient)} has not
     *                               been called yet.
     */
    public static RPCServices Services() {
        if (!isAutobahnSet) {
            throw new IllegalStateException(
                    "RPC services are not initialized. Please make sure to set the autobahn client first.");
        }

        if (services == null) {
            services = AutobahnRPC.createRPCClient(RPCServices.class);
        }

        return services;
    }
}
