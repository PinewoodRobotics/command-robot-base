package frc.robot.util;

import autobahn.client.AutobahnClient;
import autobahn.client.rpc.AutobahnRPC;
import autobahn.client.rpc.client.ClientFunction;
import proto.pathfind.Pathfind.PathfindRequest;
import proto.pathfind.Pathfind.PathfindResult;

public class RPC {

    public interface RPCServices {
    }

    ///

    private static RPCServices services = null;
    private static boolean isAutobahnSet = false;

    public static void SetClient(AutobahnClient autobahnClient) {
        isAutobahnSet = true;
        AutobahnRPC.setAutobahnClient(autobahnClient);
    }

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
