package org.ermilov.spark

import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable
import scala.io.Source

object TwitterApp {
  def main(args: Array[String]): Unit = {
    val batchInterval = scala.util.Properties.envOrElse("BATCH_INTERVAL", "10").toInt
    val sparkMaster = scala.util.Properties.envOrElse("SPARK_MASTER", "local[4]")
    val tweetsLanguage = scala.util.Properties.envOrElse("TWEETS_LANGUAGE", "en")
    val hashTagThreshold = scala.util.Properties.envOrElse("HASH_TAG_THRESHOLD", "0").toInt
    val hdfsUri = scala.util.Properties.envOrElse("HDFS_URI", "hdfs://localhost:8020/")
    val storagePrefix = scala.util.Properties.envOrElse("STORAGE_PREFIX", "twitter-trends/top-hashes")
    setTwitterCredentials()

    val config = new SparkConf().setMaster(sparkMaster).setAppName("Twitter Trends")
    val sc = new SparkContext(config)

    val ssc = new StreamingContext(sc, Seconds(batchInterval))

    val tweets = TwitterUtils.createStream(ssc, None)
    val tweetsByLang = tweets filter {tweet => tweet.getLang() == tweetsLanguage}
    val tweetTexts = tweetsByLang map {tweet => tweet.getText()}
    val words = tweetTexts flatMap {text => text.split("""\s+""")}
    val hashTags = words filter {word => word.startsWith("#")}
    val hashTagPairs = hashTags map {tag => (tag, 1)}
    val hashTagCounts = hashTagPairs reduceByKey {_ + _}

    val topHashes = hashTagCounts filter {case (tag, count) => count > hashTagThreshold}
    val sortedTopHashes = topHashes transform {rdd => rdd.sortBy({case(tag, count) => count}, false)}
    println(sortedTopHashes.count())
    sortedTopHashes.print(5)
    sortedTopHashes.saveAsObjectFiles(hdfsUri + storagePrefix)

    val streamingCheckpointDir = "twitter-trends/streaming-checkpoint"
    ssc.checkpoint(hdfsUri + streamingCheckpointDir)
    ssc.start()
    ssc.awaitTermination()
  }

  def setTwitterCredentials(): Unit = {
    var consumerKey = scala.util.Properties.envOrElse("TWITTER_CONSUMER_KEY", "")
    var consumerSecret = scala.util.Properties.envOrElse("TWITTER_CONSUMER_SECRET", "")
    var accessToken = scala.util.Properties.envOrElse("TWITTER_ACCESS_TOKEN", "")
    var accessTokenSecret = scala.util.Properties.envOrElse("TWITTER_ACCESS_TOKEN_SECRET", "")

    if(Vector(consumerKey, consumerSecret, accessToken, accessTokenSecret).exists(_ == "")) {
      val stream = getClass.getResourceAsStream("/twitter_keys.conf")
      val text = Source.fromInputStream(stream).getLines()
      var credentials = new mutable.ArrayStack[String]()
      while(text.hasNext) {
        credentials.push(text.next().split(" = ")(1))
      }
      consumerKey = credentials(3)
      consumerSecret = credentials(2)
      accessToken = credentials(1)
      accessTokenSecret = credentials(0)

    }

    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)
  }
}
