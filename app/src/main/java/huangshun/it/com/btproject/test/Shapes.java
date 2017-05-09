package huangshun.it.com.btproject.test;

/**
 * Created by hs on 2017/5/4.
 */
abstract class Shape {
    void draw() {
        System.out.println(this);
    }

    abstract public String toString();
}

class Circle extends Shape {
    public String toString() {
        return "Circle";
    }
}

class Square extends Shape {
    public String toString() {
        return "Square";
    }
}

class Triangle extends Shape {
    public String toString() {
        return "Triangle";
    }
}

public class Shapes {
    public static void main(String[] args) {
        System.out.println(String.format("http://open-test.bong.cn/v2/goToPlugin?token=%1$s&clientId=1493295955748&groupId=%2$s","fsdafasf",234234));

    }
}