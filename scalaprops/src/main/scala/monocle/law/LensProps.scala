package monocle
package law

import scalaprops.Property.forAll
import scalaprops._
import scalaz.Equal

object LensProps {

  def laws[S: Gen : Equal, A: Gen : Equal](lens: Lens[S, A]): Properties[MonocleLaw] = {
    val laws = new LensLaws(lens)
    Properties.properties(MonocleLaw.lens)(
      MonocleLaw.`lens set what you get` -> forAll( (s: S) => laws.getSet(s)),
      MonocleLaw.`lens get what you set` -> forAll( (s: S, a: A) => laws.setGet(s, a)),
      MonocleLaw.`lens set idempotent` -> forAll( (s: S, a: A) => laws.setIdempotent(s, a)),
      MonocleLaw.`lens modify id = id` -> forAll( (s: S) => laws.modifyIdentity(s)),
      MonocleLaw.`lens modifyF Id = Id` -> forAll( (s: S) => laws.modifyFId(s))
    )
  }

}
