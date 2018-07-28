package hedgehog

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import hedgehog.Range
import hedgehog.Size
import hedgehog.StdGen
import hedgehog.Tree

class Gen<A>(val run: (Size, seed: StdGen) -> Tree<Pair<StdGen, Option<A>>>) {
  fun <B> map(f: (A) -> B): Gen<B> = Gen { size, seed ->
    run(size, seed).map { Pair(it.first, it.second.map(f)) }
  }

  fun <B> flatMap(f: (A) -> Gen<B>): Gen<B> = Gen { size, seed ->
    run(size, seed).flatMap {
      val value = it.second
      when (value) {
        is None -> Tree.pure(Pair(it.first, None))
        is Some -> {
          f(value.t).run(size, it.first)
        }
      }
    }
  }

  fun <B> times(that: Gen<B>): Gen<Pair<A, B>> =
      flatMap { a ->
        that.map { b -> Pair(a, b) }
      }

  fun <B> mapTree(f: (Tree<Pair<StdGen, Option<A>>>) -> Tree<Pair<StdGen, Option<B>>>): Gen<B> =
      Gen { size, seed -> f(run(size, seed)) }

  fun shrink(f: (A) -> List<A>): Gen<A> =
      mapTree { it.expand { (seed, a) ->
        when (a) {
          is None -> emptyList()
          is Some -> f(a.t).map { Pair(seed, Some(it)) }
        }
      } }

  fun prune(): Gen<A> =
      mapTree { it.prune() }

  companion object {
    fun <A> pure(a : A): Gen<A> =
        Gen { size, seed -> Tree.pure(Pair(seed, Some(a))) }
    inline fun <A> point(crossinline a: () -> A): Gen<A> =
        Gen { size, seed -> Tree.point { Pair(seed, Some(a())) } }

    fun long(range: Range<Long>): Gen<Long> = Gen { size, seed ->
      val (x, y) = range.bounds(size)
      val min = minOf(x, y)
      val max = maxOf(x, y)
      val (a, s2) = seed.next64(max - min)
      pure(a + min).run(size, s2)
    }.shrink { Shrink.towards(range.origin, it) }
  }
}