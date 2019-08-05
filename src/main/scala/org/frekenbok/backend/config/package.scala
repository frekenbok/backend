package org.frekenbok.backend

package object config {

  case class MongoConfig(host: String, port: Int, database: String) {
    def uri = s"mongodb://$host:$port/$database"
  }

  case class HttpConfig(host: String, port: Int)

  case class Config(mongo: MongoConfig, http: HttpConfig)

}
