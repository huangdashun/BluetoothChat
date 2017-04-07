package huangshun.it.com.btproject.test;

/**
 * Created by hs on 2017/3/29.
 */

class Insect {
    private static int x1 = printInit("static insect.x1 initialized");
    private int i = 9;
    protected int j;

    public Insect() {
        System.out.println("i = " + i + " . j = " + j);
        j = 39;
    }



    static int printInit(String s) {
        System.out.println(s);
        return 47;
    }
}

public class Beetle extends Insect {
    private static int x2 = printInit("static Beetle.x2 initialized");
    private int k = printInit("Beetle.k initialized");

    public Beetle() {
        System.out.println("k = " + k);
        System.out.println("j = " + j);
    }



    public static void main(String[] args) {
        System.out.println("Beetle constructor");
        Beetle beetle = new Beetle();
    }
    //static insect.x1 initialized
    //static Beetle.x2 initialized
    //Beetle constructor
    //i=9,j=0
    //Beetle.k initialized
    //k=47
    //j=39
}
