package org.ermilov.spark

import java.io.File

import org.apache.spark.{SparkConf, SparkContext}
import com.quantifind.charts.Highcharts._

import scala.collection.mutable

object TrendVisualizationApp {
  def main(args: Array[String]): Unit = {
    val sparkMaster = scala.util.Properties.envOrElse("SPARK_MASTER", "local[4]")
    val config = new SparkConf().setMaster(sparkMaster).setAppName("Twitter Trends")
    val sc = new SparkContext(config)

    val rddPrefix = "/tmp/sorted-top-hashes"
    sc.listFiles()

    val objectFilesPathes = getObjectFileDirs("/tmp", "sorted-top-hashes")
    val objectFiles = objectFilesPathes map { of => sc.objectFile[Tuple2[String, Int]](of.getAbsolutePath()) }
    val topHashes = objectFiles reduce {_.union(_)}
    val topHashesAggr = topHashes reduceByKey(_ + _) filter {case(hash, count) => count > 1}
    val topHashesList = topHashesAggr.collect().toList
    println(topHashesList)

    //val sortedTopHashes = sc.objectFile[Tuple2[String, Int]]("/tmp/sorted-top-hashes-1505399470000")

    //Charts setup
    setPort(8000)
    startServer()
    histogram(topHashesList)
    legend(Seq("HashTags"))
    yAxis("Frequency")
  }

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
