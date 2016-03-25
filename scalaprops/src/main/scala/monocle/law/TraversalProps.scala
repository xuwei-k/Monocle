package monocle
package law

import scalaprops.Property.forAll
import scalaprops._
import scalaz.Equal
import scalaz.std.list._
import scalaz.std.option._

object TraversalProps {

  def law[S: Gen : Equal, A: Gen : Equal](traversal: Traversal[S, A]): Properties[MonocleLaw] = {
    val laws = new TraversalLaws(traversal)
    Properties.properties(MonocleLaw.traversal)(
      MonocleLaw.`traversal get what you set` -> forAll( (s: S, a: A) => laws.setGetAll(s, a)),
      MonocleLaw.`traversal set idempotent` -> forAll( (s: S, a: A) => laws.setIdempotent(s, a)),
      MonocleLaw.`traversal modify id = id` -> forAll( (s: S) => laws.modifyIdentity(s)),
      MonocleLaw.`traversal modifyF Id = Id` -> forAll( (s: S) => laws.modifyFId(s)),
      MonocleLaw.headOption -> forAll( (s: S) => laws.headOption(s))
    )
  }

}
