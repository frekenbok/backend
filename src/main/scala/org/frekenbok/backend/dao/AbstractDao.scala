package org.frekenbok.backend.dao

import java.util.UUID

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.api.{Cursor, DB}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros}
import shapeless._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class AbstractDao[T: BSONDocumentReader : BSONDocumentWriter, Repr <: UUID :: HList](db: DB)(implicit ec: ExecutionContext, ct: ClassTag[T], gen: Generic.Aux[T, Repr]) {

  import AbstractDao._

  private val collection: BSONCollection = db.collection(ct.runtimeClass.getSimpleName)

  def get(id: UUID): Future[Option[T]] = {
    collection.find(MongoSelector(id)).one[T]
  }

  def add(item: T): Future[UpdateWriteResult] = {
    collection.update.one(MongoSelector(item), item, upsert = true)
  }

  def remove(id: UUID): Future[WriteResult] = {
    collection.delete().one(MongoSelector(id))
  }

  protected def getMany(filter: BSONDocument, sort: BSONDocument, limit: Int): Future[Vector[T]] = {
    collection.find(filter)
      .sort(sort)
      .cursor[T]()
      .collect[Vector](limit, Cursor.FailOnError[Vector[T]]())
  }
}

object AbstractDao {
  implicit val selectorWriter: BSONDocumentWriter[MongoSelector] = Macros.writer[MongoSelector]
}
