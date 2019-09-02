package org.frekenbok.backend.dao.mongo

import java.time.Instant
import java.util.UUID

import org.frekenbok.backend.definitions.{Invoice, Transaction}
import reactivemongo.api.DB
import reactivemongo.bson.{BSONDocument => doc}
import shapeless.Generic.Aux
import shapeless._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.reflectiveCalls

class InvoicesDao(db: DB)(implicit ec: ExecutionContext, gen: Generic.Aux[Invoice, InvoiceRepr])
  extends AbstractDao[Invoice, InvoiceRepr](db) {

  def getMany(before: Option[Instant], limit: Int): Future[Vector[Invoice]] = {
    val filter = before.map(b => doc("timestamp" -> doc("$lt" -> b))).getOrElse(doc.empty)
    getMany(filter, doc("timestamp" -> -1), limit)
  }
}

object InvoicesDao {
  val generic: Aux[Invoice, UUID :: Instant :: IndexedSeq[Transaction] :: Option[String] :: HNil] = Generic[Invoice]

  type InvoiceRepr = generic.Repr
}