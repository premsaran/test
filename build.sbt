name := """exchange"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
javaJpa.exclude("org.hibernate.javax.persistence", "hibernate-jpa-2.0-api"),
  "org.hibernate" % "hibernate-entitymanager" % "4.3.8.Final",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "org.eclipse.persistence" % "org.eclipse.persistence.moxy" % "2.5.0",
  "org.json" % "json" % "20170516",
  "net.sourceforge.dynamicreports" % "dynamicreports-core" % "4.0.0",
  "net.sourceforge.dynamicreports" % "dynamicreports-adhoc" % "4.0.0",
  "net.sourceforge.dynamicreports" % "dynamicreports-googlecharts" % "4.0.0",
  javaJdbc,
  cache,
  javaWs
)
