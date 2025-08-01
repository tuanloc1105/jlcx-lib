package examples;

import io.vertx.core.Future;
import io.vertx.core.Completable;
import io.vertx.core.Handler;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.common.ServiceName;
import io.vertx.grpc.common.ServiceMethod;
import io.vertx.grpc.common.GrpcMessageDecoder;
import io.vertx.grpc.common.GrpcMessageEncoder;

/**
 * <p>A client for invoking the Greeter gRPC service.</p>
 */
public interface GreeterClient extends Greeter {

  /**
   * Calls the SayHello RPC service method.
   *
   * @param request the examples.HelloRequest request message
   * @return a future of the examples.HelloReply response message
   */
  Future<examples.HelloReply> sayHello(examples.HelloRequest request);
}
