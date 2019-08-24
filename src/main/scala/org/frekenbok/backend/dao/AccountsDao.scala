package org.frekenbok.backend.dao

import java.util.UUID

import org.frekenbok.backend.definitions.Account
import reactivemongo.api.DB

import scala.concurrent.{ExecutionContext, Future}

class AccountsDao(db: DB)(implicit ec: ExecutionContext) extends AbstractDao[Account](db) {
  protected def getId(item: Account): UUID = item.id
}
