package hedgehog

import arrow.typeclasses.Order

/**
 * A range describes the bounds of a number to generate, which may or may not
 * be dependent on a 'Size'.
 *
 * @param origin
 *   Get the origin of a range. This might be the mid-point or the lower bound,
 *   depending on what the range represents.
 *
 *   The 'bounds' of a range are scaled around this value when using the
 *   'linear' family of combinators.
 *
 *   When using a 'Range' to generate numbers, the shrinking function will
 *   shrink towards the origin.
 *
 * @param bounds
 *   Get the extents of a range, for a given size.
 */
class Range<A>(val origin: A, val bounds: (Size) -> Pair<A, A>) {

  /** Get the lower bound of a range for the given size. */
  fun lowerBound(size: Size, ord: Order<A>): A {
    val b = bounds(size)
    return ord.run { b.first.min(b.second) }
  }

  /** Get the upper bound of a range for the given size. */
  fun upperBound(size: Size, ord: Order<A>): A {
    val b = bounds(size)
    return ord.run { b.first.max(b.second) }
  }

  fun <B> map(f: (A) -> B): Range<B> =
      Range(f(origin)) { s ->
        val b = bounds(s)
        Pair(f(b.first), f(b.second))
      }

  companion object {
    /**
     * Construct a range which represents a constant single value.
     *
     * {{{
     * scala> Range.singleton(5).bounds(x)
     * (5,5)
     *
     * scala> Range.singleton(5).origin
     * 5
     * }}}
     */
    fun <A> singleton(x: A): Range<A> =
        Range(x) { Pair(x, x) }

    /**
     * Construct a range which is unaffected by the size parameter.
     *
     * A range from `0` to `10`, with the origin at `0`:
     *
     * {{{
     * scala> Range.constant(0, 10).bounds(x)
     * (0,10)
     *
     * scala> Range.constant(0, 10).origin
     * 0
     * }}}
     */
    fun <A> constant(x: A, y: A): Range<A> =
        constantFrom(x, x, y)

    /**
     * Construct a range which is unaffected by the size parameter with a origin
     * point which may differ from the bounds.
     *
     * A range from `-10` to `10`, with the origin at `0`:
     *
     * {{{
     * scala> Range.constantFrom(0, -10, 10).bounds(x)
     * (-10,10)
     *
     * scala> Range.constantFrom(0, -10, 10).origin
     * 0
     * }}}
     *
     * A range from `1970` to `2100`, with the origin at `2000`:
     *
     * {{{
     * scala> Range.constantFrom(2000, 1970, 2100).bounds(x)
     * (1970,2100)
     *
     * scala> Range.constantFrom(2000, 1970, 2100).origin
     * 2000
     * }}}
     */
    fun <A> constantFrom(z: A, x: A, y: A): Range<A> =
        Range(z) { Pair(x, y) }
  }
}
