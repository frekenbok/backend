package org.frekenbok.backend

import java.nio.ByteBuffer
import java.time.{LocalDate, OffsetDateTime}
import java.util.UUID

import org.frekenbok.backend.definitions._
import reactivemongo.bson._

package object dao {

  case class Selector(_id: UUID)

  implicit object UuidHandler extends BSONHandler[BSONBinary, UUID] {
    override def read(bson: BSONBinary): UUID = {
      val bb = ByteBuffer.wrap(bson.byteArray);
      val high = bb.getLong();
      val low = bb.getLong();
      new UUID(high, low)
    }

    override def write(uuid: UUID): BSONBinary = {
      BSONBinary(uuid)
    }
  }

  implicit object OffsetDateTimeHandler extends BSONHandler[BSONString, OffsetDateTime] {
    override def read(bson: BSONString): OffsetDateTime = OffsetDateTime.parse(bson.value)

    override def write(offsetDateTime: OffsetDateTime): BSONString = BSONString(offsetDateTime.toString)
  }

  implicit object LocalDateHandler extends BSONHandler[BSONString, LocalDate] {
    override def read(bson: BSONString): LocalDate = LocalDate.parse(bson.value)

    override def write(localDate: LocalDate): BSONString = BSONString(localDate.toString)
  }

  //TODO try to implement this using shapeless
  protected trait IdAdjustingHandler[T] extends BSONDocumentWriter[T] with BSONDocumentReader[T] {
    protected def handler: BSONDocumentHandler[T]

    override def read(bson: BSONDocument): T = {
      val adjustedDoc = bson.get("_id") match {
        case Some(id) =>
          bson ++ ("id" -> id)
        case None =>
          bson
      }
      handler.read(adjustedDoc)
    }

    override def write(item: T): BSONDocument = {
      val doc = handler.write(item)
      doc.get("id") match {
        case Some(id) =>
          doc ++ ("_id" -> id) -- "id"
        case None =>
          doc
      }
    }

  }

  implicit val selectorWriter: BSONDocumentWriter[Selector] = Macros.writer[Selector]

  implicit val moneyHandler: BSONDocumentHandler[Money] = Macros.handler[Money]
  implicit val transactionHandler: BSONDocumentHandler[Transaction] = Macros.handler[Transaction]
  implicit val accountTypeHandler: BSONDocumentHandler[AccountType] = Macros.handler[AccountType]

  implicit object AccountHandler extends IdAdjustingHandler[Account] {
    protected val handler: BSONDocumentHandler[Account] = Macros.handler[Account]
  }

  implicit object InvoiceHandler extends IdAdjustingHandler[Invoice] {
    protected val handler: BSONDocumentHandler[Invoice] = Macros.handler[Invoice]
  }

}
