package org.frekenbok.backend.dao

import java.util.UUID

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.api.{Cursor, DB}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class AbstractDAO[T: BSONDocumentReader : BSONDocumentWriter](db: DB)(implicit ec: ExecutionContext, ct: ClassTag[T]) {

  //TODO try to implement this using shapeless
  protected def getId(item: T): UUID

  private val collection: BSONCollection = db.collection(ct.runtimeClass.getSimpleName)

  implicit val selectorWriter: BSONDocumentWriter[Selector] = Macros.writer[Selector]

  def get(id: UUID): Future[Option[T]] = {
    collection.find[Selector](Selector(id)).one[T]
  }

  def add(item: T): Future[UpdateWriteResult] = {
    collection.update.one(Selector(getId(item)), item, upsert = true)
  }

  protected def getMany(filter: BSONDocument, limit: Int): Future[Vector[T]] = {
    collection.find(filter)
      .cursor[T]()
      .collect[Vector](limit, Cursor.FailOnError[Vector[T]]())
  }

}

