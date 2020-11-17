package com.urepscala.mypackage

import com.urepscala.mylib._

object Main {

  def main(args: Array[String]): Unit = {
    println("Hello world!")
    println(msg)
    println(MyLib.myString)
  }

  def msg = s"I am running on ${System.getProperty("java.runtime.version")}"

}
