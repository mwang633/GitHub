package com.marvinmiaowang.util

import org.scalatest.FunSuite
import org.scalatest.Matchers._

import scala.util.Random

/**
 * Created by MarvinMiao on 4/5/2015.
 */
class MutableHLLTest extends FunSuite {

  test("Test with random number") {
    testWithRandom(1)
    testWithRandom(10)
    testWithRandom(100)
    testWithRandom(1000)
    testWithRandom(10000)
    testWithRandom(100000)
    testWithRandom(1000000)
    testWithRandom(10000000)
    //testWithRandom(100000000)
    //testWithRandom(1000000000)
  }

  test("Test union") {
    testMerge(1,1)
    testMerge(100, 1)
    testMerge(1, 100)
    testMerge(100, 100)

    testMerge(1, 10000)
    testMerge(10000, 100)

    testMerge(1000000, 100000)
    testMerge(10000, 10000000)
  }


  private def testWithRandom(count : Int) = {
    val hll = new MutableHLL[Long](14, 5)
    val ran = new Random(1L)

    (0 until count).foreach(i => hll.add(ran.nextLong()))

    val actual = hll.getCardinalityDouble
    println("test with " + count + " actual:" + actual)

    assert(hll.getCardinalityDouble === 1.0 * count +- 0.03 * count)
  }

  private def testMerge(c1 : Int, c2 : Int) = {
    val hll1 = new MutableHLL[Long](14, 5)
    val hll2 = new MutableHLL[Long](14, 5)

    val ran = new Random(1L)

    (0 until c1).foreach(i => hll1.add(ran.nextLong()))
    (0 until c2).foreach(i => hll2.add(ran.nextLong()))

    val actual = hll1.mergeWith(hll2).getCardinalityDouble
    println("test merge with " + c1 + " " + c2 + " actual:" + actual)

    assert(actual === 1.0 * (c1 + c2) +- 0.02 * (c1 + c2))
  }
}
