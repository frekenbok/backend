package org.frekenbok.backend

import java.nio.ByteBuffer
import java.time.{Instant, LocalDate}
import java.util.UUID

import org.frekenbok.backend.definitions._
import reactivemongo.api.bson.Macros.Annotations.Key
import reactivemongo.api.bson.Subtype.UuidSubtype
import reactivemongo.api.bson._

import scala.util.Try

package object dao {

  case class MongoSelector[PK](@Key("_id") id: PK)

  implicit val uuidReader: BSONReader[UUID] = BSONReader.collect {
    case binary: BSONBinary if binary.subtype == UuidSubtype =>
      val bb = ByteBuffer.wrap(binary.byteArray)
      new UUID(bb.getLong, bb.getLong)
  }

  implicit val uuidWriter: BSONWriter[UUID] = BSONWriter(BSONBinary.apply)

  implicit val instantReader: BSONReader[Instant] = BSONReader.collect {
    case BSONDateTime(value) => Instant.ofEpochMilli(value)
  }

  implicit val instantWriter: BSONWriter[Instant] = BSONWriter(i => BSONDateTime(i.toEpochMilli))

  implicit val localDateReader: BSONReader[LocalDate] = BSONReader.collect {
    case BSONString(value) => LocalDate.parse(value)
  }

  implicit val localDateWriter: BSONWriter[String] = BSONWriter(BSONString.apply)

  //TODO try to implement this using shapeless
  /**
    * Replace `id` fields in BSONDocument to `_id` and vice versa. Sort of work around for
    * MongoDB, required because we can't rename primary key field.
    */
  protected[dao] trait IdAdjustingHandler[T] extends BSONDocumentReader[T] with BSONDocumentWriter[T] {
    protected def underlying: BSONDocumentHandler[T]

    def readDocument(bson: BSONDocument): Try[T] = {
      val adjustedDoc = bson.get("_id") match {
        case Some(id) =>
          bson ++ ("id" -> id)
        case None =>
          bson
      }
      underlying.readTry(adjustedDoc)
    }

    def writeTry(item: T): Try[BSONDocument] = {
      underlying.writeTry(item).map { doc =>
        doc.get("id") match {
          case Some(id) =>
            doc ++ ("_id" -> id) -- "id"
          case None =>
            doc
        }
      }
    }

  }

  implicit def selectorWriter[PK: BSONWriter]: BSONDocumentWriter[MongoSelector[PK]] = Macros.writer[MongoSelector[PK]]

  implicit val moneyHandler: BSONDocumentHandler[Money] = Macros.handler[Money]
  implicit val transactionHandler: BSONDocumentHandler[Transaction] = Macros.handler[Transaction]

  implicit val accountTypeReader: BSONReader[AccountType] = BSONReader.collect {
    case BSONString(string) if AccountType.parse(string).nonEmpty =>
      AccountType.parse(string).get
  }
  implicit val accountTypeWriter: BSONWriter[AccountType] = BSONWriter(at => BSONString(at.value))

  implicit object AccountHandler extends IdAdjustingHandler[Account] {
    protected val underlying: BSONDocumentHandler[Account] = Macros.handler[Account]
  }

  implicit object InvoiceHandler extends IdAdjustingHandler[Invoice] {
    protected val underlying: BSONDocumentHandler[Invoice] = Macros.handler[Invoice]
  }

}
