package org.frekenbok.backend.dao

import java.time.temporal.ChronoUnit.MILLIS
import java.time.{Duration, Instant}
import java.util.UUID

import org.frekenbok.backend.definitions.{Account, AccountType, Money}
import org.frekenbok.backend.test.MongoTest
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class AccountsDaoTest extends Specification with MongoTest {
  sequential

  implicit val env: ExecutionEnv = ExecutionEnv.fromGlobalExecutionContext
  val timeout: FiniteDuration = 5 seconds

  private val dao = new AccountsDao(db)

  private val account = Account(
    UUID.randomUUID,
    "Some account",
    AccountType.Account,
    opened = Some(Instant.now.minus(Duration.ofDays(1)).truncatedTo(MILLIS)),
    balance = Vector(Money("134.15", "JPY"), Money("0.13", "USD"))
  )

  "AccountDao".should {
    "add item".in {
      Await.result(dao.add(account), timeout).ok must beTrue
    }

    "get item".in {
      Await.result(dao.get(account.id), timeout) must beSome(account)
    }

    "update item".in {
      val updated = account.copy(
        closed = Some(Instant.now.truncatedTo(MILLIS)),
        balance = account.balance :+ Money("43.15", "RUB")
      )
      Await.result(dao.add(updated).flatMap(_ => dao.get(updated.id)), timeout) must beSome(updated)
    }

    "remove item".in {
      Await.result(dao.remove(account.id), timeout).ok must beTrue
      Await.result(dao.get(account.id), timeout) must beNone
    }

    "get all".in {
      val accounts = 1.to(10).map { offset =>
        account.copy(
          id = UUID.randomUUID,
          closed = Some(Instant.now.minus(Duration.ofMinutes(offset)).truncatedTo(MILLIS))
        )
      }
      Await.result(Future.sequence(accounts.map(dao.add)), timeout)

      Await.result(dao.getAll, timeout) must containTheSameElementsAs(accounts)
    }
  }

}
