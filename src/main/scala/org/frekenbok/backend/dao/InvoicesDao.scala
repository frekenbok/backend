package org.frekenbok.backend.dao

import java.time.OffsetDateTime
import java.util.UUID

import org.frekenbok.backend.definitions.Invoice
import reactivemongo.api.DB
import reactivemongo.bson.{BSONDocument => doc}

import scala.concurrent.{ExecutionContext, Future}

class InvoicesDao(db: DB)(implicit ec: ExecutionContext) extends AbstractDAO[Invoice](db) {

  protected def getId(item: Invoice): UUID = item.id

  def getMany(before: Option[OffsetDateTime], limit: Option[Int]): Future[Vector[Invoice]] = {
    val filter = before.map(b => doc("timestamp" -> doc("$lt" -> b))).getOrElse(doc.empty)
    getMany(filter, limit.getOrElse(20))
  }
}
