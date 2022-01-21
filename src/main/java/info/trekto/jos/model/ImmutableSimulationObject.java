package info.trekto.jos.model;

/**
 * @author Trayan Momkov
 * 19 Aug 2015
 */
public interface ImmutableSimulationObject {
    double getX();

    double getY();

    double getZ();
    double getSpeedX();

    double getSpeedY();

    double getSpeedZ();

    double getRadius();

    int getColor();

    double getMass();

    String getId();
}
