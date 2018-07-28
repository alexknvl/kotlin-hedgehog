package hedgehog;

import kotlin.Pair;

import java.nio.ByteBuffer;

public final class StdGen {
    public final long seed;
    public final long gamma;

    public StdGen(long seed) {
        this(seed, GOLDEN_GAMMA);
    }

    public StdGen(long seed, long gamma) {
        this.seed = seed;
        this.gamma = gamma;
    }

    public Pair<Long, StdGen> next64() {
        long s = seed + gamma;
        return new Pair<>(mix64(s), new StdGen(s, gamma));
    }

    public Pair<Long, StdGen> next64(long max) {
        long s = seed + gamma;
        s = mix64(s);

        long bound = max + 1;

        if ((max & (max + 1)) == 0) {
            // If bounded by a power of two.
            return new Pair<>(
                    s & max,
                    new StdGen(s, gamma));
        } else if (max > 0) {
            long max1 = (Long.MAX_VALUE / bound) * bound;

            long r = s >>> 1;
            while (true) {
                if (r >= max1) {
                    r = mix64(r);
                    continue;
                }
                r = r % bound;
                break;
            }
            return new Pair<>(r, new StdGen(s, gamma));
        } else {
            long r = s;
            while (true) {
                if (r > max) {
                    r = mix64(r);
                    continue;
                }
                break;
            }
            return new Pair<>(r, new StdGen(s, gamma));
        }
    }

    public Pair<Integer, StdGen> next32() {
        long s = seed + gamma;
        return new Pair<>(mix32(s), new StdGen(s, gamma));
    }

    public Pair<StdGen, StdGen> fork() {
        long s = seed + gamma;
        long r1 = mix64(s);

        s += gamma;
        long r2 = mixGamma(s);

        return new Pair<>(
                new StdGen(s, gamma),
                new StdGen(r1, r2));
    }

    public StdGen join(StdGen that) {
        return new StdGen(
                mix64(this.seed) ^ mix64(that.seed),
                mixGamma(mix64(this.gamma) ^ mix64(that.gamma)));
    }

    public static StdGen seed(byte[] array) {
        ByteBuffer bb = ByteBuffer.wrap(array);

        long h = 0;
        for (int i = 0; i < array.length; i++) {
            if (i + 8 <= array.length) { h ^= mix64(bb.getLong(i)); i += 8; }
            else if (i + 4 <= array.length) { h ^= mix64(bb.getInt(i)); i += 4; }
            else if (i + 2 <= array.length) { h ^= mix64(bb.getShort(i)); i += 2; }
            else { h ^= mix64(bb.get(i)); i += 1; }
        }

        return new StdGen(h, mixGamma(h + GOLDEN_GAMMA));
    }

    private static final long GOLDEN_GAMMA = 0x9e3779b97f4a7c15L;
    private static final long A = 0xbf58476d1ce4e5b9L;
    private static final long B = 0x94d049bb133111ebL;
    private static final long C = 0x62a9d9ed799705f5L;
    private static final long D = 0xcb24d0a5c88c35b3L;
    private static final long E = 0xff51afd7ed558ccdL;
    private static final long F = 0xc4ceb9fe1a85ec53L;

    /**
     * Computes Stafford variant 13 of 64bit mix function.
     */
    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * A;
        z = (z ^ (z >>> 27)) * B;
        return z ^ (z >>> 31);
    }

    /**
     * Returns the 32 high bits of Stafford variant 4 mix64 function as int.
     */
    private static int mix32(long z) {
        z = (z ^ (z >>> 33)) * C;
        return (int)(((z ^ (z >>> 28)) * D) >>> 32);
    }

    /**
     * Returns the gamma value to use for a new split instance.
     */
    private static long mixGamma(long z) {
        // MurmurHash3 mix constants
        z = (z ^ (z >>> 33)) * E;
        z = (z ^ (z >>> 33)) * F;
        // force to be odd
        z = (z ^ (z >>> 33)) | 1L;
        // ensure enough transitions
        int n = Long.bitCount(z ^ (z >>> 1));
        return (n < 24) ? z ^ 0xaaaaaaaaaaaaaaaaL : z;
    }
}