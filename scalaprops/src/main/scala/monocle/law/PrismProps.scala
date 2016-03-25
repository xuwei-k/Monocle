package monocle
package law

import scalaz.Equal
import scalaz.std.option._
import scalaprops._
import scalaprops.Property.forAll

object PrismProps {

  def laws[S: Gen : Equal, A: Gen : Equal](prism: Prism[S, A]): Properties[MonocleLaw] = {
    val laws: PrismLaws[S, A] = new PrismLaws(prism)
    Properties.properties(MonocleLaw.prism)(
      MonocleLaw.`partial round trip one way` -> forAll( (s: S) => laws.partialRoundTripOneWay(s)),
      MonocleLaw.`round trip other way` -> forAll( (a: A) => laws.roundTripOtherWay(a)),
      MonocleLaw.`modify id = id` -> forAll( (s: S) => laws.modifyIdentity(s)),
      MonocleLaw.`modifyF Id = Id` -> forAll( (s: S) => laws.modifyFId(s)),
      MonocleLaw.modifyOption -> forAll( (s: S) => laws.modifyOptionIdentity(s))
    )
  }

}
