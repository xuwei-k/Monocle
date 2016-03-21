package monocle

import scalaz._
import scalaprops._

final class MonocleLaw private(val ord: Int, val fullName: String, val simpleName: String) {
  override def hashCode = ord
  override def toString = simpleName
}

object MonocleLaw {
  private[this] val set = collection.mutable.Set.empty[MonocleLaw]

  private[this] def law(fullName: String, simpleName: String = ""): MonocleLaw =
    set.synchronized{
      val name = if(simpleName == "") fullName else simpleName
      val l = new MonocleLaw(set.size, fullName, name)
      set += l
      l
    }

  private[this] def law0(clazz: MonocleLaw, lawName: String): MonocleLaw =
    law(clazz.simpleName + " " + lawName, lawName)

  private[this] def all(clazz: MonocleLaw): MonocleLaw =
    law(clazz.simpleName+ " all", clazz.simpleName)


  val iso = law("iso")
  val `iso round trip one way` = law0(iso, "round trip one way")
  val `iso round trip other way` = law0(iso, "round trip other way")
  val `iso modify id = id` = law0(iso, "modify id = id")
  val `iso modifyF Id = Id` = law0(iso, "modifyF Id = Id")


  val values: List[MonocleLaw] = set.toList

  implicit val monocleLawGen: Gen[MonocleLaw] = {
    val h :: t = values
    Gen.elements(h, t: _*)
  }

  implicit val monocleLawOrder: Order[MonocleLaw] = {
    import scalaz.std.anyVal._
    Order.orderBy(_.ord)
  }
}
