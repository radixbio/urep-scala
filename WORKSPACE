load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file", "http_jar")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository", "new_git_repository")

skylib_version = "1.0.3"

http_archive(
    name = "bazel_skylib",
    sha256 = "1c531376ac7e5a180e0237938a2536de0c54d93f5c278634818e0efc952dd56c",
    type = "tar.gz",
    url = "https://github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib-{}.tar.gz".format(skylib_version, skylib_version),
)

rules_scala_version = "scala_2_13"  # update this as needed

http_archive(
    name = "io_bazel_rules_scala",
    strip_prefix = "rules_scala-%s" % rules_scala_version,
    type = "zip",
    url = "https://github.com/liucijus/rules_scala/archive/scala_2_13.zip"
)

git_repository(
    name = "rules_pkg",
    commit = "7636b7dc2e14bf198a6c21c01e33847f3863e572",
    patch_cmds = ["mv pkg/* ."],
    remote = "https://github.com/itdaniher/rules_pkg.git",
)

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")
load("@io_bazel_rules_scala//:scala_config.bzl", "scala_config")
scala_config(scala_version="2.13.3")
load(
    "@io_bazel_rules_scala//scala:toolchains.bzl",
    "scala_register_toolchains",
)


scala_register_toolchains()

load(
    "@io_bazel_rules_scala//scala:scala.bzl",
    "scala_repositories",
)

scala_repositories()

protobuf_version = "3.11.3"

protobuf_version_sha256 = "cf754718b0aa945b00550ed7962ddc167167bd922b842199eeb6505e6f344852"

http_archive(
    name = "com_google_protobuf",
    sha256 = protobuf_version_sha256,
    strip_prefix = "protobuf-%s" % protobuf_version,
    url = "https://github.com/protocolbuffers/protobuf/archive/v%s.tar.gz" % protobuf_version,
)
load(
    "@io_bazel_rules_scala//scala/scalafmt:scalafmt_repositories.bzl",
    "scalafmt_default_config",
    "scalafmt_repositories",
)

scalafmt_repositories()

scalafmt_default_config()


load(
    "@io_bazel_rules_scala//scala:scala_cross_version.bzl",
    "scala_mvn_artifact",
)
load(
    "@io_bazel_rules_scala//scala:scala_maven_import_external.bzl",
    "scala_maven_import_external",
)
load("//3rdparty:workspace.bzl", "maven_dependencies")

maven_dependencies()

load("//3rdparty:target_file.bzl", "build_external_workspace")

build_external_workspace(name = "third_party")

http_archive(
    name = "io_bazel_rules_docker",
    sha256 = "dc97fccceacd4c6be14e800b2a00693d5e8d07f69ee187babfd04a80a9f8e250",
    strip_prefix = "rules_docker-0.14.1",
    urls = ["https://github.com/bazelbuild/rules_docker/releases/download/v0.14.1/rules_docker-v0.14.1.tar.gz"],
)

load(
    "@io_bazel_rules_docker//repositories:repositories.bzl",
    container_repositories = "repositories",
)

container_repositories()

load("@io_bazel_rules_docker//container:container.bzl", "container_pull")

container_pull(
    name = "openjdk-base",
    digest = "sha256:4f70a5fa4d957a6de322ad2f548eea79f04d1bc71f2a842f79897bd34ec38b3e",
    registry = "index.docker.io",
    repository = "adoptopenjdk/openjdk11",
)

load(
    "@io_bazel_rules_docker//scala:image.bzl",
    _scala_image_repos = "repositories",
)

rules_pkg_dependencies()

_scala_image_repos()

http_jar(
    name = "scala_stm",
    url = "https://oss.sonatype.org/content/repositories/releases/org/scala-stm/scala-stm_2.12/0.8/scala-stm_2.12-0.8.jar",
    sha256 = "307d61bbbc4e6ed33881646f23140ac73d71a508452abdbb8da689e64a1e4d93"
)

http_jar(
    name = "sonopyjava",
    url = "https://github.com/mikex86/SonopyJava/releases/download/v1.0-SNAPSHOT/SonopyJava-1.0-SNAPSHOT.jar"
)

new_git_repository(name = "rediscala",
                   remote = "https://github.com/itdaniher/rediscala.git",
                   branch = "master",
                   verbose = True,
                   workspace_file = "@//:WORKSPACE",
                   build_file_content = """
load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")
package(default_visibility = ["//visibility:public"])
scala_library(
    name = "rediscala",
    srcs = glob(["src/main/scala/redis/*.scala", "src/main/scala/redis/**/*.scala"]),
    deps = ["@third_party//3rdparty/jvm/com/typesafe/akka:akka_actor", "@scala_stm//jar"],
    resources = ["src/main/resources/reference.conf"],
)
""")

all_content = """filegroup(name = "all", srcs = glob(["**"]), visibility = ["//visibility:public"])"""

http_archive(
   name = "rules_foreign_cc",
   strip_prefix = "rules_foreign_cc-master",
   url = "https://github.com/bazelbuild/rules_foreign_cc/archive/master.zip",
)

load("@rules_foreign_cc//:workspace_definitions.bzl", "rules_foreign_cc_dependencies")

rules_foreign_cc_dependencies()

http_jar(
    name = "jna",
    url = "https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.6.0/jna-5.6.0.jar"
)

http_archive(
    name = "xt_audio",
    urls = ["https://github.com/sjoerdvankreel/xt-audio/archive/v1.0.6.tar.gz"],
    strip_prefix = "xt-audio-1.0.6",
    build_file = "@//:xt-audio.BUILD",
)

