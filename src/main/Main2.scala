//package com.urepscala.mypackage

/*
 * Copyright (c) 2020, itdaniher
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Sink, Source, SourceQueue}
import akka.{Done, NotUsed}
import com.cibo.evilplot.colors.{ColorGradients, ScaledColorBar}
import com.cibo.evilplot.plot._
import com.urepscala.mylib.StatOps.{mean, stdDev, variance}
import com.xtaudio.xt._

import scala.concurrent._
import scala.concurrent.duration._
//import uk.me.berndporr.iirj

import java.awt.{Graphics, Graphics2D}

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme
import com.xtaudio.xt.XtStream
import javax.swing.{JFrame, JPanel}
import me.gommeantilegit.sonopy._
import scala.collection.mutable.ArrayBuffer

class SyncQueue[T](q: SourceQueue[T]) {
  def offerBlocking(elem: T, maxWait: Duration = 10.seconds): QueueOfferResult =
    synchronized {
      val result = q.offer(elem)
      Await.result(result, maxWait)
    }
}

object displayPlot {

  /**
   * Display a plot in a JFrame. Passing in a plot makes the window resizable.
   * @param plot The plot to render.
   * @param theme The plot's theme.
   */
  def apply(plot: Plot)(implicit theme: Theme): DrawableFrame = {
    JFrame.setDefaultLookAndFeelDecorated(true)
    new DrawableFrame(Left(plot))
  }

  /**
   * Display any Drawable in a JFrame. Resizing the window does not affect the size of
   * the rendered image.
   * @param drawnPlot the Drawable to show in the window.
   */
  def apply(drawnPlot: Drawable)(implicit theme: Theme): DrawableFrame = {
    JFrame.setDefaultLookAndFeelDecorated(true)
    new DrawableFrame(Right(drawnPlot))
  }

  class DrawablePanel extends JPanel {
    var drawable: Option[Drawable] = None // scalastyle:ignore

    def setDrawable(drawnPlot: Drawable): Unit = {
      drawable = Some(drawnPlot)
    }

    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)
      val g2 = g.asInstanceOf[Graphics2D]
      drawable.foreach { d =>
        g2.drawImage(d.asBufferedImage, 0, 0, this)
      }
    }
  }

  class DrawableFrame(displayable: Either[Plot, Drawable])(implicit theme: Theme) extends JFrame {

    val panel: DrawablePanel = new DrawablePanel()
    init()

    private def init()(implicit theme: Theme): Unit = {
      setTitle("Plot")
      displayable match {
        case Right(d) =>
          setSize(d.extent.width.toInt + 64, d.extent.height.toInt + 64)
          panel.setDrawable(d.scaled(1, 1))
        case Left(p) =>
          setSize(1024, 1024)
          panel.setDrawable(p.render(Extent(1024, 1024)).scaled(1, 1))
      }

      add(panel)
      setVisible(true)
    }

    def getPlotExtent: Extent = {
      Extent(this.getWidth - 64, this.getHeight - 64)
    }

  }
}

object Main extends App {

  System.setProperty("jna.platform.library.path", s"${os.home.toString()}/urep-scala/bazel-bin/external/xt_audio")
  implicit val configuration = aesthetics.DefaultTheme.defaultTheme
  implicit val system = ActorSystem("audiovis")
  implicit val ec = system.dispatcher
  val sonopy = new Sonopy(48000, 512, 32, 512, 64)
  val FORMAT = new XtFormat(new XtMix(48000, XtSample.FLOAT32), 1, 0, 0, 0)
  val parentQueue = Source.queue[ArrayBuffer[Float]](64, OverflowStrategy.dropTail)
  val empty = Heatmap(sonopy.melSpec(new Array[Float](4800)).map(_.map(_.toDouble).toSeq).toSeq)
  val plotDisplay = new displayPlot.DrawableFrame(Left(empty))
  val squeue = parentQueue
    .mapConcat(_.toSeq)
    .grouped(4800)
    .map(renderPipeline)
    .sliding(100, 10)
    .to(Sink.foreach((x: Seq[Double]) => println(s"${(mean(x), stdDev(x), variance(x))}")))
    .run()
  val queue = new SyncQueue(squeue)
  val audio = new XtAudio("Main2 demo app", null, null, null)
  val service = XtAudio.getServiceBySetup(XtSetup.CONSUMER_AUDIO)
  val device = service.openDefaultDevice(false)
  assert(device.supportsFormat(FORMAT))
  val buffer = device.getBuffer(FORMAT)
  val stream = device.openStream(FORMAT, true, false, buffer.current, callback, null.asInstanceOf[XtXRunCallback], None)
  val source: Source[Int, NotUsed] = Source(1 to 10000)
  val done: Future[Done] = source
    .throttle(1, 1.second)
    .runForeach(println)
  var lastRender: Double = System.currentTimeMillis / 1000.0

  stream.start()

  def renderPipeline(inputVector: Seq[Float]): Double = {
    val mel = sonopy.melSpec(inputVector.toArray[Float])
    val smooshed = mel.map(_.map(_.abs.toDouble).toSeq).toSeq
    val max = smooshed.map(_.max).max
    val plot2 = Heatmap(smooshed, colorBar = ScaledColorBar(ColorGradients.inferno, 0, 30))
    plotDisplay.panel.setDrawable(plot2.render(Extent(256, 256)))
    plotDisplay.panel.setVisible(false)
    plotDisplay.panel.setVisible(true)
    val renderedTime: Double = System.currentTimeMillis / 1000.0
    val tickDuration = renderedTime - lastRender
    lastRender = renderedTime
    tickDuration
  }

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
    val outputArray = outputBuffer.array.to(ArrayBuffer)
    // wait for up to maxDuration for akka to enqueue data
    queue.offerBlocking(outputArray, maxDuration) match {
      case QueueOfferResult.Enqueued    => () // normal case
      case QueueOfferResult.Dropped     => println("dropped")
      case QueueOfferResult.Failure(_)  => println("failed to enqueue")
      case QueueOfferResult.QueueClosed => println("queue closed")
    }
  }
  done.onComplete(_ => system.terminate())
}
