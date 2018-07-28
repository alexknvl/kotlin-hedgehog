import arrow.typeclasses.Eq
import hedgehog.StdGen
import hedgehog.Tree
import org.junit.Assert
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.util.*

class TreeTests {
  fun assertEqualTrees(n: Int, expected: Tree<Any?>, actual: Tree<Any?>) {
    val coEq = Tree.coEq(Eq.any())
    val r = coEq.compare(expected, actual).skip(n).toBoolean()
    assertTrue(r == true)
  }

  fun assertUnequalTrees(n: Int, expected: Tree<Any?>, actual: Tree<Any?>) {
    val coEq = Tree.coEq(Eq.any())
    val r = coEq.compare(expected, actual).skip(n).toBoolean()
    assertTrue(r == false)
  }

  fun assertUndecidedTrees(n: Int, expected: Tree<Any?>, actual: Tree<Any?>) {
    val coEq = Tree.coEq(Eq.any())
    val r = coEq.compare(expected, actual).skip(n).toBoolean()
    assertTrue(r == null)
  }

  @Test
  fun coEq1() {
    val a = Tree { Tree.Companion.Node(10, emptyList()) }
    val b = Tree { Tree.Companion.Node(10, emptyList()) }
    val c = Tree { Tree.Companion.Node(20, emptyList()) }

    assertEqualTrees(10, a, b)
    assertUnequalTrees(10, a, c)
    assertUnequalTrees(10, b, c)
  }

  @Test
  fun coEq2() {
    fun a(): Tree<Int> = Tree { Tree.Companion.Node(10, listOf(a())) }
    fun b(): Tree<Int> = Tree { Tree.Companion.Node(10, listOf(b())) }
    fun c(): Tree<Int> = Tree { Tree.Companion.Node(10, listOf(a(), b())) }

    fun d1(): Tree<Int> = Tree { Tree.Companion.Node(20, listOf(a(), b())) }
    fun d2(): Tree<Int> = Tree { Tree.Companion.Node(10, listOf(a(), d1())) }

    assertUndecidedTrees(100, a(), b())
    assertUnequalTrees(100, d1(), d2())
    assertUnequalTrees(100, d2(), c())

    assertUnequalTrees(100, d2().map { it + 1 }, c().map { it + 1 })
    assertUndecidedTrees(100, a().map { it + 1 }, b().map { it + 1 })

    assertUndecidedTrees(100, a() * b(), b() * a())
    assertUnequalTrees(100, a() * b(), b() * c())
  }
}