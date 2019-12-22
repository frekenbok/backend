package org.frekenbok.backend

import java.time.Instant

import akka.http.scaladsl.unmarshalling.Unmarshaller

import scala.concurrent.Future
import scala.util.Try

package object invoices {

  implicit val instantUnmarshaller: Unmarshaller[String, Instant] = Unmarshaller { _ => string =>
    Future.fromTry(Try(Instant.parse(string)))
  }

}
