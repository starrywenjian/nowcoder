
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PrintABC {
    private static final int TOTAL_PRINT_COUNT = 10;
    private static Lock lock = new ReentrantLock();
    private static Condition conditionA = lock.newCondition();
    private static Condition conditionB = lock.newCondition();
    private static Condition conditionC = lock.newCondition();
    private static volatile int currentCount = 0;

    static class PrintThread extends Thread {
        private String value;
        private Condition currentCondition;
        private Condition nextCondition;

        public PrintThread(String value, Condition currentCondition, Condition nextCondition) {
            this.value = value;
            this.currentCondition = currentCondition;
            this.nextCondition = nextCondition;
        }

        @Override
        public void run() {
            for (int i = 0; i < TOTAL_PRINT_COUNT; i++) {
                lock.lock();
                try {
                    while (currentCount % 3 != (value.equals("A") ? 0 : value.equals("B") ? 1 : 2)) {
                        currentCondition.await();
                    }
                    System.out.print(value);
                    currentCount++;
                    nextCondition.signal();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread threadA = new PrintThread("A", conditionA, conditionB);
        Thread threadB = new PrintThread("B", conditionB, conditionC);
        Thread threadC = new PrintThread("C", conditionC, conditionA);

        threadA.start();
        threadB.start();
        threadC.start();
    }
}
