scala_library(
    name = "mylib",
    srcs = glob(["lib/*.scala"]),
    visibility = ["//visibility:public"],
)

scala_binary(
    name = "main-bin",
    srcs = [
        "main/Main.scala",
    ],
    main_class = "Main",
    deps = [":mylib"],
)

scala_binary(
    name = "main3-bin",
    srcs = [
        "main/Main3.scala",
    ],
    main_class = "Main",
    scalacopts = ["-Ypartial-unification"],
    deps = [
        ":mylib",
        "@jna//jar",
        "@sonopyjava//jar",
        "@third_party//3rdparty/jvm/com/cibo:evilplot",
        "@third_party//3rdparty/jvm/com/lihaoyi:ammonite_ops",
        "@third_party//3rdparty/jvm/com/lihaoyi:os_lib",
        "@third_party//3rdparty/jvm/com/typesafe/akka:akka_actor_typed",
        "@third_party//3rdparty/jvm/com/typesafe/akka:akka_stream",
        "@third_party//3rdparty/jvm/com/typesafe/akka:akka_stream_typed",
        "@third_party//3rdparty/jvm/org/slf4j:slf4j_nop",
        "@third_party//3rdparty/jvm/org/typelevel:cats_effect",
        "@third_party//3rdparty/jvm/uk/me/berndporr:iirj",
        "@third_party//3rdparty/jvm/org/typelevel:cats_core",
        # for evilplot
        "@third_party//3rdparty/jvm/io/circe:circe_generic_extras",
        "@xt_audio//:xt-driver",
    ],
)

scala_binary(
    name = "main2-bin",
    srcs = [
        "main/Main2.scala",
    ],
    main_class = "Main",
    scalacopts = ["-Ypartial-unification"],
    deps = [
        ":mylib",
        "@jna//jar",
        "@sonopyjava//jar",
        "@third_party//3rdparty/jvm/com/cibo:evilplot",
        "@third_party//3rdparty/jvm/com/lihaoyi:ammonite_ops",
        "@third_party//3rdparty/jvm/com/lihaoyi:os_lib",
        "@third_party//3rdparty/jvm/com/typesafe/akka:akka_actor_typed",
        "@third_party//3rdparty/jvm/com/typesafe/akka:akka_stream",
        "@third_party//3rdparty/jvm/com/typesafe/akka:akka_stream_typed",
        "@third_party//3rdparty/jvm/org/slf4j:slf4j_nop",
        "@third_party//3rdparty/jvm/org/typelevel:cats_effect",
        "@third_party//3rdparty/jvm/uk/me/berndporr:iirj",
        "@third_party//3rdparty/jvm/org/typelevel:cats_core",
        # for evilplot
        "@third_party//3rdparty/jvm/io/circe:circe_generic_extras",
        "@xt_audio//:xt-driver",
    ],
)

scala_image(
    name = "main-docker",
    srcs = [
        "main/Main.scala",
    ],
    base = "//:custom_java_base",
    main_class = "Main",
    deps = [":mylib"],
)
