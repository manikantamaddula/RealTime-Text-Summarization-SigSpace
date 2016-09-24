import AssemblyKeys._

assemblySettings

organization := "org.altic.spark.clustering"

name := "spark-clustering"

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.2"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.5.2"

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"
resolvers +=  "OpenIMAJ Maven Repo" at "http://maven.openimaj.org"
resolvers += "Semantic Desktop" at "http://aperture.sourceforge.net/maven"















//mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
//{
//  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
//  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
//  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
//  case "about.html" => MergeStrategy.rename
//  case x => old(x)
//}
//}

libraryDependencies ++= Seq(


  //per image retrieval pipeline
  "org.openimaj" % "core" % "1.3.5",
  "org.openimaj" % "image-feature-extraction" % "1.3.5",
  "org.openimaj" % "image-local-features" % "1.3.5")