package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.ScientificConstants;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;

/**
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class ScientificConstantsAP implements ScientificConstants {
    private Number gravity;
    private Number pi;

    public ScientificConstantsAP() {
        setGravity(New.num("0.00000000006674")); // 6.674×10^−11 N⋅m2/kg2
        setPi(New.num("3.1415926535897932384626433832795028841971693993751058209749445923078164062862"));
    }

    @Override
    public Number getGravity() {
        return gravity;
    }

    @Override
    public void setGravity(Number gravity) {
        this.gravity = gravity;
    }

    @Override
    public Number getPi() {
        return pi;
    }

    @Override
    public void setPi(Number pi) {
        this.pi = pi;
    }
}
