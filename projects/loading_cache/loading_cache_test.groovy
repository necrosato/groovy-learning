package loading_cache;
import loading_cache.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class LoadingCacheTest {
  static void main(String[] args) {
    TestLoadingCacheWorks();
    TestLoadingCacheWorksConcurrent();
  }

  static void TestLoadingCacheWorks() {
    def cache_size = 3;
    def cache = new LoadingCache<Integer,Integer>(new LeastRecentlyUsed<String,Integer>(cache_size));
    def compute_counts = [:];
    cache.set(1, this.&compute, [1, compute_counts]);
    cache.set(2, this.&compute, [2, compute_counts]);
    cache.set(3, this.&compute, [3, compute_counts]);
    cache.set(4, this.&compute, [4, compute_counts]);

    assert(cache.get(1) == 1);
    assert(cache.get(1) == 1);
    assert(cache.get(2) == 2);
    assert(cache.get(2) == 2);
    assert(cache.get(3) == 3);
    assert(cache.get(3) == 3);
    assert(cache.get(4) == 4);
    assert(cache.get(4) == 4);
    assert(cache.get(2) == 2);
    assert(cache.get(1) == 1);
    assert(cache.get(4) == 4);

    assert(compute_counts[1] == 2)
    assert(compute_counts[2] == 1)
    assert(compute_counts[3] == 1)
    assert(compute_counts[4] == 1)

    assert(cache.CachedSize() <= cache_size);
  }

  static void TestLoadingCacheWorksConcurrent() {
    def cache_size = 2;
    def cache = new LoadingCache<Integer,Integer>(new LeastRecentlyUsed<Integer,Integer>(cache_size));
    def es = Executors.newCachedThreadPool();
    def compute_counts = [:];
    int nkeys = 8;
    for (int i = 0; i < nkeys; i++) {
      cache.set(i, this.&compute_delayed, [i, 100, compute_counts]);
    }
    def result_map = [:];
    for (int i = 0; i < nkeys; i++) {
      result_map[i] = 0;
    }
    int niters = 1000;
    for (int j = 0; j < niters; j++) {
      for (int i = 0; i < nkeys; i++) {
        // Cannot use i because the value is not captured until the function runs, as opposed to when the thread is created.
        // This way we force cache.get(ic) to get the value of ic when the loop iterates, not the value of i when the thread executes (which may be after loop termination).
        int ic = i;
        es.execute(new Runnable() {
          @Override
          public void run() {
            def val = cache.get(ic);
            synchronized(result_map) {
              result_map[val]++;
            }
          }
        });
      }
    }
    es.shutdown();
    assert(es.awaitTermination(10, TimeUnit.SECONDS));
    for (int i = 0; i < nkeys; i++) {
      assert(result_map[i] == niters);
    }
    assert(cache.CachedSize() <= cache_size);
  }

  static int compute(int i, Map<Integer,Integer> compute_counts) {
    synchronized(compute_counts) {
      if (!compute_counts.containsKey(i)) {
        compute_counts[i] = 1;
      } else {
        compute_counts[i]++;
      }
    }
    return i;
  }

  static int compute_delayed(int i, int ms, Map<Integer,Integer> compute_counts) {
    synchronized(compute_counts) {
      if (!compute_counts.containsKey(i)) {
        compute_counts[i] = 1;
      } else {
        compute_counts[i]++;
      }
    }
    Thread.sleep(ms);
    return i;
  }
}

