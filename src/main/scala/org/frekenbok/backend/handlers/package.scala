package org.frekenbok.backend

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, NotFound}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import io.circe.CursorOp.DownField
import io.circe.DecodingFailure
import org.frekenbok.backend.AkkaHttpImplicits.jsonEntityMarshaller
import org.frekenbok.backend.dao.InvoicesDao
import org.frekenbok.backend.definitions.{Error, ErrorResponse, ErrorType}
import org.frekenbok.backend.invoices.InvoicesResource
import reactivemongo.api.DB

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

package object handlers {

  private implicit class ErrorResponseOps(errorResponse: ErrorResponse.type) {

    def apply(statusCode: StatusCode, message: String): Route = {
      val errorType = statusCode match {
        case NotFound => ErrorType.NotFound
        case BadRequest => ErrorType.BadRequest
        case InternalServerError => ErrorType.InternalServerError
        case _ => ErrorType.InternalServerError
      }

      complete(statusCode -> ErrorResponse(statusCode.intValue, Error(errorType, message)))
    }
  }

  private val exceptionHandler = ExceptionHandler {
    case NonFatal(exception) =>
      extractLog { log =>
        extractRequest { request =>
          log.error(exception, s"Error while handling $request")
          ErrorResponse(InternalServerError, "Internal server error")
        }
      }
  }

  private val rejectionHandler: RejectionHandler = RejectionHandler
    .newBuilder()
    .handle {
      case ValidationRejection(message, _) =>
        ErrorResponse(BadRequest, message)

      case MalformedRequestContentRejection(_, DecodingFailure(_, ops)) =>
        // sort of compromise between hiding of JSON parser and verbosity of 400 response
        val problem = ops.headOption.collect { case DownField(field) => field }.getOrElse("request body")
        ErrorResponse(BadRequest, s"Something is wrong with $problem")

      case MalformedRequestContentRejection(message, _) =>
        ErrorResponse(BadRequest, message)
    }
    .result()

  def routes(db: DB)(implicit ec: ExecutionContext, ma: Materializer): Route = {
    handleExceptions(exceptionHandler) {
      handleRejections(rejectionHandler) {
        InvoicesResource.routes(new InvoicesHandlerImpl(new InvoicesDao(db)))
      }
    } ~ ErrorResponse(NotFound, "Not found")
  }

}
