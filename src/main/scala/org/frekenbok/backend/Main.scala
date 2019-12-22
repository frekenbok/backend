package org.frekenbok.backend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.frekenbok.backend.config.Config
import org.frekenbok.backend.handlers.routes
import reactivemongo.api.AsyncDriver

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Main extends App {

  implicit val as: ActorSystem = ActorSystem("frekenbok")

  import as.dispatcher

  val log = Logger("main")

  (for {
    config <- Future.fromTry(Try(ConfigFactory.load().as[Config]))
    connection <- AsyncDriver().connect(config.mongo.uri)
    db <- connection.database(config.mongo.database)
    binding <- Http().bindAndHandle(
      handler = routes(db),
      interface = config.http.host,
      port = config.http.port
    )
  } yield binding).onComplete({
    case Failure(exception) =>
      log.error(s"Can't start application: ${exception.getMessage}")
      System.exit(1)
    case Success(binding) =>
      log.info(s"Server started on ${binding.localAddress}")
  })
}
