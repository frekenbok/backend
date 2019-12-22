package org.frekenbok.backend.dao

import java.time.Instant
import java.util.UUID

import org.frekenbok.backend.definitions.Invoice
import reactivemongo.api.DB
import reactivemongo.api.bson.{BSONDocument => doc}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.reflectiveCalls

class InvoicesDao(db: DB)(implicit ec: ExecutionContext) extends AbstractDao[Invoice, UUID](db) {

  protected def getPK(item: Invoice): UUID = item.id

  def getMany(before: Option[Instant], limit: Int): Future[Vector[Invoice]] = {
    val filter = before.map(b => doc("timestamp" -> doc("$lt" -> b))).getOrElse(doc.empty)
    getMany(filter, doc("timestamp" -> -1), limit)
  }
}
