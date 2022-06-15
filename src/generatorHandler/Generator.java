// put the code for generating the password in here
package generator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Generate implements RequestHandler<String, String> {
  @Override
  public String handleRequest(String input, Context context)
  {
    // put code here
  }
}