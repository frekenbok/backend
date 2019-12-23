package org.frekenbok.backend.handlers

import java.time.Instant
import java.util.UUID

import org.frekenbok.backend.dao.InvoicesDao
import org.frekenbok.backend.definitions.{Invoice, InvoiceListResponse, InvoiceResponse}
import org.frekenbok.backend.invoices.InvoicesHandler
import org.frekenbok.backend.invoices.InvoicesResource._

import scala.concurrent.{ExecutionContext, Future}

class InvoicesHandlerImpl(dao: InvoicesDao)(implicit ec: ExecutionContext) extends InvoicesHandler {

  def createInvoice(respond: createInvoiceResponse.type)(body: Invoice): Future[createInvoiceResponse] = {
    dao
      .add(body)
      .onWriteResult(
        respond.Created(InvoiceResponse(201, body)),
        respond.InternalServerError
      )

  }

  def getInvoices(
    respond: getInvoicesResponse.type
  )(
    before: Option[Instant] = None,
    limit: Option[Int] = Option(20)
  ): Future[getInvoicesResponse] = {
    dao.getMany(before, limit.getOrElse(20)).map { invoices =>
      respond.OK(InvoiceListResponse(200, invoices))
    }
  }

  def getSingeInvoice(respond: getSingeInvoiceResponse.type)(invoiceId: UUID): Future[getSingeInvoiceResponse] = {
    dao.get(invoiceId).onResult(invoice => respond.OK(InvoiceResponse(200, invoice)), respond.NotFound)
  }
}
