package org.frekenbok.backend.dao

import java.util.UUID

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import reactivemongo.api.DB
import reactivemongo.bson.{BSONBinary, BSONDocument, BSONDocumentHandler, Macros}

import scala.concurrent.ExecutionContext.Implicits.global

class AbstractDaoTest extends Specification with Mockito {

  case class TestClass(id: UUID, int: Int, string: String)

  implicit object TestClassHandler extends IdAdjustingHandler[TestClass] {
    protected val handler: BSONDocumentHandler[TestClass] = Macros.handler[TestClass]
  }

  val abstractDao: AbstractDao[TestClass] = new AbstractDao[TestClass](mock[DB]) {
    override protected def getId(item: TestClass): UUID = item.id
  }

  "AbstractDao" should {
    "serialize selector" in {
      val uuid = UUID.fromString("5129beec-1fad-48f7-9c5c-a91c3e308c27")
      val actualResult = abstractDao.selectorWriter.write(Selector(uuid))
      val expectedResult = BSONDocument("_id" -> BSONBinary(uuid))

      actualResult must beEqualTo(expectedResult)
    }
  }
}
