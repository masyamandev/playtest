name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

libraryDependencies ++= Seq(
  "org.sorm-framework" % "sorm" % "0.3.16",
//  "com.h2database" % "h2" % "1.3.168",
  "mysql" % "mysql-connector-java" % "5.1.21",
  "org.mindrot" % "jbcrypt" % "0.3m"
)

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"