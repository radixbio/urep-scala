## Bazel infrastructure for Scala

Scala's usually built with [SBT, and this is not great](https://www.lihaoyi.com/post/SowhatswrongwithSBT.html).

Bazel, a build tool from Google, is [the worst build system, except for all the
others](https://medium.com/windmill-engineering/bazel-is-the-worst-build-system-except-for-all-the-others-b369396a9e26).

This repository contains all the infrastructure and examples required to allow you to build Scala code and its dependencies in Bazel.

### What's included?

- functioning, up-to-date, intellij support!
- [bazel-deps](https://github.com/johnynek/bazel-deps) for managing dependencies via dependencies.yaml.
- [rules_scala](https://github.com/bazelbuild/rules_scala) for compiling and formatting, via scalafmt
- [rules_docker](https://github.com/bazelbuild/rules_docker) for packaging and isolated deployment
- [ammonite](http://ammonite.io/) as a powerful REPL for working with your libraries

bonus compatible versioning for...

akka, scalaz, circe, logstage

#### getting started on the CLI

Ubuntu 20.04, amd64 - confirmed to work. Everything else may require some tweaks.

* clone this repo
* `git submodule init && git submodule update` - to grab the intellij bazel plugin for IDE support
* `./scripts/update_bazel_deps.sh` - grab dependencies
* download [bazelisk](https://github.com/bazelbuild/bazelisk) from [here](https://github.com/bazelbuild/bazelisk/releases/download/v1.7.4/bazelisk-linux-amd64)
* rename as `bazel` and mark executable, place somewhere in your $PATH
* hello, world!

#### hello world, and hello world in docker

```
SPX:~/urep-scala $ bazel run //src:main-bin
INFO: Analyzed target //src:main-bin (0 packages loaded, 0 targets configured).
INFO: Found 1 target...
Target //src:main-bin up-to-date:
  bazel-bin/src/main-bin.jar
INFO: Elapsed time: 0.195s, Critical Path: 0.01s
INFO: 0 processes.
INFO: Build completed successfully, 1 total action
INFO: Build completed successfully, 1 total action
Hello world!
I am running on 11.0.9+11-Ubuntu-0ubuntu1.20.04
this is a string in a library.

SPX:~/urep-scala $ bazel run //src:main-docker
INFO: Analyzed target //src:main-docker (47 packages loaded, 6463 targets configured).
INFO: Found 1 target...
Target //src:main-docker up-to-date:
  bazel-bin/src/main-docker-layer.tar
INFO: Elapsed time: 2.457s, Critical Path: 0.68s
INFO: 0 processes.
INFO: Build completed successfully, 1 total action
INFO: Build completed successfully, 1 total action
Loaded image ID: sha256:ad196f426e5adf796470e6d861526f7de92ab2821e67087cb6a7eda671ad4146
Tagging ad196f426e5adf796470e6d861526f7de92ab2821e67087cb6a7eda671ad4146 as bazel/src:main-docker
Hello world!
I am running on 11.0.8+10
this is a string in a library.
```

#### hello world, with Ammonite

`bazel run //:amm`


```
SPX:~/urep-scala $ bazel run //:amm
INFO: Analyzed target //:amm (1 packages loaded, 4 targets configured).
INFO: Found 1 target...
Target //:amm up-to-date:
  bazel-bin/amm.jar
INFO: Elapsed time: 0.092s, Critical Path: 0.00s
INFO: 0 processes.
INFO: Build completed successfully, 1 total action
INFO: Build completed successfully, 1 total action
Loading...
Welcome to the Ammonite Repl 2.2.0 (Scala 2.12.8 Java 11.0.9)
@ import com.urepscala.mylib._
import com.urepscala.mylib._

@ println(com.urepscala.mylib.MyLib.myString)
this is a string in a library.

@ com.urepscala.mylib.MyLib.helloWorld
hello, world!

@ Bye!
```

#### format your code with scalafmt and bazel!

```
SPX:~/urep-scala $ bazel run //src:main-bin.format
INFO: Analyzed target //src:main-bin.format (0 packages loaded, 0 targets configured).
INFO: Found 1 target...
SUBCOMMAND: # //src:main-bin [action 'ScalaFmt src/src/main/Main.scala.fmt.output', configuration: 45d0b89373a0dfd75ac4b432cf2b12138c1203b34bdec57557dfef52e7a6ed03]
(cd /home/name/.cache/bazel/_bazel_name/0f392d4f227f7735cc2ffb1555d4a4e9/execroot/__main__ && \
  exec env - \
  bazel-out/host/bin/external/io_bazel_rules_scala/scala/scalafmt/scalafmt '--jvm_flag=-Dfile.encoding=UTF-8' @bazel-out/k8-fastbuild/bin/src/src/main/Main.scala.fmt.output-0.params)
Target //src:main-bin.format up-to-date:
  bazel-bin/src/main-bin.format
INFO: Elapsed time: 0.192s, Critical Path: 0.06s
INFO: 1 process: 1 worker.
INFO: Build completed successfully, 2 total actions
INFO: Build completed successfully, 2 total actions
```

#### build plugin for intellij:

The checksum for a dependency in the intellij plugin is presently incorrect, for whatever slightly unsettling reason.

Patch in the expected checksum and build the intellij plugin zip by running the below as "build.sh" in `tools/intellij-bazel/`

```bash

#!/bin/bash

JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

git checkout 1b66ac89e13513243f910c9cf950d99872ce8d5b

sed -ie 's/fa9524db2f5b6e42be559ffdab73a963ca06ca057e27a290f5665d38e581764a/61eb876781f3fd75f2d9e76cac192672a02e008725ad9d7ac0fbd4e3dcf25b16/' WORKSPACE

bazel build //ijwb:ijwb_bazel_zip --define=ij_product=intellij-2020.2 --sandbox_debug --verbose_failures

cp bazel-bin/ijwb/ijwb_bazel.zip .

echo 4b4d9d6e52aa559e79f7b0a9fbfcdbcece3e91a8add7d7c0eed51b3cad8dde23  ijwb_bazel.zip | sha256sum -c -

echo 'WORKSPACE' >> .gitignore
```

TODO: add this as a bazel rule :)

#### the rest of the owl

* grab intellij community edition:

`wget https://download.jetbrains.com/idea/ideaIC-2020.2.3.tar.gz`

install the plugin (built?) and settings (from releases), cross your fingers

TODO: add these steps as a bazel rule :)

Bonus:

* check out [Recaf](https://github.com/Col-E/Recaf/releases/download/2.12.0/recaf-2.12.0-J8-jar-with-dependencies.jar) and poke around the inside of the freshly built `.jar`s
