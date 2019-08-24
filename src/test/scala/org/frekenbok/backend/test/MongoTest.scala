package org.frekenbok.backend.test

import java.util.UUID

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.frekenbok.backend.config.MongoConfig
import org.specs2.control.NoLanguageFeatures
import org.specs2.mutable.Specification
import org.specs2.specification.AfterAll
import reactivemongo.api.{DefaultDB, MongoDriver}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

trait MongoTest extends NoLanguageFeatures with AfterAll {
  this: Specification =>

  val mongoConfig: MongoConfig = ConfigFactory.load().as[MongoConfig]("mongo")

  def afterAll(): Unit = {
    Await.result(db.drop(), 5 seconds)
  }

  lazy val db: DefaultDB = Await.result(for {
    connection <- Future.fromTry(MongoDriver().connection(mongoConfig.uri))
    db <- connection.database(s"test-${UUID.randomUUID}")
  } yield db, 5 seconds)
}
