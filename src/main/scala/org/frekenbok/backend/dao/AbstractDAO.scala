package org.frekenbok.backend.dao

import java.util.UUID

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.api.{Cursor, DB}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class AbstractDAO[T: BSONDocumentReader : BSONDocumentWriter](db: Future[DB])(implicit ec: ExecutionContext, ct: ClassTag[T]) {

  protected def getId(item: T): UUID

  private val collection: Future[BSONCollection] = db.map(_.collection(ct.runtimeClass.getSimpleName))

  implicit val selectorWriter: BSONDocumentWriter[Selector] = Macros.writer[Selector]

  def get(id: UUID): Future[Option[T]] = {
    collection.flatMap(_.find[Selector](Selector(id)).one[T])
  }

  def add(item: T): Future[UpdateWriteResult] = {
    collection.flatMap(_.update.one(Selector(getId(item)), item, upsert = true))
  }

  protected def getMany(filter: BSONDocument): Future[Vector[T]] = {
    collection.flatMap(
      _.find(filter)
        .cursor[T]()
        .collect[Vector](100, Cursor.FailOnError[Vector[T]]())
    )
  }

}

