package hedgehog

import arrow.core.identity
import arrow.typeclasses.Eq

class Tree<out A>(val run: () -> Node<A>) {
  fun <B> map(f: (A) -> B): Tree<B> =
      Tree { run().map(f) }

  operator fun <B> times(that: Tree<B>): Tree<Pair<A, B>> =
      Tree {
        val a = this.run()
        val b = that.run()
        Node(Pair(a.value, b.value),
            a.children.flatMap {
              val a = it
              b.children.map { a * it }
            })
      }

  fun <B> flatMap(f: (A) -> Tree<B>): Tree<B> =
      Tree {
        val a = this.run()
        val b = f(a.value).run()
        Node(b.value, a.children.map { it.flatMap(f) } + b.children)
      }

  fun prune(): Tree<A> =
      Tree { Node(run().value, emptyList()) }

  companion object {
    fun <A> pure(value: A): Tree<A> =
        Tree { Node(value, emptyList()) }
    inline fun <A> point(crossinline value: () -> A): Tree<A> =
        Tree { Node(value(), emptyList()) }

    fun <A, B> unfoldTree(f: (B) -> A, g: (B) -> List<B>, x: B): Tree<A> =
        Tree { Node(f(x), unfoldForest(f, g, x)) }

    fun <A, B> unfoldForest(f: (B) -> A, g: (B) -> List<B>, x: B): List<Tree<A>> =
        g(x).map { unfoldTree(f, g, it) }

    data class Node<out A>(val value: A, val children: List<Tree<A>>) {
      fun <B> map(f: (A) -> B): Node<B> =
          Node(f(value), children.map { it.map(f) })
    }

    fun <A> coEq(a: Eq<A>): CoEq<Tree<A>> = (CoEq.fromEq(a) * CoEq.list { coEq(a) }).contramap {
      val (v, c) = it.run()
      Pair(v, c)
    }
  }
}

fun <A> Tree<A>.expand(f: (A) -> List<A>): Tree<A> =
    Tree {
      val n = this.run()
      Tree.Companion.Node(n.value, n.children.map { it.expand(f) } +
          Tree.unfoldForest(::identity, f, n.value))
    }