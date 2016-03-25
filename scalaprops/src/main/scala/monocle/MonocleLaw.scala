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

  val optional = law("optional")
  val `optional set what you get` = law0(optional, "set what you get")
  val `optional get what you set` = law0(optional, "get what you set")
  val `optional set idempotent` = law0(optional, "set idempotent")
  val `optional modify id = id` = law0(optional, "modify id = id")
  val `optional modifyF Id = Id` = law0(optional, "modifyF Id = Id")
  val `optional modifyOption` = law0(optional, "modifyOption")

  val setter = law("setter")
  val `setter set idempotent` = law0(setter, "set idempotent")
  val `setter modify id = id` = law0(setter, "modify id = id")

  val prism = law("prism")
  val `partial round trip one way` = law0(prism, "partial round trip one way")
  val `round trip other way` = law0(prism, "round trip other way")
  val `prism modify id = id` = law0(prism, "modify id = id")
  val `prism modifyF Id = Id` = law0(prism, "modifyF Id = Id")
  val modifyOption = law0(prism, "modifyOption")

  val traversal = law("traversal")
  val `get what you set` = law0(traversal, "get what you set")
  val `set idempotent` = law0(traversal, "set idempotent")
  val `traversal modify id = id` = law0(traversal, "modify id = id")
  val `traversal modifyF Id = Id` = law0(traversal, "modifyF Id = Id")
  val headOption = law0(traversal, "headOption")

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
