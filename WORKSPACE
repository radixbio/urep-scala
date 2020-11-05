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

rules_scala_version = "eabb1d28fb288fb5b15857260f87818dda5a97c8"  # update this as needed

http_archive(
    name = "io_bazel_rules_scala",
    sha256 = "c75f3f6725369171f7a670767a28fd488190070fc9f31d882d9b7a61caffeb26",
    strip_prefix = "rules_scala-%s" % rules_scala_version,
    type = "zip",
    url = "https://github.com/bazelbuild/rules_scala/archive/%s.zip" % rules_scala_version,
)

load(
    "@io_bazel_rules_scala//scala/scalafmt:scalafmt_repositories.bzl",
    "scalafmt_default_config",
    "scalafmt_repositories",
)

scalafmt_repositories()

scalafmt_default_config()

bind(
    name = "io_bazel_rules_scala_dependency_scalap_scalap",
    actual = "//3rdparty/jvm/org/scala-lang:scalap",
)

bind(
    name = "io_bazel_rules_scala_dependency_scalatest_scalatest",
    actual = "//3rdparty/jvm/org/scalatest:scalatest",
)

bind(
    name = "io_bazel_rules_scala_dependency_scala_scalactic_scalactic",
    actual = "//3rdparty/jvm/org/scalactic:scalactic",
)

git_repository(
    name = "rules_pkg",
    commit = "7636b7dc2e14bf198a6c21c01e33847f3863e572",
    patch_cmds = ["mv pkg/* ."],
    remote = "https://github.com/itdaniher/rules_pkg.git",
)

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")
load(
    "@io_bazel_rules_scala//scala:toolchains.bzl",
    "scala_register_toolchains",
)

scala_register_toolchains()

load(
    "@io_bazel_rules_scala//scala:scala.bzl",
    "scala_repositories",
)

scala_repositories(
    scala_extra_jars = {
        "2.12": {
            "scalatest": {
                "version": "3.0.8",
                "sha256": "a4045cea66f3eaab525696f3000d7d610593778bd070e98349a7066f872844cd",
            },
            "scalactic": {
                "version": "3.0.8",
                "sha256": "5f9ad122f54e9a0112ff4fcaadfb2802d8796f5dde021caa4c831067fca68469",
            },
            "scala_xml": {
                "version": "1.0.5",
                "sha256": "035015366f54f403d076d95f4529ce9eeaf544064dbc17c2d10e4f5908ef4256",
            },
            "scala_parser_combinators": {
                "version": "1.0.4",
                "sha256": "282c78d064d3e8f09b3663190d9494b85e0bb7d96b0da05994fe994384d96111",
            },
        },
    },
    scala_version_shas = (
        "2.12.8",
        {
            "scala_compiler": "f34e9119f45abd41e85b9e121ba19dd9288b3b4af7f7047e86dc70236708d170",
            "scala_library": "321fb55685635c931eba4bc0d7668349da3f2c09aee2de93a70566066ff25c28",
            "scala_reflect": "4d6405395c4599ce04cea08ba082339e3e42135de9aae2923c9f5367e957315a",
            "scalajs_compiler": "fc54c1a5f08598c3aef8b4dd13cf482323b56cb416547da9944655d7c53eae32",
        },
    ),
)

protobuf_version = "3.11.3"

protobuf_version_sha256 = "cf754718b0aa945b00550ed7962ddc167167bd922b842199eeb6505e6f344852"

http_archive(
    name = "com_google_protobuf",
    sha256 = protobuf_version_sha256,
    strip_prefix = "protobuf-%s" % protobuf_version,
    url = "https://github.com/protocolbuffers/protobuf/archive/v%s.tar.gz" % protobuf_version,
)

load(
    "@io_bazel_rules_scala//scala:scala_cross_version.bzl",
    "default_scala_major_version",
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
