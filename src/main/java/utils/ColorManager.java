package utils;

public class ColorManager {

    public static int COLOR_COUNT() {
        return Color.values().length;
    }

    public static Color getColor(int colorId) {
        return Color.values()[colorId % COLOR_COUNT()];
    }

    public enum Color {
        Red("\u001b[31;1m"),
        Green("\u001b[32;1m"),
        Yellow("\u001b[33;1m"),
        Blue("\u001b[34;1m"),
        Magenta("\u001b[35;1m"),
        Cyan("\u001b[36;1m"),
        White("\u001b[37;1m"),
        ;
        final String value;

        Color(String s) {
            value = s;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
