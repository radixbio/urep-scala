//package com.urepscala.mypackage
import com.cibo.evilplot.plot._
// This has "displayPlot" function
import com.cibo.evilplot._
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Sink, Source, SourceQueue}
import akka.{Done, NotUsed}
import com.cibo.evilplot.colors.{ColorGradients, ScaledColorBar}
import com.xtaudio.xt._

import ammonite.ops._
import cats.implicits._
import cats.effect.{ContextShift, IO, Resource, Timer}
import scala.concurrent._
import scala.concurrent.duration._
//import uk.me.berndporr.iirj

import com.xtaudio.xt.XtStream
import me.gommeantilegit.sonopy._

import scala.collection.mutable.ArrayBuffer

class SyncQueue[T](q: SourceQueue[T]) {
  def offerBlocking(elem: T, maxWait: Duration = 10.seconds): QueueOfferResult =
    synchronized {
      val result = q.offer(elem)
      Await.result(result, maxWait)
    }
}
import com.cibo.evilplot.numeric.Point

object Main extends App {

  System.setProperty("jna.platform.library.path", s"${os.home.toString()}/urep-scala/bazel-bin/external/xt_audio")

  implicit val configuration = aesthetics.DefaultTheme.defaultTheme
  implicit val system = ActorSystem("QuickStart")
  implicit val ec = system.dispatcher
//  implicit val circeConfig:Configuration = Configuration.default
  val FORMAT = new XtFormat(new XtMix(48000, XtSample.FLOAT32), 1, 0, 0, 0)
  val parentQueue = Source.queue[ArrayBuffer[Float]](64, OverflowStrategy.dropTail)
  var sonopy = new Sonopy(48000, 256, 60, 256, 30)
  val squeue = parentQueue
    .to(Sink.foreach(res => {
      val l = res.length
      println(res.length)
      val padded = res.padTo(4800, 0.0.toFloat)
//    val powers = Sonopy.powerSpec(padded.toArray[Float], 128, 64, 256)
      println(sonopy)
      val mel = sonopy.melSpec(res.toArray[Float])
      val smooshed = mel.map(_.map(_.toDouble).toSeq).toSeq
      val plot2 = Heatmap(smooshed, colorBar = ScaledColorBar(ColorGradients.viridis, 0, 100))
      plot2.render().asBufferedImage
    }))
    .run()
  val queue = new SyncQueue(squeue)
  val audio = new XtAudio("Main2 demo app", null, null, null)
  val service = XtAudio.getServiceBySetup(XtSetup.CONSUMER_AUDIO)
  val device = service.openDefaultDevice(false)
  val buffer = device.getBuffer(FORMAT)
  val stream = device.openStream(FORMAT, true, false, buffer.current, callback, null.asInstanceOf[XtXRunCallback], None)
  assert(device.supportsFormat(FORMAT))
  val source: Source[Int, NotUsed] = Source(1 to 100)
  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
  stream.start()
  val done: Future[Done] = factorials
    .zipWith(Source(0 to 100))((num, idx) => s"$idx! = $num")
    .throttle(1, 1.second)
    .runForeach(println)

  @throws[Exception]
  def callback(
    stream: XtStream,
    input: Any,
    output: Any,
    frames: Int,
    time: Double,
    position: Long,
    timeValid: Boolean,
    error: Long,
    user: Any
  ): Unit = {
    val maxDuration = 10.second //(1.second / FORMAT.mix.rate) * frames
    // allocate scratch buffer for latest data
    val outputBuffer = java.nio.FloatBuffer.allocate(frames)
    // populate it
    System.arraycopy(input, 0, outputBuffer.array(), 0, frames)
    // get an ArrayBuffer
    val outputArray = outputBuffer.array.to[ArrayBuffer]
    // wait for up to maxDuration for akka to enqueue data
    val offer = queue.offerBlocking(outputArray, maxDuration)
  }
  done.onComplete(_ => system.terminate())
}
