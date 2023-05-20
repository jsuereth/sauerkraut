import sbtlicensereport.license.{DepModuleInfo}
import sbtghactions.JavaSpec


val Deps = new {
  val protobufJava = "com.google.protobuf" % "protobuf-java" % "3.23.1"
  val jawnAst = "org.typelevel" %% "jawn-ast" % "1.4.0"
  val junit = "junit" % "junit" % "4.13.2"
  val easyMock = "org.easymock" % "easymock" % "5.1.0"
}

val commonSettings: Seq[Setting[_]] = Seq(
  organization := (ThisBuild / organization).value,
  organizationName := (ThisBuild / organizationName).value,
  startYear := Some(2019),
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),  
  version := (ThisBuild / version).value,  
  scalaVersion := (ThisBuild / scalaVersion).value,
  libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
  licenseReportTitle := "third_party_licenses",
  licenseReportDir := baseDirectory.value / "third_party",
  licenseReportTypes := Seq(MarkDown),
  licenseReportNotes := {
    case DepModuleInfo(group, id, version) if group contains "com.google.protobuf" => "Google Protocol Buffers"
    case DepModuleInfo(group, id, version) if id contains "junit" => "Used for testing"
    case DepModuleInfo(group, id, version) if id contains "protocjar" => "Used to compile proto files to Java."
  },
)



// Overall GHA setup + project defaults.
ThisBuild / githubWorkflowPublish := Nil
ThisBuild / githubWorkflowArtifactUpload := false
// ThisBuild / githubWorkflowScalaVersions := Seq(dottyVersion)
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17.0"), JavaSpec.temurin("11.0"))
ThisBuild / scalaVersion := "3.2.2"
ThisBuild / crossScalaVersions := Seq((ThisBuild / scalaVersion).value)
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.jsuereth.sauerkraut"
ThisBuild / organizationName := "Google"

val core = project
  .settings(commonSettings:_*)
  .settings(libraryDependencies += Deps.easyMock % "test")

val utils = project
  .settings(commonSettings:_*)
  .dependsOn(core)
  .settings(
    libraryDependencies += Deps.junit,
    // For comparisons
    libraryDependencies += Deps.protobufJava % "test"
  )

val compliance = project
  .settings(commonSettings:_*)
  .dependsOn(core)
  .settings(
    libraryDependencies += Deps.junit
  )

val json = project
  .settings(commonSettings:_*)
  .dependsOn(core, compliance % "test")
  .settings(
    libraryDependencies += Deps.jawnAst
  )

val pb = project
  .settings(commonSettings:_*)
  .dependsOn(core, utils, compliance % "test")

val pbtest = project
  .settings(commonSettings:_*)
  .dependsOn(pb)
  .enablePlugins(ProtobufPlugin)
  .settings(
    publish / skip := true,
    libraryDependencies += Deps.protobufJava,
    ProtobufConfig / protobufRunProtoc  := { args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v370" +: args.toArray)
    }
  )

val nbt = project
  .settings(commonSettings:_*)
  .dependsOn(core, utils, compliance % "test")

val xml = project
  .settings(commonSettings:_*)
  .dependsOn(core, compliance % "test")


val benchmarks = project
  .enablePlugins(JmhPlugin)
  .enablePlugins(ProtobufPlugin)
  .settings(commonSettings:_*)
  .dependsOn(nbt, pb, json, xml)
  .settings(
    run / fork := true,
    run  / javaOptions += "-Xmx6G",
    libraryDependencies += "org.openjdk.jmh" % "jmh-core" % "1.23",
    libraryDependencies += "com.esotericsoftware" % "kryo" % "5.0.3",
    libraryDependencies += "com.lihaoyi" %% "upickle" % "3.1.0",
    ProtobufConfig / protobufRunProtoc := { args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v370" +: args.toArray)
    }
  )

val root = project.in(file(".")).aggregate(
  core,
  compliance,
  json,
  nbt,
  pb,
  xml,
  pbtest,
  benchmarks
).settings(publish / skip := true)