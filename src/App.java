import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class App {

    private static final int LIMIT = 10000000;
    private static List<Integer> primes = Collections.synchronizedList(new ArrayList<>());
    private static ReentrantLock lock = new ReentrantLock();
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the upper limit (e.g., 10000000): ");
        int upperLimit = scanner.nextInt();
        
        System.out.print("Enter the number of threads to use: ");
        int numThreads = scanner.nextInt();

        long startTime = System.nanoTime();

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        int rangeSize = upperLimit / numThreads;
        
        for (int i = 0; i < numThreads; i++) {
            int start = i * rangeSize + 1;
            int end = (i == numThreads - 1) ? upperLimit : (i + 1) * rangeSize;
            executorService.execute(new PrimeTask(start, end));
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;

        System.out.printf("%d primes were found.\n", primes.size());
        System.out.printf("Time taken: %d ms\n", duration);
    }

    public static boolean checkPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    public static class PrimeTask implements Runnable {
        private final int start;
        private final int end;

        public PrimeTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            for (int i = start; i <= end; i++) {
                if (checkPrime(i)) {
                    lock.lock();
                    try {
                        primes.add(i);
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }
    }
}
