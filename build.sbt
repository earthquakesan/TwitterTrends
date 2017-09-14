name := "TwitterTrends"

version := "0.1"

scalaVersion := "2.11.11"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.2.0" // % "provided"
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "2.2.0" // % "provided"
libraryDependencies += "org.apache.bahir" % "spark-streaming-twitter_2.11" % "2.2.0"
libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.6"
libraryDependencies += "com.quantifind" %% "wisp" % "0.0.4"