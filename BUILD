package(
    default_visibility = ["//visibility:public"],
)

scala_binary(
    name = "amm",
    srcs = [],
    data = ["//:predef.sc"],
    main_class = "ammonite.Main",
    deps = [
        "//src:mylib",
        "@third_party//3rdparty/jvm/com/lihaoyi:ammonite_2_13_3",
        "@third_party//3rdparty/jvm/org/typelevel:cats_core",
        "@third_party//3rdparty/jvm/org/typelevel:cats_effect",
        "@third_party//3rdparty/jvm/org/typelevel:squants",
        "@xt_audio//:xt-driver",
        "@sonopyjava//jar",
    ],
)

# this is the base docker image used for all scala_image targets
# adoptopenjdk is great, but their images do not have the `java` executable at the path expected by rules_docker

container_image(
    name = "custom_java_base",
    base = "@openjdk-base//image",
    symlinks = {"/usr/bin/java": "/opt/java/openjdk/bin/java"},
    visibility = ["//visibility:public"],
)
