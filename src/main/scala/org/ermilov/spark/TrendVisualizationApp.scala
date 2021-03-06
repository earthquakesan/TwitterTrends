package org.ermilov.spark

import java.io.File

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}
import com.quantifind.charts.Highcharts._
import org.apache.hadoop.conf.Configuration

import scala.collection.mutable

object TrendVisualizationApp {
  def main(args: Array[String]): Unit = {
    val sparkMaster = scala.util.Properties.envOrElse("SPARK_MASTER", "local[4]")
    val config = new SparkConf().setMaster(sparkMaster).setAppName("Twitter Trends - Visualization")
    val hdfsUri = scala.util.Properties.envOrElse("HDFS_URI", "hdfs://localhost:8020/")
    val storageFolder = scala.util.Properties.envOrElse("STORAGE_FOLDER", "twitter-trends/")
    val storagePrefix = scala.util.Properties.envOrElse("STORAGE_PREFIX", "top-hashes")
    val updateInterval = scala.util.Properties.envOrElse("UPDATE_INTERVAL_MS", "30000").toInt
    val sc = new SparkContext(config)

    //Update loop
    while(true) {
      val objectFileDirs = getObjectFileDirs(hdfsUri, storageFolder, storagePrefix)
      val objectFiles = objectFileDirs map { of => sc.objectFile[Tuple2[String, Int]](of) }
      val topHashes = objectFiles reduce {_.union(_)}
      val topHashesAggr = topHashes reduceByKey(_ + _) filter {case(hash, count) => count > 1}
      val topHashesList = topHashesAggr.collect().toList

      setPort(8000)
      startServer()
      histogram(topHashesList)
      legend(Seq("HashTags"))
      yAxis("Frequency")
      Thread.sleep(updateInterval)
    }
  }

  /** This function returns from HDFS all the directories inside storageFolder which contains storagePrefix
    * @param hdfsUri
    * @param storageFolder
    * @param storagePrefix
    * @return mutable.ArrayStack[String]
    */
  def getObjectFileDirs(hdfsUri: String, storageFolder: String, storagePrefix: String): mutable.ArrayStack[String] = {
    val configuration = new Configuration()
    configuration.set("fs.defaultFS", hdfsUri)
    val fs = FileSystem.get(configuration)

    var objectFileDirs = new mutable.ArrayStack[String]()
    val dirs = List(hdfsUri + storageFolder)
    for(dir <- dirs){
      val status = fs.listStatus(new Path(dir))
      status.foreach({
        x =>
          if(x.isDirectory && x.getPath.toString.contains(storagePrefix))
            objectFileDirs.push(x.getPath.toString)
      })
    }
    objectFileDirs
  }

  /** This function returns from local filesystem all the directories from dir which contain prefix
    * @param dir
    * @param prefix
    * @return mutable.ArrayStack[File]
    */
  def getObjectFileDirs(dir: String, prefix: String): mutable.ArrayStack[File] = {
    var objectFiles = new mutable.ArrayStack[File]()

    val d = new File(dir)
    d.listFiles().foreach({
      d => {
        if (d.isDirectory() && d.getName().contains(prefix)) {
          objectFiles.push(d)
        }
      }
    })
    objectFiles
  }

}
