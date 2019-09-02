package org.frekenbok.backend.dao

import java.time.Instant
import java.util.UUID

import org.frekenbok.backend.dao.AccountsDao.AccountRepr
import org.frekenbok.backend.definitions.{Account, AccountType, Money}
import reactivemongo.api.DB
import shapeless.Generic.Aux
import shapeless._

import scala.concurrent.ExecutionContext
import scala.language.reflectiveCalls

class AccountsDao(db: DB)(implicit ec: ExecutionContext, gen: LabelledGeneric.Aux[Account, AccountRepr])
  extends AbstractDao[Account, AccountRepr](db)

object AccountsDao {

  val generic: Aux[Account, UUID :: String :: AccountType :: Option[Instant] :: Option[Instant] :: IndexedSeq[Money] :: HNil] = Generic[Account]

  type AccountRepr = generic.Repr
}