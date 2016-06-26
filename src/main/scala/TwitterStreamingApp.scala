import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter._

import java.util.Date
import org.apache.log4j.{Level, Logger}

/**
 *
 * Use this as starting point for performing sentiment analysis on tweets from Twitter
 *
 *  To run this app
 *   ./sbt/sbt assembly
 *   $SPARK_HOME/bin/spark-submit --class "TwitterStreamingApp" --master local[*] ./target/scala-2.10/twitter-streaming-assembly-1.0.jar
 *      <consumer key> <consumer secret> <access token> <access token secret>
 */
object TwitterStreamingApp {
  def main(args: Array[String]) {

    if (args.length < 4) {
      System.err.println("Usage: TwitterStreamingApp <consumer key> <consumer secret> " +
        "<access token> <access token secret> [<filters>]")
      System.exit(1)
    }

    val Array(consumerKey, consumerSecret, accessToken, accessTokenSecret) = args.take(4)
    val filters = args.takeRight(args.length - 4)

    // Set the system properties so that Twitter4j library used by twitter stream
    // can use them to generate OAuth credentials
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    val sparkConf = new SparkConf().setAppName("TwitterPopularTags")
    // to run this application inside an IDE, comment out previous line and uncomment line below
    //val sparkConf = new SparkConf().setAppName("TwitterPopularTags").setMaster("local[*]")

    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val stream = TwitterUtils.createStream(ssc, None, filters)

    val stopWordsRDD = ssc.sparkContext.textFile("src/main/resources/stop-words.txt")
    val posWordsRDD = ssc.sparkContext.textFile("src/main/resources/pos-words.txt")
    val negWordsRDD = ssc.sparkContext.textFile("src/main/resources/neg-words.txt")

    val positiveWords = posWordsRDD.collect().toSet
    val negativeWords = negWordsRDD.collect().toSet
    val stopWords = stopWordsRDD.collect().toSet

    val englishTweets = stream.filter(status => status.getUser().getLang() == "en")
    
    val tweets = englishTweets.map(status => status.getText())
    
    tweets.print()
    ssc.start()
    ssc.awaitTermination()
  }

}
