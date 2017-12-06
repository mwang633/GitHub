import java.util.Date
import scala.collection.mutable

case class Message(priority: Int, ts: Long, msg: String, date: Date, threadName : String) extends Ordered[Message] {
  override def compare(that: Message): Int =
      that.priority - this.priority match {
      case 0 => (that.ts - this.ts).toInt
      case d => d
    }
}

object Logging {
  val SIZE_LIMIT = 10000
  private val queue = new mutable.PriorityQueue[Message]()

  class Logger(defaultPriority : Int = 3) {
    private val sb = new mutable.StringBuilder()

    /** Use this method to log a message with priority
      */
    def log(priority: Int, msg: String): Boolean = {
      queue.synchronized {
        if (queue.size <= SIZE_LIMIT) {
          queue.enqueue(
            Message(
              priority,
              System.currentTimeMillis(),
              msg,
              new Date,
              Thread.currentThread().getName))
          true
        }
        else {
          false
        }
      }
    }

    /** Use this method to log a message with a default priority
      */
    def log(msg : String)  : Boolean = log(defaultPriority, msg)

    /** Use this method to log partial msg
      */
    def logPartial(msg : String) : Unit = {
      sb.append(msg)
    }

    /** Use this method to flush partially logged message
      */
    def logPartialDone(priority : Int) : Boolean = {
      val ret = log(priority, sb.toString())
      sb.clear()
      ret
    }
  }

  object LogReader {
    /** Use this method to retrieve oldest message with highest priority
      */
    def get(): Option[Message] = {
      queue.synchronized {
        if (queue.nonEmpty) {
          Some(queue.dequeue())
        }
        else None
      }
    }
  }

  // Tests
  def main(args: Array[String]): Unit = {
    val logger = new Logger()

    {
      logger.log(1, "abc")
      logger.log(3, "abc")
      logger.log(1, "xyz")

      assert(LogReader.get().exists(_.msg == "abc"))
      assert(LogReader.get().exists(_.msg == "xyz"))
      assert(LogReader.get().exists(_.msg == "abc"))
      assert(LogReader.get().isEmpty)
    }

    {
      logger.log("xyz")
      logger.log(1, "efg")
      logger.log(2, "abc")

      assert(LogReader.get().exists(_.msg == "efg"))
      assert(LogReader.get().exists(_.msg == "abc"))
      assert(LogReader.get().exists(_.msg == "xyz"))
      assert(LogReader.get().isEmpty)
    }

    {
      logger.logPartial("abc")
      logger.logPartial("def")
      logger.logPartial("xyz")
      logger.logPartialDone(1)

      assert(LogReader.get().exists(_.msg == "abcdefxyz"))
    }

    {
      (0 to SIZE_LIMIT).foreach(_ => logger.log("a"))
      assert(!logger.log("a"))
    }
  }
}
