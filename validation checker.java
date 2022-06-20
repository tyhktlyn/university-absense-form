import java.util.Scanner;
public class validatePass{

public static void main(String[] args) {
    final int pass_length = 8;
    final int num_digits = 2;
    final int special = 1;
    final int min_upp = 1;
    
    int min = 8;
    int max = 20;
    int digit = 0;
    int specialChar = 0;
    int uplCount = 0;
    int lolCount = 0;
    String pass;
     

    // could be changed to just retiving the geenerated password
    Scanner scan = new Scanner(System.in);
    System.out.println("Enter Your Password:");
    pass = scan.nextLine();
    
    for(int i = 0; i < pass.length(); i++){
         char c = pass.charAt(i);
         if(Character.isUpperCase(c)){
                uplCount++;
            }
    
         if(Character.isDigit(c)){
                digit++;
            }
            
         if (c == '$' || c == '#' || c == '?' || c == '!' || c == '_'|| c == '=' || c == '%') {
            specialChar++;
         }
         
        }
    
   if (pass.length() >= max && uplCount >= min_upp && digit >= num_digits && special >= specialChar) { 
                    System.out.println("The password is valid");
             }
             else {
       System.out.println("Your password does not contain:");
        if(pass.length() < min)
            System.out.println("at least 8 characters");

        if (uplCount < min_upp) 
            System.out.println("at least one uppercase letter");
            
        if(digit < num_digits) 
            System.out.println("at least 2 numeric characters");
            
        if(specialChar < special) 
            System.out.println("at least 1 special character");
        }
            }
            }