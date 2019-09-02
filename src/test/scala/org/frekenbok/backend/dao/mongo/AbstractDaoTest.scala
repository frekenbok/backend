package org.frekenbok.backend.dao.mongo

import java.util.UUID

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import reactivemongo.bson.{BSONBinary, BSONDocument}

class AbstractDaoTest extends Specification with Mockito {

  "AbstractDao" should {
    "serialize selector" in {
      val uuid = UUID.fromString("5129beec-1fad-48f7-9c5c-a91c3e308c27")
      val actualResult = AbstractDao.selectorWriter.write(MongoSelector(uuid))
      val expectedResult = BSONDocument("_id" -> BSONBinary(uuid))

      actualResult must beEqualTo(expectedResult)
    }
  }
}
