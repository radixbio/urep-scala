// "Sometimes, stream programs that call unsafeRunSync or other blocking operations hang in the REPL. This is a result of the Scala 2.12’s lambda encoding and is tracked in SI-9076. There are various workarounds:" from https://web.archive.org/web/20201107024955/https://fs2.io/faq.html
interp.configureCompiler(_.settings.Ydelambdafy.tryToSetColon(List("inline")))

// find libs
System.setProperty("jna.platform.library.path", "bazel-bin/external/xt_audio")
