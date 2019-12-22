package org.frekenbok.backend.dao

import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONWriter}
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.api.{Cursor, DB}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class AbstractDao[T: BSONDocumentWriter: BSONDocumentReader, PK: BSONWriter](
  db: DB,
)(
  implicit ec: ExecutionContext,
  ct: ClassTag[T]
) {

  protected def getPK(item: T): PK

  private val collection: BSONCollection =
    db.collection(ct.runtimeClass.getSimpleName)

  def get(id: PK): Future[Option[T]] = {
    collection.find[MongoSelector[PK], BSONDocument](MongoSelector[PK](id), None).one[T]
  }

  def add(item: T): Future[UpdateWriteResult] = {
    collection.update.one(MongoSelector[PK](getPK(item)), item, upsert = true)
  }

  def remove(id: PK): Future[WriteResult] = {
    collection.delete().one(MongoSelector[PK](id))
  }

  protected def getMany(filter: BSONDocument, sort: BSONDocument, limit: Int): Future[Vector[T]] = {
    collection
      .find[BSONDocument, BSONDocument](filter, None)
      .sort(sort)
      .cursor[T]()
      .collect[Vector](limit, Cursor.FailOnError[Vector[T]]())
  }
}
