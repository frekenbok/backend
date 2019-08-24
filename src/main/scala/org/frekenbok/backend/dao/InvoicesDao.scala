package org.frekenbok.backend.dao

import java.time.Instant
import java.util.UUID

import org.frekenbok.backend.definitions.Invoice
import reactivemongo.api.DB
import reactivemongo.bson.{BSONDocument => doc}

import scala.concurrent.{ExecutionContext, Future}

class InvoicesDao(db: DB)(implicit ec: ExecutionContext) extends AbstractDao[Invoice](db) {

  protected def getId(item: Invoice): UUID = item.id

  def getMany(before: Option[Instant], limit: Int): Future[Vector[Invoice]] = {
    val filter = before.map(b => doc("timestamp" -> doc("$lt" -> b))).getOrElse(doc.empty)
    getMany(filter, doc("timestamp" -> -1), limit)
  }
}
