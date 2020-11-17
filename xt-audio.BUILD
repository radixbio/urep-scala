load("@rules_foreign_cc//tools/build_defs:cmake.bzl", "cmake_external")
package(default_visibility = ["//visibility:public"])
filegroup(name = "all", srcs = glob(["**"]), visibility = ["//visibility:public"])
cmake_external(
    name = "xt-core",
    env_vars = {"CXXFLAGS": "-Dredacted='\\\"redacted\\\"'"},
    make_commands = ["VERBOSE=1 make", "mkdir xt-core"],
    cache_entries = {
        "BUILD_SHARED_LIBS": "ON",
        },
    lib_source = "@xt_audio//:all",
    postfix_script = "mkdir -p $INSTALLDIR/lib && cp libxt-core.so $INSTALLDIR/lib",
    shared_libraries = ["libxt-core.so"],
    working_directory = "build"
)

genrule(
    name = "mv-xt-core",
    srcs = [":xt-core"],
    outs = ["linux-x64/libxt-core.so"],
    cmd = "find . && mkdir -p bazel-out/k8-fastbuild/bin/external/xt_audiolinux-x64/ && cp bazel-out/k8-fastbuild/bin/external/xt_audio/xt-core/lib/libxt-core.so bazel-out/k8-fastbuild/bin/external/xt_audio/linux-x64/",
)

java_library(
    name = "xt-driver",
    srcs = glob(["src/java/com/xtaudio/xt/*.java", "src/java-driver/com/xtaudio/xt/driver/Driver.java"]),
    deps = ["@jna//jar", ":xt-core"],
    data = [":xt-core"],
    resources = [":mv-xt-core"]
)

java_binary(
    name = "print-detailed",
    srcs = ["src/java-sample/com/xtaudio/xt/sample/PrintDetailed.java"],
    deps = [":xt-driver"],
    runtime_deps = [":mv-xt-core"],
    main_class = "com.xtaudio.xt.sample.PrintDetailed",
)
