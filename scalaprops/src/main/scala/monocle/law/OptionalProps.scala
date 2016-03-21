package monocle
package law

import scalaz.Equal
import scalaz.std.option._
import scalaprops._
import scalaprops.Property.forAll

object OptionalProps {

  def laws[S: Gen : Equal, A: Gen : Equal](optional: Optional[S, A]): Properties[MonocleLaw] = {
    val laws: OptionalLaws[S, A] = new OptionalLaws(optional)
    Properties.properties(MonocleLaw.optional)(
      MonocleLaw.`optional set what you get` -> forAll( (s: S) => laws.getOptionSet(s)),
      MonocleLaw.`optional get what you set` -> forAll( (s: S, a: A) => laws.setGetOption(s, a)),
      MonocleLaw.`optional set idempotent`   -> forAll( (s: S, a: A) => laws.setIdempotent(s, a)),
      MonocleLaw.`optional modify id = id`   -> forAll( (s: S) => laws.modifyIdentity(s)),
      MonocleLaw.`optional modifyF Id = Id`  -> forAll( (s: S) => laws.modifyFId(s)),
      MonocleLaw.`optional modifyOption`     -> forAll( (s: S) => laws.modifyOptionIdentity(s))
    )
  }

}
