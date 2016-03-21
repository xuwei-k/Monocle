package monocle
package law

import scalaz.Equal
import scalaprops._
import scalaprops.Property.forAll

object IsoProps {

  def laws[S: Gen : Equal, A: Gen : Equal](iso: Iso[S, A]): Properties[MonocleLaw] = {
    val laws = new IsoLaws(iso)
    Properties.properties(MonocleLaw.iso)(
      MonocleLaw.`iso round trip one way` -> forAll( (s: S) => laws.roundTripOneWay(s)),
      MonocleLaw.`iso round trip other way` -> forAll( (a: A) => laws.roundTripOtherWay(a)),
      MonocleLaw.`iso modify id = id` -> forAll( (s: S) => laws.modifyIdentity(s)),
      MonocleLaw.`iso modifyF Id = Id` -> forAll( (s: S) => laws.modifyFId(s))
    )
  }

}
