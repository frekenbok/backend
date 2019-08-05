package org.frekenbok.backend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.frekenbok.backend.Invoices.InvoicesResource
import org.frekenbok.backend.config.Config
import org.frekenbok.backend.dao.InvoicesDao
import org.frekenbok.backend.handlers.InvoicesHandlerImpl
import reactivemongo.api.MongoDriver

import scala.util.{Failure, Success}

object Main extends App {

  implicit val as: ActorSystem = ActorSystem("frekenbok")
  implicit val ma: Materializer = ActorMaterializer()

  import as.dispatcher

  val config = ConfigFactory.load().as[Config]

  MongoDriver().connection(config.mongo.uri) match {
    case Failure(exception) =>
      throw exception

    case Success(connection) =>
      val db = connection.database(config.mongo.database)
      Http().bindAndHandle(
        handler = InvoicesResource.routes(new InvoicesHandlerImpl(new InvoicesDao(db))),
        interface = config.http.host,
        port = config.http.port
      ).foreach { binding =>
        println(s"Server started on ${binding.localAddress}")
      }
  }
}
