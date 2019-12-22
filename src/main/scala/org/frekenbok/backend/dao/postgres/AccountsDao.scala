package org.frekenbok.backend.dao.postgres

import java.time.Instant
import java.util.UUID

import org.frekenbok.backend.definitions.{Account, AccountType, BalanceItem}
import shapeless.Generic.Aux
import shapeless.{Generic, HNil}

class AccountsDao {

  import dc._

  implicit val accountSchemaMeta = queryMeta {
    q: Query[Account] =>
      q.map { a =>
        (a.balance, infix)
      }
  }

  def get(id: UUID) = {
    quote {
      for {
        account <- query[Account].filter(_.id == id)

      } yield ()
    }
  }

}

object AccountsDao {
  val accountWithoutBalance = Generic[Account]
  accountWithoutBalance
}
