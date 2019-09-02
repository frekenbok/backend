package org.frekenbok.backend.handlers

import java.time.Instant
import java.util.UUID

import org.frekenbok.backend.dao.mongo.InvoicesDao
import org.frekenbok.backend.definitions.ErrorType.NotFound
import org.frekenbok.backend.definitions.{Error, ErrorResponse, Invoice, InvoiceListResponse, InvoiceResponse}
import org.frekenbok.backend.invoices.{InvoicesHandler, InvoicesResource}

import scala.concurrent.{ExecutionContext, Future}

class InvoicesHandlerImpl(dao: InvoicesDao)(implicit ec: ExecutionContext) extends InvoicesHandler {
  def createInvoice(respond: InvoicesResource.createInvoiceResponse.type)(body: Invoice): Future[InvoicesResource.createInvoiceResponse] = {
    dao.add(body).map { _ =>
      respond.Created(InvoiceResponse(201, body))
    }
  }

  def getInvoices(respond: InvoicesResource.getInvoicesResponse.type)(before: Option[Instant] = None, limit: Option[Int] = Option(20)): Future[InvoicesResource.getInvoicesResponse] = {
    dao.getMany(before, limit.getOrElse(20)).map { invoices =>
      respond.OK(InvoiceListResponse(200, invoices))
    }
  }

  def getSingeInvoice(respond: InvoicesResource.getSingeInvoiceResponse.type)(invoiceId: UUID): Future[InvoicesResource.getSingeInvoiceResponse] = {
    dao.get(invoiceId).map {
      case Some(invoice) =>
        respond.OK(InvoiceResponse(200, invoice))
      case None =>
        respond.NotFound(ErrorResponse(404, Error(NotFound, s"Invoice $invoiceId not found")))
    }
  }
}
