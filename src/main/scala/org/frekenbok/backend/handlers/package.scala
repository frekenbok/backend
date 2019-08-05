package org.frekenbok.backend

import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import org.frekenbok.backend.AkkaHttpImplicits.jsonEntityMarshaller
import org.frekenbok.backend.Invoices.InvoicesResource
import org.frekenbok.backend.dao.InvoicesDao
import org.frekenbok.backend.definitions.{Error, ErrorResponse, ErrorType}
import reactivemongo.api.DB

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

package object handlers {

  private val exceptionHandler = ExceptionHandler {
    case NonFatal(exception) =>
      extractLog { log =>
        extractRequest { request =>
          log.error(exception, s"Error while handling $request")
          complete(InternalServerError -> ErrorResponse(InternalServerError.intValue, Error(ErrorType.InternalServerError, exception.getMessage)))
        }
      }
  }

  private val rejectionHandler: RejectionHandler = RejectionHandler.newBuilder().handle {
    case ValidationRejection(message, _) =>
      complete(BadRequest -> ErrorResponse(BadRequest.intValue, Error(ErrorType.BadRequest, message)))
    case MalformedRequestContentRejection(message, _) =>
      complete(BadRequest -> ErrorResponse(BadRequest.intValue, Error(ErrorType.BadRequest, message)))
  }.result()

  def routes(db: DB)(implicit ec: ExecutionContext, ma: Materializer): Route = {
    handleExceptions(exceptionHandler) {
      handleRejections(rejectionHandler) {
        InvoicesResource.routes(new InvoicesHandlerImpl(new InvoicesDao(db)))
      }
    }
  }

}
