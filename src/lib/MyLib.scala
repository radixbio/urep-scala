package com.urepscala.mylib
import scala.Numeric.Implicits._

object MyLib {
  val myString: String = "this is a string in a library."
  def helloWorld() = { println("hello, world!") }
}

object StatOps {
  def stdDev[T: Numeric](xs: Iterable[T]): Double = math.sqrt(variance(xs))

  def variance[T: Numeric](xs: Iterable[T]): Double = {
    val avg = mean(xs)

    xs.map(_.toDouble).map(a => math.pow(a - avg, 2)).sum / xs.size
  }

  def mean[T: Numeric](xs: Iterable[T]): Double = xs.sum.toDouble / xs.size
}