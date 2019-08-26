package inheritance;
import inheritance.*;

class InteritanceTest {
  public static void main(String[] args) {
    new Foo()
    new Bar()
  }
}

class Foo {
  Foo() {
    println("Initializing Foo");
  }
}

class Bar extends Foo {
  Bar() {
    println("Initializing Bar");
  }
}
