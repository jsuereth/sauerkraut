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

val root = project.in(file(".")).aggregate(core,json,pb).settings(skip in publish := true)