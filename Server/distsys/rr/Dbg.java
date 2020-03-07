package distsys.rr;

class Dbg {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void red(String s) {
        System.out.println(ANSI_RED + s + ANSI_RESET);
    }

    public static void green(String s) {
        System.out.println(ANSI_GREEN + s + ANSI_RESET);
    }

    public static void yellow(String s) {
        System.out.println(ANSI_YELLOW + s + ANSI_RESET);
    }

    public static void cyan(String s) {
        System.out.println(ANSI_CYAN + s + ANSI_RESET);
    }

    public static void purple(String s) {
        System.out.println(ANSI_PURPLE + s + ANSI_RESET);
    }

    public static void blue(String s) {
        System.out.println(ANSI_BLUE + s + ANSI_RESET);
    }
}