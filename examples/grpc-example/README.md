# Vert.X gRPC Example

This project demonstrates a simple gRPC server and client implementation using Vert.X.

## Project Structure

- `grpc-server/` - Vert.X gRPC server implementation
- `grpc-client/` - Vert.X gRPC client implementation
- `proto/` - Protocol Buffer definition file

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

From the root directory:

```bash
mvn clean compile
```

## Running the Example

### 1. Start the gRPC Server

First, start the server:

```bash
cd grpc-server
mvn exec:java -Dexec.mainClass="com.example.App"
```

You should see output like:
```
GreeterVerticle deployed successfully
gRPC server is running on localhost:9090
You can now run the client to test the server
```

### 2. Run the gRPC Client

In a new terminal, run the client:

```bash
cd grpc-client
mvn exec:java -Dexec.mainClass="com.example.App"
```

You should see output like:
```
Starting gRPC client...
Will send requests to localhost:9090
Sending request for name: Alice
Received response: Hello Alice! Welcome to Vert.X gRPC server!
Sending request for name: Bob
Received response: Hello Bob! Welcome to Vert.X gRPC server!
Sending request for name: Charlie
Received response: Hello Charlie! Welcome to Vert.X gRPC server!
Sending request for name: Diana
Received response: Hello Diana! Welcome to Vert.X gRPC server!
Client finished. Exiting...
```

## How it Works

1. **Server (`GreeterVerticle.java`)**: 
   - Implements the `Greeter` service from the proto file
   - Listens on port 9090
   - Handles `SayHello` requests and returns personalized greetings

2. **Client (`GrpcClient.java`)**:
   - Connects to the server on localhost:9090
   - Sends multiple test requests with different names
   - Displays the responses from the server

3. **Protocol Buffer (`hello.proto`)**:
   - Defines the `Greeter` service with a `SayHello` method
   - Defines `HelloRequest` and `HelloReply` message types

## Key Features

- **Vert.X Integration**: Uses Vert.X for non-blocking, event-driven architecture
- **gRPC Communication**: Implements gRPC server and client using Vert.X gRPC modules
- **Protocol Buffers**: Uses generated code from the proto file for type-safe communication
- **Asynchronous**: All operations are non-blocking and asynchronous

## Dependencies

The project uses:
- Vert.X Core and gRPC modules
- gRPC Java libraries
- Protocol Buffers Java runtime
- Lombok for reducing boilerplate code 
