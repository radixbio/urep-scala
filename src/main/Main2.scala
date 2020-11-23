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

import com.cibo.evilplot.plot._
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Sink, Source, SourceQueue}
import akka.{Done, NotUsed}
import com.xtaudio.xt._

import scala.concurrent._
import scala.concurrent.duration._
//import uk.me.berndporr.iirj

import com.xtaudio.xt.XtStream
import me.gommeantilegit.sonopy._

import scala.collection.mutable.ArrayBuffer

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{Graphics, Graphics2D}
import java.io.File
import java.util.prefs.Preferences

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.{JFileChooser, JFrame, JPanel}

object displayPlot {

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

    import javax.swing.{JMenuBar, JMenuItem}

    val panel: DrawablePanel = new DrawablePanel()
    init()

    private def createMenuBar()(implicit theme: Theme): Unit = {
      val menubar = new JMenuBar
      val save = new JMenuItem("Save")
      val actionListener = new ActionListener {
        def actionPerformed(e: ActionEvent) = {
          val selectFile = new JFileChooser()
          selectFile.setCurrentDirectory(loadLastSaveDir()) //scalastyle:ignore
          selectFile.setFileFilter(new FileNameExtensionFilter("png", "png"))
          val savedFile: Int = selectFile.showSaveDialog(panel)
          if (savedFile == JFileChooser.APPROVE_OPTION) {
            val extensionPattern = "(.*\\.png)".r
            val file: File = selectFile.getSelectedFile.toString match {
              case extensionPattern(s) => new File(s)
              case s                   => new File(s + ".png")
            }
            savePlot(file)
          }
        }
      }
      save.addActionListener(actionListener)
      menubar.add(save)
      setJMenuBar(menubar)
    }

    private def lastSaveDirPref = "lastSaveDir"

    private def updateLastSaveDir(file: File): Unit = {
      prefs.put(lastSaveDirPref, file.getParent)
    }

    private def loadLastSaveDir(): File = {
      Option(prefs.get(lastSaveDirPref, null))
        .map(new File(_))
        .getOrElse(null)
    }

    private def prefs: Preferences = {
      Preferences.userNodeForPackage(getClass).node("displayPlot")
    }

    private def init()(implicit theme: Theme): Unit = {
      setTitle("Plot")
      displayable match {
        case Right(d) =>
          setSize(d.extent.width.toInt + 64, d.extent.height.toInt + 64)
          panel.setDrawable(d.scaled(0.5, 0.5))
        case Left(p) =>
          setSize(1024, 1024)
          panel.setDrawable(p.render(Extent(1024, 1024)).scaled(0.5, 0.5))
      }

      add(panel)
      createMenuBar()
//      addComponentListener(new ComponentAdapter {
//        override def componentResized(e: ComponentEvent): Unit = {
//          resizePlot(getWidth, getHeight)
//        }
//      })
      setVisible(true)
    }

    def getPlotExtent: Extent = {
      Extent(this.getWidth - 64, this.getHeight - 64)
    }

//    def resizePlot(width: Int, height: Int)(implicit theme: Theme): Unit = {
//      displayable match {
//        case Left(p) => panel.setDrawable(p.render(getPlotExtent).scaled(0.5, 0.5))
//        case _       =>
//      }
//    }

    def savePlot(result: File)(implicit theme: Theme): Unit = {
      displayable match {
        case Right(d) => d.write(result)
        case Left(p)  => p.render(getPlotExtent).scaled(0.25, 0.25).write(result)
      }

      updateLastSaveDir(result)
    }

  }

  /** Display a plot in a JFrame. Passing in a plot makes the window resizable.
   * @param plot The plot to render.
   * @param theme The plot's theme. */
  def apply(plot: Plot)(implicit theme: Theme): DrawableFrame = {
    JFrame.setDefaultLookAndFeelDecorated(true)
    new DrawableFrame(Left(plot))
  }

  /** Display any Drawable in a JFrame. Resizing the window does not affect the size of
   * the rendered image.
   * @param drawnPlot the Drawable to show in the window.
   */
  def apply(drawnPlot: Drawable)(implicit theme: Theme): DrawableFrame = {
    JFrame.setDefaultLookAndFeelDecorated(true)
    new DrawableFrame(Right(drawnPlot))
  }
}

class SyncQueue[T](q: SourceQueue[T]) {
  def offerBlocking(elem: T, maxWait: Duration = 10.seconds): QueueOfferResult =
    synchronized {
      val result = q.offer(elem)
      Await.result(result, maxWait)
    }
}

object Main extends App {

  System.setProperty("jna.platform.library.path", s"${os.home.toString()}/urep-scala/bazel-bin/external/xt_audio")

  implicit val configuration = aesthetics.DefaultTheme.defaultTheme
  implicit val system = ActorSystem("QuickStart")
  implicit val ec = system.dispatcher

//  implicit val circeConfig:Configuration = Configuration.default
  val FORMAT = new XtFormat(new XtMix(48000, XtSample.FLOAT32), 1, 0, 0, 0)
  val parentQueue = Source.queue[ArrayBuffer[Float]](64, OverflowStrategy.dropTail)
  var sonopy = new Sonopy(48000, 512, 128, 512, 128)
  val empty = Heatmap(sonopy.melSpec(new Array[Float](4800)).map(_.map(_.toDouble).toSeq).toSeq)
  val plotDisplay = new displayPlot.DrawableFrame(Left(empty))
  val squeue = parentQueue
    .to(Sink.foreach(res => {
      val l = res.length
      val padded = res.padTo(4800, 0.0.toFloat)
      println(padded.slice(0, 100))
//    val powers = Sonopy.powerSpec(padded.toArray[Float], 256, 60, 256)
//      println(sonopy)
      val mel = sonopy.melSpec(padded.toArray[Float])
      val smooshed = mel.map(_.map(_.toDouble).toSeq).toSeq
      val plot2 = Heatmap(smooshed) //, colorBar = ScaledColorBar(ColorGradients.viridis, 0, 100))
//      println(smooshed)
      plotDisplay.panel.setDrawable(plot2.render(Extent(256, 256)))
      plotDisplay.panel.setVisible(false)
      plotDisplay.panel.setVisible(true)
//      plotDisplay.panel.invalidate()
//      plotDisplay.panel.validate()
//      plotDisplay.panel.repaint()
      println(s"rendered! ${plotDisplay.getPlotExtent}")
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
    val outputArray = outputBuffer.array.to(ArrayBuffer)
    // wait for up to maxDuration for akka to enqueue data
    val offer = queue.offerBlocking(outputArray, maxDuration)
  }
  done.onComplete(_ => system.terminate())
}
