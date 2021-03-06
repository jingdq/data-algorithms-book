package org.dataalgorithms.chap28.scala

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

/**
 * Finding averge using monoid (implement using Spark's combineByKey())
 *
 * A monoid is a triple (T, ∗, z) such that ∗ is an associative 
 * binary operation on T, and z ∈ T has the property that for 
 * all x ∈ T it holds that x∗z = z∗x = x.
 * 
 * 
 * To find means of numbers, convert each number into (number, 1),
 * then add them preserving a monoid structure:
 * 
 * The monoid structure is defined as (sum, count)
 * 
 * number1 -> (number1, 1)
 * number2 -> (number2, 1)
 * (number1, 1) + (number2, 1) -> (number1+number2, 1+1) = (number1+number2, 2)
 * (number1, x) + (number2, y) ->  (number1+number2, x+y)
 * 
 * Finally, per key, we will have a value as (sum, count), then to find the mean,
 * mean = sum / count
 * 
 * @author Gaurav Bhardwaj (gauravbhardwajemail@gmail.com)
 *
 * @editor Mahmoud Parsian (mahmoud.parsian@yahoo.com)
 *
 */
object MeanMonoidizedUsingCombineByKey {
  //
  def main(args: Array[String]): Unit = {
    //
    if (args.size < 2) {
      println("Usage: MeanMonoidizedUsingCombineByKey <input-dir> <output-dir>")
      sys.exit(1)
    }
    
    val sparkConf = new SparkConf().setAppName("MeanMonoidizedUsingCombineByKey")
    val sc = new SparkContext(sparkConf)

    val input = args(0)
    val output = args(1)

    val records = sc.textFile(input)
    
    // create a monoid structure
    val monoid = records.map(line => {
      val tokens = line.split("\\s+")
      (tokens(0), tokens(1).toInt)
    })

    // combineBykey() requires 3 basic functions:
    val createCombiner = (num: Int) => (num, 1)
    val mergeValue = (c: (Int, Int), num: Int) => (c._1 + num, c._2 + 1)
    val mergeCombiners = (a: (Int, Int), b: (Int, Int)) => (a._1 + b._1, a._2 + b._2)

    val combined = monoid.combineByKey(createCombiner, mergeValue, mergeCombiners)

    val result = combined.mapValues(f => f._1.toDouble / f._2)

    // For debugging
    result.foreach(println)

    result.saveAsTextFile(output)

    // done!
    sc.stop()
  }
}