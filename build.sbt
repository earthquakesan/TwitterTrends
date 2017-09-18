lazy val commonSettings = Seq(
  version := "0.1",
  organization := "org.ermilov",
  scalaVersion := "2.11.11",
  test in assembly := {}
)

lazy val TwitterApp = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "TwitterTrends",
    mainClass in assembly := Some("org.ermilov.spark.TwitterApp"),
    libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.1.0",
    libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "2.1.0",
    libraryDependencies += "org.apache.bahir" % "spark-streaming-twitter_2.11" % "2.1.0",
    libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.6",
    libraryDependencies += "com.quantifind" %% "wisp" % "0.0.4"
  )

lazy val utils = (project in file("utils"))
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "twitter-app.jar"    
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
