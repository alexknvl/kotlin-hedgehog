package hedgehog

/**
 * Tests are parameterized by the size of the randomly-generated data, the
 * meaning of which depends on the particular generator used.
 */
class Size(val value: Int) {
  fun inc() : Size = Size(value + 1)

  /**
   * Scale a size using the golden ratio.
   */
  fun golden(): Size = Size((value * 0.61803398875).toInt())
}

class Bits(val value: Double)