package hedgehog

object Shrink {
  /**
   * Shrink an integral number by edging towards a destination.
   *
   * {{{
   * scala> towards(0, 100)
   * List(0, 50, 75, 88, 94, 97, 99)
   *
   * scala> towards(500, 1000)
   * List(500, 750, 875, 938, 969, 985, 993, 997, 999)
   *
   * scala> towards(-50,  -26)
   * List(-50, -38, -32, -29, -27)
   * }}}
   *
   * ''Note we always try the destination first, as that is the optimal shrink.''
   */
  fun towards(destination: Long, x: Long): List<Long> =
    if (destination == x) {
      emptyList()
    } else {
      // Halve the operands before subtracting them so they don't overflow.
      // Consider `min` and `max` for a fixed sized type like 'Int'.
      val diff = (x / 2) - (destination / 2)
      halves(diff).map { x - it }
    }

  /**
   * Produce a list containing the progressive halving of an integral.
   *
   * {{{
   * scala> halves(15)
   * List(15, 7, 3, 1)
   *
   * scala> halves(100)
   * List(100, 50, 25, 12, 6, 3, 1)
   *
   * scala> halves(-26)
   * List(-26, -13, -6, -3, -1)
   * }}}
   */
  fun halves(a: Long): List<Long> {
    val r = mutableListOf<Long>()
    var x = a
    while (x != 0L) {
      x /= 2
      r.add(x)
    }
    if (r.size > 0) r.add(0)
    return r
  }
}