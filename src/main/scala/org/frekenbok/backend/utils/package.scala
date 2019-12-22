package org.frekenbok.backend

import scala.reflect.ClassTag

package object utils {

  private val camelToUnderscoreRegexp = "[A-Z\\d]".r

  /**
    * @see [[https://gist.github.com/sidharthkuruvila/3154845 source gist]]
    */
  def camelToUnderscores(name: String): String =
    camelToUnderscoreRegexp.replaceAllIn(name, { m =>
      "_" + m.group(0).toLowerCase()
    })

  def snakeCaseName[A: ClassTag]: String = camelToUnderscores(implicitly[ClassTag[A]].runtimeClass.getSimpleName)

}
