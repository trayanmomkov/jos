package info.trekto.jos.core;

import info.trekto.jos.core.numbers.Number;

public interface ScientificConstants {
    long NANOSECONDS_IN_ONE_SECOND = 1000 * 1000 * 1000;
    long MILLISECONDS_IN_ONE_SECOND = 1000;
    long NANOSECONDS_IN_ONE_MILLISECOND = 1000 * 1000;
    long MILLI_IN_DAY = 24 * 60 * 60 * MILLISECONDS_IN_ONE_SECOND;
    long MILLI_IN_HOUR = 60 * 60 * MILLISECONDS_IN_ONE_SECOND;
    long MILLI_IN_MINUTE = 60 * MILLISECONDS_IN_ONE_SECOND;
    
    Number getGravity();

    void setGravity(Number gravity);

    Number getPi();

    void setPi(Number pi);
}
