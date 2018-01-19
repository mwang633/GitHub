/**
  * Created by mwang on 3/3/17.
  *
  * A week before Thanksgiving, a sly turkey is hiding from a family that wants to cook it for the holiday dinner.
  * There are five boxes in a row, and the turkey hides in one of these boxes. Let's label these boxes sequentially where Box 1 is the leftmost box and Box 5 is the rightmost box. Each night,
  * the turkey moves one box to the left or right, hiding in an adjacent box the next day. Each morning, the family can look in one box to try to find the turkey.

  * How can the family guarantee they will find the turkey before Thanksgiving dinner?
  *
  */


object Turkey extends App {

  val N = 5
  val S = 7

  def solve(actions: Seq[Int], pos: Set[Int]): Option[Seq[Int]] = {
    if (pos.isEmpty) Some(actions)
    else if (actions.length >= S) None
    else {
      val newPos = pos.flatMap(p => Seq(p + 1, p - 1).filter(x => x >= 0 && x < N))

      (0 until N).flatMap(i => {
        solve(actions :+ i, newPos.filter(_ != i))
      }).headOption
    }
  }

  println(solve(Seq(), (0 until N).toSet))
}
