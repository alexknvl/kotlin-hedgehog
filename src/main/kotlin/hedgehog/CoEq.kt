package hedgehog

import arrow.typeclasses.Eq

sealed class Comparison {
  object Equal : Comparison()
  object Unequal : Comparison()
  class Maybe(val resume: () -> Comparison) : Comparison()

  operator fun plus(that: Comparison): Comparison = combine(this, that)

  fun skip(n: Int): Comparison {
    require(n >= 0)
    var r = this
    for (i in 0 until n) {
      when (r) {
        is Maybe -> r = r.resume()
        else -> return r
      }
    }
    return r
  }

  fun toBoolean(): Boolean? = when(this) {
    is Equal -> true
    is Unequal -> false
    is Maybe -> null
  }

  companion object {
    fun combine(a: Comparison, b: Comparison): Comparison =
        when(a) {
          is Comparison.Unequal -> Comparison.Unequal
          is Comparison.Equal -> b
          is Comparison.Maybe -> Comparison.Maybe { combine(b, a.resume()) }
        }

    // Breadth-first strategy.
    fun combineAll(args: Array<Comparison>): Comparison {
      if (args.isEmpty()) return Comparison.Equal

      val first = args.first()
      return when (first) {
        is Comparison.Unequal -> Comparison.Unequal
        is Comparison.Equal -> combineAll(args.drop(1).toTypedArray())
        is Comparison.Maybe -> Comparison.Maybe {
          val res = arrayOfNulls<Comparison>(args.size)
          for (i in 0 until args.size-1) {
            res[i] = args[i + 1]
          }
          res[args.size - 1] = first.resume()
          combineAll(res as Array<Comparison>)
        }
      }
    }
  }
}

interface CoEq<A> {
  fun compare(a: A, b: A): Comparison

  fun <B> contramap(f: (B) -> A): CoEq<B> = object : CoEq<B> {
    override fun compare(a: B, b: B): Comparison =
        Comparison.Maybe { this@CoEq.compare(f(a), f(b)) }
  }

  operator fun <B> times(that: CoEq<B>): CoEq<Pair<A, B>> = object : CoEq<Pair<A, B>> {
    override fun compare(a: Pair<A, B>, b: Pair<A, B>): Comparison {
      return this@CoEq.compare(a.first, b.first) + that.compare(a.second, b.second)
    }
  }

  companion object {
    fun <A> fromEq(eq: Eq<A>): CoEq<A> = object : CoEq<A> {
      override fun compare(a: A, b: A): Comparison = eq.run {
        if (a.eqv(b)) Comparison.Equal else Comparison.Unequal
      }
    }

    fun <A> list(coEq: () -> CoEq<A>): CoEq<List<A>> = object : CoEq<List<A>> {
      override fun compare(a: List<A>, b: List<A>): Comparison {
        if (a.size != b.size) return Comparison.Unequal
        return Comparison.combineAll(a.zip(b).map { (a, b) -> coEq().compare(a, b) }.toTypedArray())
      }
    }
  }
}