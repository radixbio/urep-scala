scala_library(
    name = "mylib",
    srcs = glob(["lib/*.scala"]),
    visibility = ["//visibility:public"]
)

scala_binary(
    name = "main-bin",
    srcs = [
            "main/Main.scala",
            ],
    main_class = "Main",
    deps = [":mylib"]
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
