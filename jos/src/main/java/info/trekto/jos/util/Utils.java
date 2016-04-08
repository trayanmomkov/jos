package info.trekto.jos.util;

/**
 * @author Trayan Momkov
 * @date 6.03.2016 г.1:54:43
 */
public class Utils {
    public static final int CORES = Runtime.getRuntime().availableProcessors();

    //    public static final int CORES = 1;

    public static void log(Object... objects) {
        for (Object object : objects) {
            log(object.toString());
        }
    }

    public static void log(String... messages) {
        for (String message : messages) {
            System.out.println(message);
        }
    }
}
