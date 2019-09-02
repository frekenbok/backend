package org.frekenbok.backend.dao.mongo

import java.time.{Instant, LocalDate}
import java.util.UUID

import org.frekenbok.backend.definitions.{Invoice, Money, Transaction}
import org.specs2.mutable.Specification
import reactivemongo.bson.{BSONArray, BSONBinary, BSONDecimal, BSONDocument}

class InvoiceHandlerTest extends Specification {

  val expenseAccountId: UUID = UUID.randomUUID
  val walletAccountId: UUID = UUID.randomUUID
  val invoice = Invoice(
    id = UUID.randomUUID,
    timestamp = Instant.parse("2019-08-06T01:24:15.450Z"),
    transactions = Vector(
      Transaction(
        accountId = expenseAccountId,
        amount = Money(BigDecimal("23.15"), "RUB"),
        date = LocalDate.parse("2019-08-06")
      ),
      Transaction(
        accountId = walletAccountId,
        amount = Money(BigDecimal("-23.15"), "RUB"),
        date = LocalDate.parse("2019-08-09")
      )
    ),
    description = Some("Just some invoice")
  )
  val invoiceDoc = BSONDocument(
    "timestamp" -> "2019-08-06T01:24:15.450Z",
    "transactions" -> BSONArray(
      List(
        BSONDocument(
          "accountId" -> invoice.transactions.head.accountId,
          "amount" -> BSONDocument(
            "amount" -> BSONDecimal.fromBigDecimal(invoice.transactions.head.amount.amount).get,
            "currency" -> "RUB"
          ),
          "date" -> "2019-08-06"
        ),
        BSONDocument(
          "accountId" -> invoice.transactions.last.accountId,
          "amount" -> BSONDocument(
            "amount" -> BSONDecimal.fromBigDecimal(invoice.transactions.last.amount.amount).get,
            "currency" -> "RUB"
          ),
          "date" -> "2019-08-09"
        )
      )
    ),
    "description" -> "Just some invoice",
    "_id" -> BSONBinary(invoice.id)
  )

  "InvoiceHandler" should {
    "write document" in {
      InvoiceHandler.write(invoice) mustEqual invoiceDoc
    }

    "read document" in {
      InvoiceHandler.read(invoiceDoc) mustEqual invoice
    }
  }
}
