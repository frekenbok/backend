package org.frekenbok.backend.dao.mongo

import org.frekenbok.backend.dao.mongo.AccountsDao.AccountRepr
import org.frekenbok.backend.definitions.Account
import reactivemongo.api.DB
import shapeless._

import scala.concurrent.ExecutionContext
import scala.language.reflectiveCalls

class AccountsDao(db: DB)(implicit ec: ExecutionContext, gen: LabelledGeneric.Aux[Account, AccountRepr])
  extends AbstractDao[Account, AccountRepr](db)

object AccountsDao {

  //noinspection TypeAnnotation
  val generic = Generic[Account]

  type AccountRepr = generic.Repr
}
