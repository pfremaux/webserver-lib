package test;

public class Color {

    // Sous Linux = "\027[34m" = 27 est decimal
    // Sous Windows = "\u001B[34m" = 1B est Hexa et vaut 27 en decimal

    public String clear() {
        return "\u001B[0m";
    }

    public String underline() {
        return "\u001B[4m";
    }

    public String stroke() {
        return "\u001B[9m";
    }

    public String doubleUnderline() {
        return "\u001B[21m";
    }

    public String blink() {
        return "\u001B[5m";
    }

    public String italic() {
        return "\u001B[3m";
    }

    public String red() {
        return "\u001B[31m";
    }

    public String blue() {
        return "\u001B[34m";
    }

    public String red2() {
        return "\u001B[35m";
    }

    public String green() {
        return "\u001B[32m";
    }

    public String green2() {
        return "\u001B[36m";
    }

    public String yellow() {
        return "\u001B[33m";
    }

    public String black() {
        return "\u001B[30m";
    }

    public String grey() {
        return "\u001B[2m";
    }

    public String bgRed() {
        return "\u001B[41m";
    }

    public String bgGreen() {
        return "\u001B[42m";
    }

    public String bgYellow() {
        return "\u001B[43m";
    }

    public String bgBlue() {
        return "\u001B[44m";
    }

    public String bgRed2() {
        return "\u001B[45m";
    }

    public String bgGreen2() {
        return "\u001B[46m";
    }

    public String bgWhite() {
        return "\u001B[47m";
    }

    public static void main(String[] args) {
      Color color = new Color();
      System.out.println(color.underline() + color.green()+ "Test");
      System.out.println(color.italic() + color.green2()+ "Test");
    }
}
