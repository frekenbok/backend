package org.frekenbok.backend.dao

import java.time.temporal.ChronoUnit.{HOURS, MINUTES}
import java.time.{Instant, LocalDate}
import java.util.UUID

import org.frekenbok.backend.definitions.{Invoice, Money, Transaction}
import org.frekenbok.backend.test.MongoTest
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.{implicitConversions, postfixOps}
import scala.util.Random

class InvoicesDaoTest extends Specification with MongoTest {
  sequential

  implicit val env: ExecutionEnv = ExecutionEnv.fromGlobalExecutionContext
  val timeout: FiniteDuration = 5 seconds

  private val dao = new InvoicesDao(db)

  val expenseAccountId: UUID = UUID.randomUUID
  val walletAccountId: UUID = UUID.randomUUID
  val invoice: Invoice = Invoice(
    UUID.randomUUID,
    Instant.parse("2019-08-06T01:24:15.450Z"),
    Vector(
      Transaction(
        expenseAccountId,
        Money("23.15", "RUB"),
        LocalDate.parse("2019-08-06")
      ),
      Transaction(
        walletAccountId,
        Money("-23.15", "RUB"),
        LocalDate.parse("2019-08-09")
      )
    ),
    Some("Just some invoice")
  )

  "InvoicesDao" should {
    "create item" in {
      Await.result(dao.add(invoice), timeout).ok must beTrue
    }

    "get item" in {
      Await.result(dao.get(invoice.id), timeout) must beSome(invoice)
    }

    "update item" in {
      val newTransaction = Transaction(UUID.randomUUID, Money("10.15", "RUB"), LocalDate.of(2025, 3, 1))
      val updated = invoice.copy(transactions = invoice.transactions :+ newTransaction)

      Await.result(dao.add(updated).flatMap(_ => dao.get(updated.id)), timeout) must beSome(updated)
    }

    "remove item" in {
      Await.result(dao.remove(invoice.id), timeout).ok must beTrue
      Await.result(dao.get(invoice.id), timeout) must beNone
    }

    "get all items sorted by descending timestamp" in {
      val invoices = 1.to(10).map { _ =>
        invoice.copy(
          id = UUID.randomUUID,
          timestamp = invoice.timestamp.minus(Random.nextInt(12), HOURS).minus(Random.nextInt(60), MINUTES)
        )
      }
      Await.result(Future.sequence(invoices.map(dao.add)), timeout)

      val actualResult = Await.result(dao.getMany(None, invoices.size), timeout)

      Await.result(Future.sequence(invoices.map(i => dao.remove(i.id))), timeout)

      actualResult mustEqual invoices.sortBy(-_.timestamp.toEpochMilli)
    }

    "get all items before given timestamp" in {
      val invoices = 1.to(10).map { offset =>
        invoice.copy(
          id = UUID.randomUUID,
          timestamp = invoice.timestamp.minus(offset, HOURS)
        )
      }.sortBy(-_.timestamp.toEpochMilli)
      Await.result(Future.sequence(invoices.map(dao.add)), timeout)

      val before = invoices(5).timestamp
      val actualResult = Await.result(dao.getMany(Some(before), invoices.size), timeout)

      Await.result(Future.sequence(invoices.map(i => dao.remove(i.id))), timeout)

      actualResult mustEqual invoices.drop(6)
    }
  }

}
