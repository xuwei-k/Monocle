package monocle
package law

import scalaz.Equal
import scalaprops._
import scalaprops.Property.forAll

object SetterProps {

  def laws[S: Gen : Equal, A: Gen : Equal](setter: Setter[S, A]): Properties[MonocleLaw] = {
    val laws: SetterLaws[S, A] = new SetterLaws(setter)
    Properties.properties(MonocleLaw.setter)(
      MonocleLaw.`setter set idempotent` -> forAll( (s: S, a: A) => laws.setIdempotent(s, a)),
      MonocleLaw.`setter modify id = id` -> forAll( (s: S) => laws.modifyIdentity(s))
    )
  }

}
