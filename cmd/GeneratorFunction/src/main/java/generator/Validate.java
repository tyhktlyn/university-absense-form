package generator;

public class Validate {
    public static boolean validatePassword(String password) {
        final int num_digits = 2;
        final int special = 1;
        final int min_upp = 1;

        int min_length = 8;
        int max_length = 20;
        int digit = 0;
        int specialChar = 0;
        int uplCount = 0;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                uplCount++;
            }

            if (Character.isDigit(c)) {
                digit++;
            }

            switch(c) {
                case '$':
                    specialChar++;
                    break;
                case '#':
                    specialChar++;
                    break;
                case '?':
                    specialChar++;
                    break;
                case '!':
                    specialChar++;
                    break;
                case '_':
                    specialChar++;
                    break;
                case '%':
                    specialChar++;
                    break;
                case '"':
                    specialChar++;
                    break;
                case '^':
                    specialChar++;
                    break;
                case '&':
                    specialChar++;
                    break;
                case '*':
                    specialChar++;
                    break;
                case '(':
                    specialChar++;
                    break;
                case ')':
                    specialChar++;
                    break;
                case '-':
                    specialChar++;
                    break;
                case '+':
                    specialChar++;
                    break;
                case '/':
                    specialChar++;
                    break;
                case '@':
                    specialChar++;
                    break;
            }
        }

        if (password.length() >= min_length && password.length() <= max_length && uplCount >= min_upp && digit >= num_digits && special >= specialChar) {
            return true;
        } else {
            System.out.println("Your password (" + password + ") does not contain:");
            if (password.length() < min_length) {
                System.out.println("at least 8 characters");
            }

            if (uplCount < min_upp) {
                System.out.println("at least one uppercase letter");
            }

            if (digit < num_digits) {
                System.out.println("at least 2 numeric characters");
            }

            if (specialChar < special) {
                System.out.println("at least 1 special character");
            }

            return false;
        }
    }
}