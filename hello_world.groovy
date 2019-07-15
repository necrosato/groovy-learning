class Test {
  static void main(String[] args) {
    int x = 10;
    println("Hello World ${x}");
    def t = new Tuple<String, Integer>("Hello", 100);
    println(t);
    println(t[0]);
  }
}
