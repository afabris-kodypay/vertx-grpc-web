# vertx-grpc-web
GRPC echo service implementation from web-grpc examples

This sample runs a server compatible with the client defined in
https://github.com/grpc/grpc-web

The project uses bufGen plugin to generate stubs for the EchoService defined in the proto file in `src/main/proto`

To build the server `./greadlew clean bufGen assemble`

To run the client follow instruction in https://github.com/grpc/grpc-web README to run the common_js client


#Issue
The web-grpc client performs a preflight check against the server before performing the real GRPC call.

Adding first the route with the cors handler and then the grpcServer to the main server ends up with the preflight request 
serving the incorrect response (no allow-origin headers are being served)
However this setup works with a pure gRPC client (like Postman)

Trying to switch the order makes the cors request working but then the GRPC call fails due to 404
In this scenario the pure GRPC client also fails