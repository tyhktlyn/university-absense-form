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

public class Retrieve implements RequestHandler<String, String> {
  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
    // put code here
    final Map<String, String> queryParams = event.getQueryStringParameters();

    final String userName = queryParams.get("userName");
    final String companyName = queryParams.get("companyName");
    final String randomWord = queryParams.get("randomWord");
  }
}