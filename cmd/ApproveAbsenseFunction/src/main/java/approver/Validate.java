package generator;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Validate {
    private static Logger logger = Logger.getLogger(Validate.class.getName());

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

            switch (c) {
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
        logger.log(Level.INFO, "Uppercase letters " + uplCount + ", Digits " + digit + " and Special Characters "
                + specialChar + ". The total length of the password is " + password.length());

        if (password.length() >= min_length && password.length() <= max_length && uplCount >= min_upp
                && digit >= num_digits && specialChar >= special) {
            logger.log(Level.FINE, "The password passes the requirements necessary - " + password);
            return true;
        } else {
            logger.log(Level.SEVERE, "ERROR: Password not valid");

            if (password.length() < min_length) {
                logger.log(Level.SEVERE, "at least 8 characters");
            } else if (password.length() > max_length) {
                logger.log(Level.SEVERE, "More than 20 characters");
            }

            if (uplCount < min_upp) {
                logger.log(Level.SEVERE, "at least one uppercase letter");
            }

            if (digit < num_digits) {
                logger.log(Level.SEVERE, "at least 2 numeric characters");
            }

            if (specialChar < special) {
                logger.log(Level.SEVERE, "at least 1 special character");
            }

            return false;
        }
    }
}