import scala.collection.mutable
import scala.io.Source

/**
  */


object Crypt extends App {
  val dict =
  // Source.fromFile("/usr/share/dict/words")
    Source.fromFile("/Users/mwang/google-10000-english.txt")
      .getLines().map(_.toLowerCase).toList
      .groupBy(w => w.length)
      .mapValues(_.toStream)

  val input = "MABIX THG GEBKBSI GABF CSFFSLX WXVEPCGF XSFBIP MBGA NBTBNSI XZZHEG SISF TH ABTG SCCXSEF JCHT WXVHWBTL IXSKBTL PHJE CSFFMHEW FGBII JTRTHMT".split(" ")
  //val input = "MABIX THG GEBKBSI GABF CSFFSLX".split(" ")

  val inputSorted = input.sortBy(s => dict(s.length).length)

  var best: String = _
  var max = 0

  type Mapping = Array[Int]

  def decode(index: Int, mapping: Mapping, skips: Int): Option[Mapping] = {
    if (index == input.length) Some(mapping)
    else {
      val cryptWord = inputSorted(index)

      val newMapping = Array.fill(26)(-1)

      dict(cryptWord.length)
        .flatMap(dictWord => {
          mapping.copyToArray(newMapping)

          var isValid = true

          cryptWord.zip(dictWord).foreach {
            case _ if !isValid =>

            case (cc, dc) =>
              newMapping(cc - 'A') match {
                case -1 =>
                  newMapping(cc - 'A') = dc - 'a'

                case d if d == (dc - 'a') =>

                case _ =>
                  isValid = false
              }
          }

          if (isValid)
            decode(index + 1, newMapping, skips)
          else if (skips < 3)
            decode(index + 1, mapping, skips + 1)
          else
            None

        }).headOption
    }
  }

  val emptyMapping = Array.fill(26)(-1)
  val mappings = decode(0, emptyMapping, 0).get

  println(input.map(w => w.map(c => ('a' + mappings(c - 'A')).toChar)).mkString(" "))


}
