val dottyVersion = "0.22.0-RC1"

val commonSettings: Seq[Setting[_]] = Seq(
  organization := "com.jsuereth.sauerkraut",
  version := "0.1.0",  
  scalaVersion := dottyVersion,
  libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
)

val core = project
  .settings(commonSettings:_*)

val json = project
  .settings(commonSettings:_*)
  .dependsOn(core)

val pb = project
  .settings(commonSettings:_*)
  .dependsOn(core)
  .settings(
    libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.11.3"
  )

val pbtest = project
  .settings(commonSettings:_*)
  .dependsOn(pb)
  .enablePlugins(ProtobufPlugin)
  .settings(
    skip in publish := true,
    libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.11.3",
    protobufRunProtoc in ProtobufConfig := { args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v370" +: args.toArray)
    }
  )

val root = project.in(file(".")).aggregate(core,json,pb,pbtest).settings(skip in publish := true)