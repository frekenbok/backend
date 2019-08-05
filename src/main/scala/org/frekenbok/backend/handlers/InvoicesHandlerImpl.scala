package org.frekenbok.backend.handlers

import java.time.OffsetDateTime
import java.util.UUID

import org.frekenbok.backend.Invoices.{InvoicesHandler, InvoicesResource}
import org.frekenbok.backend.dao.InvoicesDao
import org.frekenbok.backend.definitions.ErrorType.NotFound
import org.frekenbok.backend.definitions.{Error, ErrorResponse, Invoice, InvoiceResponse}

import scala.concurrent.{ExecutionContext, Future}

class InvoicesHandlerImpl(dao: InvoicesDao)(implicit ec: ExecutionContext) extends InvoicesHandler {
  def createInvoice(respond: InvoicesResource.createInvoiceResponse.type)(body: Invoice): Future[InvoicesResource.createInvoiceResponse] = {
    dao.add(body).map { _ =>
      respond.Created(InvoiceResponse(201, body))
    }
  }

  def getInvoices(respond: InvoicesResource.getInvoicesResponse.type)(before: Option[OffsetDateTime], limit: Option[Int]): Future[InvoicesResource.getInvoicesResponse] = ???

  def getSingeInvoice(respond: InvoicesResource.getSingeInvoiceResponse.type)(invoiceId: UUID): Future[InvoicesResource.getSingeInvoiceResponse] = {
    dao.get(invoiceId).map {
      case Some(invoice) =>
        respond.OK(InvoiceResponse(200, invoice))
      case None =>
        respond.NotFound(ErrorResponse(404, Error(NotFound, s"Invoice $invoiceId not found")))
    }
  }
}
