package org.frekenbok.backend.dao

import java.util.UUID

import org.frekenbok.backend.definitions.Invoice
import reactivemongo.api.DB

import scala.concurrent.{ExecutionContext, Future}

class InvoicesDao(db: Future[DB])(implicit ec: ExecutionContext) extends AbstractDAO[Invoice](db) {
  protected def getId(item: Invoice): UUID = item.id
}
