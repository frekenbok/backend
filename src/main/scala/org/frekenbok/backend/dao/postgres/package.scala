package org.frekenbok.backend.dao

import doobie.quill.DoobieContext
import io.getquill.SnakeCase

package object postgres {

  val dc = new DoobieContext.Postgres(SnakeCase)

}
