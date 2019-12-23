package org.frekenbok.backend.dao

import java.util.UUID

import org.frekenbok.backend.definitions.Account
import reactivemongo.api.DB
import reactivemongo.api.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

class AccountsDao(db: DB)(implicit ec: ExecutionContext) extends AbstractDao[Account, UUID](db) {

  def getPK(item: Account): UUID = item.id

  def getAll: Future[Vector[Account]] = getMany(BSONDocument.empty, BSONDocument.empty, Int.MaxValue)
}
