package monocle

import monocle.internal.IsEq
import scalaprops._
import scalaz.Equal

package object law {

  implicit def isEqToBoolean[A](isEq: IsEq[A])(implicit A: Equal[A]): Boolean =
    if(A.equal(isEq.lhs, isEq.rhs)) {
      true
    } else {
      val expect = isEq.rhs
      val actual = isEq.lhs
      throw new AssertionError(s"$expect != $actual")
    }

}
