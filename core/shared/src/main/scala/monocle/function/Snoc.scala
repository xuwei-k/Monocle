package monocle.function

import monocle.function.fields._
import monocle.{Iso, Optional, Prism}

import scala.annotation.implicitNotFound
import scalaz.{Apply, EphemeralStream}
import EphemeralStream.##::

/**
 * Typeclass that defines a [[Prism]] between an `S` and its init `S` and last `S`
 * @tparam S source of [[Prism]] and init of [[Prism]] target
 * @tparam A last of [[Prism]] target, `A` is supposed to be unique for a given `S`
 */
@implicitNotFound("Could not find an instance of Snoc[${S},${A}], please check Monocle instance location policy to " +
  "find out which import is necessary")
abstract class Snoc[S, A] extends Serializable {
  def snoc: Prism[S, (S, A)]

  def initOption: Optional[S, S] = snoc composeLens first
  def lastOption: Optional[S, A] = snoc composeLens second
}

trait SnocFunctions {
  final def snoc[S, A](implicit ev: Snoc[S, A]): Prism[S, (S, A)] = ev.snoc

  final def initOption[S, A](implicit ev: Snoc[S, A]): Optional[S, S] = ev.initOption
  final def lastOption[S, A](implicit ev: Snoc[S, A]): Optional[S, A] = ev.lastOption

  /** append an element to the end */
  final def _snoc[S, A](init: S, last: A)(implicit ev: Snoc[S, A]): S =
    ev.snoc.reverseGet((init, last))

  /** deconstruct an S between its init and last */
  final def _unsnoc[S, A](s: S)(implicit ev: Snoc[S, A]): Option[(S, A)] =
    ev.snoc.getOption(s)
}

object Snoc extends SnocFunctions {

  def apply[S, A](prism: Prism[S, (S, A)]): Snoc[S, A] = new Snoc[S, A] {
    override val snoc: Prism[S, (S, A)] = prism
  }

  /** lift an instance of [[Snoc]] using an [[Iso]] */
  def fromIso[S, A, B](iso: Iso[S, A])(implicit ev: Snoc[A, B]): Snoc[S, B] = Snoc(
    iso composePrism ev.snoc composeIso iso.reverse.first
  )

  /************************************************************************************************/
  /** Std instances                                                                               */
  /************************************************************************************************/
  import scalaz.std.option._

  implicit def listSnoc[A]: Snoc[List[A], A] = Snoc(
    Prism[List[A], (List[A], A)]{
      case init :+ last =>
        Some((init, last))
      case _ =>
        None
    }{
      case (init, last) => init :+ last
    }
  )

  implicit def streamSnoc[A]: Snoc[Stream[A], A] = Snoc(
    Prism[Stream[A], (Stream[A], A)]( s =>
      for {
        init <- if(s.isEmpty) None else Some(s.init)
        last <- s.lastOption
      } yield (init, last)){
      case (init, last) => init :+ last
    }
  )

  implicit val stringSnoc: Snoc[String, Char] = Snoc(
    Prism[String, (String, Char)](
        s => if(s.isEmpty) None else Some((s.init, s.last))){
        case (init, last) => init :+ last
      }
  )

  implicit def vectorSnoc[A]: Snoc[Vector[A], A] = Snoc(
    Prism[Vector[A], (Vector[A], A)](
      v => if(v.isEmpty) None else Some((v.init, v.last))){
      case (xs, x) => xs :+ x
    }
  )

  /************************************************************************************************/
  /** Scalaz instances                                                                            */
  /************************************************************************************************/
  import scalaz.IList

  implicit def iListSnoc[A]: Snoc[IList[A], A] = Snoc(
    Prism[IList[A], (IList[A], A)](
      il => Apply[Option].apply2(il.initMaybe.toOption, il.lastOption)((_,_))){
      case (init, last) => init :+ last
    }
  )

  implicit def ephemeralStreamSnoc[A]: Snoc[EphemeralStream[A], A] = Snoc(
    Prism[EphemeralStream[A], (EphemeralStream[A], A)](s =>
      if(s.isEmpty) {
        None
      } else {
        val x ##:: xs = s.reverse
        Some((xs.reverse, x))
      }
    ){
      case (init, last) => init.foldRight(EphemeralStream(last))(EphemeralStream.cons(_, _))
    }
  )
}