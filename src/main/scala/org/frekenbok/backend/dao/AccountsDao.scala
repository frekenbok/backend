package org.frekenbok.backend.dao

import java.util.UUID

import org.frekenbok.backend.definitions.Account
import reactivemongo.api.DB

import scala.concurrent.ExecutionContext

class AccountsDao(db: DB)(implicit ec: ExecutionContext) extends AbstractDao[Account, UUID](db) {

  def getPK(item: Account): UUID = item.id
}
