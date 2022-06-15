// put the code for retrieving the password in here
package retriever;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Retrieve implements RequestHandler<String, String> {
  @Override
  public String handleRequest(String input, Context context)
  {
    // put code here
  }
}