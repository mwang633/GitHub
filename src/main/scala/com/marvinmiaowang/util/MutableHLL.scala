package com.marvinmiaowang.util

import com.google.common.hash.Hashing

import scala.collection.mutable

/**
 * A simple HyperLogLog library for cardinality approximation.
 * based on https://github.com/aggregateknowledge/java-hll
 * Created by MarvinMiao on 4/2/2015.
 */
class MutableHLL[T](val log2m: Byte, val regwidth: Byte) {
  require(log2m >= 4 && log2m <= 31)
  require(regwidth >= 2 && regwidth <= 7)

  @transient final val m: Int = 1 << log2m
  @transient private final val mBitsMask: Int = m - 1
  @transient private final val rhoMask = ~((1L << (((1 << regwidth) - 1) - 1)) - 1)

  @transient private final val smallEstCutoff = (m * 5.0) / 2
  @transient private final lazy val twoToL = {
      val maxRegValue = (1 << regwidth) - 1
      val rhoBits = maxRegValue - 1
      val totalBits = rhoBits + log2m
      math.pow(2.0, totalBits)
    }
  @transient private final lazy val largeEstCutoff = twoToL / 30.0
  @transient private final lazy val alphaMSquared = MutableHLL.alphaMSquared(m)
  @transient private final val sparseModeThreshold = m / 16

  // sparse bucket representation using hash map
  val sparseBuckets = new mutable.HashMap[Int, Byte]()
  // full buckets using byte array. only create in full mode
  var fullBuckets: Array[Byte] = null

  def add(v: T): Unit = {
    val rawValue = MutableHLL.getHash(v)
    val substreamValue: Long = rawValue >>> log2m

    if (substreamValue != 0L) {
      val rho = (1 + MutableHLL.leastSignificantBit(substreamValue | rhoMask)).toByte
      if (rho != 0) {
        val bucketIdx = (rawValue & mBitsMask).toInt

        // check if need to switch to full mode
        if (fullBuckets == null && sparseBuckets.size >= sparseModeThreshold){
          toFullMode()
        }

        setBucketWithMax(bucketIdx, rho)
      }
    }
  }

  def getCardinalityDouble: Double = {
    val (sum, zeroBucketCounts) =
      fullBuckets match {
        case null => // sparse mode
          (sparseBuckets.map(kv => 1.0 / (1L << kv._2)).sum + (m - sparseBuckets.size), m - sparseBuckets.size)
        case _ => //full mode
          fullBuckets.map(b => if (b == 0) (1.0, 1) else (1.0 / (1L << b), 0))
            .reduce((v1, v2) => (v1._1 + v2._1, v1._2 + v2._2))
      }

    val estimator: Double = alphaMSquared / sum
    if ((zeroBucketCounts > 0) && (estimator < smallEstCutoff)) {
      MutableHLL.smallEstimator(m, zeroBucketCounts)
    }
    else if (estimator <= largeEstCutoff) {
      estimator
    }
    else {
      MutableHLL.largeEstimator(twoToL, estimator)
    }
  }

  def getCardinality: Long = getCardinalityDouble.toLong

  def mergeWith(other: MutableHLL[T]): MutableHLL[T] ={
    require(other.log2m == log2m)
    require(other.regwidth == regwidth)

    if (other.fullBuckets == null) {
      other.sparseBuckets.foreach(kv => setBucketWithMax(kv._1, kv._2))
    }
    else {
      (0 until m).foreach(i => setBucketWithMax(i, other.fullBuckets(i)))
    }
    this
  }

  def toFullMode() : Unit = {
    if (fullBuckets == null){
      fullBuckets = new Array[Byte](m)
      sparseBuckets.foreach(kv => fullBuckets(kv._1) = kv._2)
      sparseBuckets.clear()
    }
  }

  private def getBucket(idx : Int): Byte ={
    if (fullBuckets == null) {
      // sparse mode
      sparseBuckets.get(idx) match {
        case None => 0
        case Some(v) => v
      }
    }
    else {
      fullBuckets(idx)
    }
  }

  private def setBucketWithMax(idx: Int, newVal: Byte): Unit = {
    if (newVal == 0) {
      return
    }
    else if (fullBuckets == null) {
      // sparse mode
      sparseBuckets.get(idx) match {
        case None =>
          sparseBuckets(idx) = newVal
        case Some(oldVal) if oldVal < newVal =>
          sparseBuckets(idx) = newVal
        case _ => // No action
      }
    }
    else {
      if (fullBuckets(idx) < newVal) {
        fullBuckets(idx) = newVal
      }
    }
  }
}

object MutableHLL {
  private final val LEAST_SIGNIFICANT_BIT = Array[Int](
    -1, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
    4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0)

  final def alpha(bits: Int): Double = bits match {
    case 4 => 0.673
    case 5 => 0.697
    case 6 => 0.709
    case _ => 0.7213 / (1.0 + 1.079 / (1 << bits).toDouble)
  }

  final def alphaMSquared(m: Int): Double =
    m match {
      case 16 =>
        0.673 * m * m
      case 32 =>
        0.697 * m * m
      case 64 =>
        0.709 * m * m
      case _ =>
        (0.7213 / (1.0 + 1.079 / m)) * m * m
    }

  final def getHash[T](v: T): Long =
    v match {
      case x: Int => Hashing.murmur3_128().hashInt(x).asLong()
      case x: Long => Hashing.murmur3_128().hashLong(x).asLong()
      // TODO: support string
      case _ => v.hashCode()
    }

  final def leastSignificantBit(value: Long): Int = {
    if (value == 0L) -1
    else if ((value & 0xFFL) != 0) LEAST_SIGNIFICANT_BIT(((value >>> 0) & 0xFF).toInt)
    else if ((value & 0xFFFFL) != 0) LEAST_SIGNIFICANT_BIT(((value >>> 8) & 0xFF).toInt) + 8
    else if ((value & 0xFFFFFFL) != 0) LEAST_SIGNIFICANT_BIT(((value >>> 16) & 0xFF).toInt) + 16
    else if ((value & 0xFFFFFFFFL) != 0) LEAST_SIGNIFICANT_BIT(((value >>> 24) & 0xFF).toInt) + 24
    else if ((value & 0xFFFFFFFFFFL) != 0) LEAST_SIGNIFICANT_BIT(((value >>> 32) & 0xFF).toInt) + 32
    else if ((value & 0xFFFFFFFFFFFFL) != 0) LEAST_SIGNIFICANT_BIT(((value >>> 40) & 0xFF).toInt) + 40
    else if ((value & 0xFFFFFFFFFFFFFFL) != 0) LEAST_SIGNIFICANT_BIT(((value >>> 48) & 0xFF).toInt) + 48
    else LEAST_SIGNIFICANT_BIT(((value >>> 56) & 0xFFL).toInt) + 56
  }

  final def smallEstimator(m: Int, zeroBucketCount: Int): Double = m * math.log(m.toDouble / zeroBucketCount)

  final def largeEstimator(twoToL : Double, estimator : Double) : Double = -1.0 * twoToL * math.log(1.0 - (estimator / twoToL))
}
