package retriever;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Map;
import java.util.HashMap;

public class Retrieve implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
  private static Logger logger = Logger.getLogger(Retrieve.class.getName());

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
    S3Client s3Client = S3Client.create();
    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

    final Map<String, String> queryParams = event.getQueryStringParameters();

    final String userName = queryParams.get("userName");

    List<String> responseBody = RetrieveUserData(userName, s3Client);

    if (checkUserExists(Username + ".csv", s3Client) == null){
      response.setStatusCode(400);
      response.setBody("Bad request: username does not exist!");

      response.setHeaders(new HashMap<String, String>() {{
        put("Access-Control-Allow-Origin", "https://password-generator.tracd-projects.uk");
      }});

      return response;

    }

    response.setStatusCode(200);
    response.setBody(String.join("\n", responseBody));

    response.setHeaders(new HashMap<String, String>() {
      {
        put("Access-Control-Allow-Origin", "https://password-generator.tracd-projects.uk");
      }
    });

    return response;
  }

  public static InputStream GetFileFromS3(String fileName, S3Client s3Client) {
    String bucketName = "password-file-bucket";

    GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();

    try {
      return s3Client.getObject(request);
    } catch (Exception e) {
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.
      logger.log(Level.WARNING, "The call was transmitted successfully, but Amazon S3 couldn't process");
      e.printStackTrace();
    }

    logger.log(Level.FINE, "The call was transmitted successfully and Amazon S3 processed successfully");

    return null;
  }

  public static InputStream checkUserExists(String fileName, S3Client s3Client) {
    String bucketName = "password-file-bucket";

    GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();

    try {
      return s3Client.getObject(request);
    } catch (S3Exception e) {
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.
      System.out.println("this username doesn't exist, proceeding to create a new one");
      return null;
    }
  }

  public static List<String> RetrieveUserData(String Username, S3Client s3Client) {
    String newFileName = Username + ".csv";
    InputStream retrievedFile = GetFileFromS3(newFileName, s3Client);
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
      logger.log(Level.WARNING, "An error occurred.");
      e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "An error occurred. File was not able to close successfully");
        e.printStackTrace();
      }
    }
    return new ArrayList<String>();
  }
} 
