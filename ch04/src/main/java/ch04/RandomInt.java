package ch04;

import java.util.Random;

public class RandomInt {
    static Integer randomInt() {
        return new Random(System.currentTimeMillis()).nextInt();
    }
}
