package pl.edu.icm.trurl.util.grid;

/**
 * Pasted from https://lemire.me/blog/2017/09/18/visiting-all-values-in-an-array-exactly-once-in-random-order/
 *
 */
public class VisitInDisorder {
    int maxrange;
    int prime;
    int index;
    int offset;
    int runningvalue;

    public VisitInDisorder(int range) {
      if(range < 2) throw new IllegalArgumentException("Your range need to be greater than 1 "+range);
      int min = range / 2 ;
      maxrange = range;
      prime = selectCoPrimeResev(min, range);
      offset = java.util.concurrent.ThreadLocalRandom.current().nextInt(range);
      index = 0;
      runningvalue = offset;
    }

    public boolean hasNext() {
      return index < maxrange;
    }

    public int next() {
      runningvalue += prime;
      if (runningvalue >= maxrange) runningvalue -= maxrange;
      index++;
      return runningvalue;
    }

    final static int MAX_COUNT = 100000;

    static int selectCoPrimeResev(int min, int target) {
      int count = 0;
      int selected = 0;
      java.util.concurrent.ThreadLocalRandom rand = java.util.concurrent.ThreadLocalRandom.current();
      for(int val = min; val < target; ++val) {
        if(coprime(val,target)) {
          count += 1;
          if((count == 1) || ( rand.nextInt(count) < 1 )) {
            selected = val;
          }
        };
        if(count == MAX_COUNT) return val;
      }
      return selected;
    }


    static boolean coprime(int u, int v) {
       return gcd(u,v) == 1;
    }

    static int gcd(int u, int v) {
       int shift;
       if (u == 0) return v;
       if (v == 0) return u;
       for (shift = 0; ((u | v) & 1) == 0; ++shift) {
        u >>= 1;
        v >>= 1;
       }

       while ((u & 1) == 0)
        u >>= 1;

      do {
        while ((v & 1) == 0)
            v >>= 1;
        if (u > v) {
            int t = v;
            v = u;
            u = t;
        }
        v = v - u;
      } while (v != 0);
      return u << shift;
   }


}
