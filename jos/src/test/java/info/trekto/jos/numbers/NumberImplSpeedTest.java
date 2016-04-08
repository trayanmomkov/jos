package info.trekto.jos.numbers;

public class NumberImplSpeedTest {
    public static final long N = 10000000;

    private static Number n1;
    private static Number n2;

    // @Test
    public void testPerformance() {
        // System.out.println(n1.compareTo(n2));
        // System.out.println(n2.compareTo(n1));
        // System.out.println(n1.compareTo(n1));

        // /** Set implementation for number generated by the factory */
        // NumberFactoryProxy.number = new NumberDoubleImpl(0);

        long[] times = new long[10];
        for (int j = 0; j < times.length; j++) {
            n1 = New.num(3.4);
            n2 = New.num(3.5);
            long startTime = System.nanoTime();
            for (int i = 0; i < N; i++) {
                calculate();
                // n1 = n1.multiply(n2);
                // n1 = n1.divide(NumberFactoryProxy.createNumber(10));
            }
            long diff = System.nanoTime() - startTime;
            times[j] = diff;
            System.out.print((diff / 1000 / 1000) + "ms");
            System.out.println("\t\t" + n1 + n2);
        }

        long sum = 0;
        for (long time : times) {
            sum += time;
        }
        System.out.println("Average: " + (sum / times.length / 1000 / 1000));
    }

    private static void calculate() {
        n1 = n1.multiply(n2);
        n1 = n1.divide(New.num(10));
    }
}
