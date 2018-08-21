import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.sql.functions.{count, _}
import org.apache.spark.sql.types.DoubleType

/**
  * Created by mymil on 8/21/2018.
  */
object StreetTreeAnalysis extends App {
  main()

  def main() : Unit = {
    val spark =
      SparkSession.builder
        .master("local[5]")
        .config("spark.sql.shuffle.partition", 5)
        .appName("StreetTreeAnalysis").getOrCreate()

    import spark.implicits._

    val rawDF = spark.
      read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("2015StreetTreesCensus_TREES.csv")
      .cache()

//    rawDF.printSchema()
//
//    val aliveDF =
//      rawDF
//        .filter('status === "Alive")
//        .cache()
//
//    val total = aliveDF.count()
//
//    val countBySpecBoro =
//      aliveDF
//        .groupBy('spc_common, 'boroname)
//        .agg(count("*") as 'count, count("*").cast(DoubleType) / lit(total) as 'percentage)
//        .orderBy('spc_common, 'boroname)
//        .collect()
//
//    for (Row(spec, boroname, count, percentage : Double) <- countBySpecBoro) {
//      println(s"$spec\t$boroname\t$count\t${percentage * 100}%.0f%%")
//    }
//
//    val histogram =
//      rawDF
//        .select('tree_dbh)
//        .rdd
//        .map(_.getInt(0))
//        .histogram(100)
//
//    println(s"90 percentile ${histogram._1(90)}")
//
    val treePositions =
      rawDF
        .select('tree_id, 'x_sp, 'y_sp)
        .orderBy('tree_id)
        .as[Tree]
        .collect()

    println(s"Id of the tree has most neighbors" + mostNeighbors(treePositions))

    spark.stop()
  }

  case class Tree(tree_id : Int, x_sp : Double, y_sp : Double)

  def withInDistance(t1 : Tree, t2 : Tree) : Boolean = {
    val deltaX = t1.x_sp - t2.x_sp
    val deltaY = t1.y_sp - t2.y_sp

    deltaX * deltaX + deltaY * deltaY <= 500.0 * 500.0
  }

  // Returns the Id of the tree has most neighbors
  def mostNeighbors(trees : Array[Tree]) : Int = {
    // build pairs-wise distance

    trees.indices.par.flatMap(t1 =>
      (t1 + 1 until trees.length).flatMap(t2 =>
        if (withInDistance(trees(t1), trees(t2))) {
          Seq(t1, t2)
        }
        else {
          None
        }
    )).groupBy(x => x)
      .maxBy(_._2.length)
      ._1

/** Alternative single-threaded solution **/
//    val counters = Array.fill(trees.length)(0)
//
//    for (t1 <- trees.indices; t2 <- t1 + 1 until trees.length) {
//      if (withInDistance(trees(t1), trees(t2))) {
//        counters(t1) = counters(t1) + 1
//        counters(t2) = counters(t2) + 1
//      }
//    }
//
//    val maxIndex = counters.indexOf(counters.max)
//    trees(maxIndex).tree_id
  }
}
