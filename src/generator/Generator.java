// put the code for retrieving the password in here
package generator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;
import java.util.HashMap;
import java.util.*;
import java.util.regex.*;

public class Retrieve implements RequestHandler<String, String> {
  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
    // put code here
    final Map<String, String> queryParams = event.getQueryStringParameters();

    final String userName = queryParams.get("userName");
    final String companyName = queryParams.get("companyName");
    final String randomWord = queryParams.get("randomWord");

    public Retrieve(String companyName, String rand){
       this.companyName = companyName;
       this.randomWord = randomWord;
    }

    public String passCompile() {
      String pass="";
        if(randomWord.length() >= 6 && companyName.length() >= 2 ){
            pass = companyName + randomWord;
            //System.out.println(pass);
            if(pass.length() > 15){
                System.out.println("This is not the correct input");
                System.exit(0);;
            } 
        }
        return pass;
    }

    public void passValidator(String p){
        String regex = "^(?=.*[a-z])(?=.*[A-Z]).*$";
        Pattern ps = Pattern.compile(regex);
        Matcher m = ps.matcher(pass);
        if(!m.matches()){
            System.out.println("This is not the correct input");
            System.exit(0);
        }
        int passLength = p.length();
        int newPassLength = passLength + 6;
        String numbers = "1234567890";
        String sc = "-_!@#$^&*?";
        String combinedChars = numbers + sc;
        Random random = new Random();
        char[] passChars = new char[passLength];
        char[] cChars = new char[newPassLength];
        for(int i = 0; i < 8; i++){
            passChars[i] = p.charAt(random.nextInt(p.length()));
        }
        for(int i = 0; i < newPassLength; i++){
            cChars[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
        }
        String pw = String.valueOf(passChars);
        String chars = String.valueOf(cChars);
        String[] charsColl = null;
        ArrayList<String> uniqueChar = new ArrayList<String>();
        charsColl = chars.split("");
        for(String c : charsColl){
            if(!uniqueChar.contains(c)) {
                uniqueChar.add(c);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String s : uniqueChar)
        {
            sb.append(s);
        }
        String finalSpecChars = sb.toString();
        String finalStr = pw + finalSpecChars;
        System.out.println("Password: " + finalStr);
    }

    public static void main(String[] args){
        System.out.println("For Password Generation, the password has to be at least 8 characters long");
        System.out.println("Must contain uppercase and lowercase letters");
        System.out.println("The company name must be at least two characters long");
        System.out.println("The random word must be at least six characters long");
        System.out.println();
        Scanner passGen = new Scanner(System.in);
        System.out.println("Enter Company Name: ");
        String companyName = passGen.nextLine();

        System.out.println("Enter a Random Name: ");
        String randomName = passGen.nextLine();

        Retrieve g = new Retrieve(companyName, randomName);
        String originalPass = g.passCompile();
        g.passValidator(originalPass);
    }
  }
}