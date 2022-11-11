package com.example.test_vertx_grpc

import grpc.gateway.testing.Echo
import grpc.gateway.testing.EchoServiceGrpcKt
import grpc.gateway.testing.echoResponse
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.grpc.server.GrpcServer
import io.vertx.grpc.server.GrpcServiceBridge
import mu.KotlinLogging
import kotlin.system.exitProcess

class EchoService: EchoServiceGrpcKt.EchoServiceCoroutineImplBase() {

  override suspend fun echo(request: Echo.EchoRequest): Echo.EchoResponse {
    return echoResponse {
      message = request.message
      messageCount = 1
    }
  }

}

private val LOG = KotlinLogging.logger {  }

private val vertex = Vertx.vertx()

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    val grpcServer = GrpcServer.server(vertx)
    GrpcServiceBridge.bridge(EchoService()).bind(grpcServer)


    val router = Router.router(vertex)
    router.route()
      .handler(CorsHandler.create()
        .addOrigin("*")
        .allowedHeader("*"))

    vertx
      .createHttpServer(
        HttpServerOptions()
          .setCompressionSupported(true)
          .setCompressionLevel(9)
      )
      .requestHandler(router)
      .requestHandler(grpcServer)
      .listen(8080) { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port 8080")
        } else {
          startPromise.fail(http.cause());
        }
      }
  }
}

fun main() {
  vertex.deployVerticle(MainVerticle()).onFailure{ e ->
    LOG.error(e) { "Failed to deploy main verticle" }
    exitProcess(-1)
  }
}
