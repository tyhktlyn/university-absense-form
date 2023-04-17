package generator;

import java.util.Map;
import java.util.Random;
import java.util.HashMap;

public class Data {
    public String m_Username;
    public String m_CompanyName;
    public String m_RandomWord;
    public String m_Password;

    public Data(String username, String business, String specialWord) {
        m_Username = username;
        m_CompanyName = business;
        m_RandomWord = specialWord;
    }

    public String CreatePass(int symbolAmount, boolean convertToSymbol) {
        char[] symbolsToAdd = new char[] { '!', '"', '$', '%', '^', '&', '*', '(', ')', '_', '-', '+', '/', '@', '#',
                '?' };

        Map<Character, Character> symbolsToChange = new HashMap<Character, Character>();

        // what this does is create a hash table of letters and the numbers that look
        // like the letters
        symbolsToChange = CreateSymbolChange(symbolsToChange);

        Random rand = new Random(); // instance of random class
        int upperbound = 1998;
        int int_random = rand.nextInt(upperbound - 999);

        // bus = business and what this does is it makes the word you entered title case
        // (each word capitalized), then it removes all spaces from the word
        String Bus = SentenceCase(m_CompanyName).replace(" ", "");
        String randomWord = SentenceCase(m_RandomWord).replace(" ", "");

        // the is the password that will be be returned but it is conjoined because you
        // have to have numbers and symbols to it
        // this concantinates the pass with the business name then adds the number to it
        // as a string
        String conjoinedString = Bus.concat(randomWord) + String.valueOf(int_random);

        // this converts the letters to numbers
        if (convertToSymbol) {
            char[] conjoinedChars = conjoinedString.toCharArray();
            for (int i = 0; i < conjoinedString.length(); i++) {

                char charToSympol = Character.toLowerCase(conjoinedString.charAt(i));
                if (symbolsToChange.containsKey(charToSympol)) {
                    conjoinedChars[i] = symbolsToChange.get(charToSympol);
                }
            }
            conjoinedString = String.valueOf(conjoinedChars);
        }

        int max = symbolsToAdd.length;
        switch (symbolAmount) {
            case 1:
                conjoinedString = symbolsToAdd[rand.nextInt(max)] + conjoinedString;
                break;
            case 2:
                conjoinedString = conjoinedString + symbolsToAdd[rand.nextInt(max)];
                break;
            case 3:
                conjoinedString = symbolsToAdd[rand.nextInt(max)] + conjoinedString + symbolsToAdd[rand.nextInt(max)];
                break;
            default:
                break;
        }

        return conjoinedString;
    }

    public Map<Character, Character> CreateSymbolChange(Map<Character, Character> symbolsToChange) {
        symbolsToChange.put('a', '4');
        symbolsToChange.put('e', '3');
        symbolsToChange.put('i', '1');
        symbolsToChange.put('o', '0');
        symbolsToChange.put('s', '5');
        return symbolsToChange;
    }

    public static String RemoveSpace(String Word) {
        Word.replace(" ", "");
        return Word;
    }

    public static String ConvertToLower(String Word) {
        Word.toLowerCase();
        return Word;
    }

    public static String SentenceCase(String Word) {
        if (Word == null || Word.isEmpty()) {
            return Word;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : Word.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }

}