name := "backend"

version := "0.1"

scalaVersion := "2.12.10"

val akkaStreamVersion = "2.6.1"
val akkaHttpVersion = "10.1.11"
val circeVersion = "0.12.3"
val specs2Version = "4.8.1"

libraryDependencies ++= List(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "org.reactivemongo" %% "reactivemongo" % "0.19.5",

  "org.specs2" %% "specs2-core" % specs2Version % Test,
  "org.specs2" %% "specs2-mock" % specs2Version % Test,

  "com.chuusai" %% "shapeless" % "2.3.3",

  "com.iheart" %% "ficus" % "1.4.7",

  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)

guardrailTasks in Compile := List(
  ScalaServer(file("src/main/resources/swagger.yaml"), pkg = "org.frekenbok.backend")
)

scalacOptions in Test ++= Seq("-Yrangepos")