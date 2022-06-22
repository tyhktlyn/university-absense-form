package retriever;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Map;

public class Retrieve implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

    final Map<String, String> queryParams = event.getQueryStringParameters();

    final String userName = queryParams.get("userName");

    List<String> responseBody = RetrieveUserData(userName);

    response.setStatusCode(200);
    response.setBody(String.join("\n", responseBody));

    System.out.println(response.getBody());

    return response;
  }

  public static InputStream GetFileFromS3(String fileName) {
    String bucketName = "password-file-bucket";
    String clientRegion = "eu-west-2";

    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        .withRegion(clientRegion)
        .build();

    GetObjectRequest request = new GetObjectRequest(bucketName, fileName);

    try {
      return s3Client.getObject(request).getObjectContent();
    } catch (Exception e) {
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.
      e.printStackTrace();
    }

    return null;
  }

  public static List<String> RetrieveUserData(String Username) {
    String newFileName = Username + ".csv";
    InputStream retrievedFile = GetFileFromS3(newFileName);
    BufferedReader reader = null;
    String Line = "";
    List<String> records = new ArrayList<String>();

    try {
      reader = new BufferedReader(new InputStreamReader(retrievedFile));
      while ((Line = reader.readLine()) != null) {
        records.add(Line);
      }
      return records;
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return new ArrayList<String>();
  }
}