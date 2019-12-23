package org.frekenbok.backend.handlers

import java.util.UUID

import org.frekenbok.backend.accounts.AccountsHandler
import org.frekenbok.backend.accounts.AccountsResource._
import org.frekenbok.backend.dao.AccountsDao
import org.frekenbok.backend.definitions.{Account, AccountListResponse, AccountResponse}

import scala.concurrent.{ExecutionContext, Future}

class AccountsHandlerImpl(dao: AccountsDao)(implicit ec: ExecutionContext) extends AccountsHandler {

  def createAccount(respond: createAccountResponse.type)(body: Account): Future[createAccountResponse] = {
    dao
      .add(body)
      .onWriteResult(
        respond.Created(AccountResponse(201, body)),
        respond.InternalServerError
      )
  }

  def getAccounts(respond: getAccountsResponse.type)(): Future[getAccountsResponse] = {
    dao.getAll.map { accounts =>
      respond.OK(AccountListResponse(200, accounts))
    }
  }

  def getSingleAccount(respond: getSingleAccountResponse.type)(accountId: UUID): Future[getSingleAccountResponse] = {
    dao.get(accountId).onResult(account => respond.OK(AccountResponse(200, account)), respond.NotFound)
  }
}
