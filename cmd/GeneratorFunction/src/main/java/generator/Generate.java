package generator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;

public class Generate implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
    final S3Client s3Client = S3Client.create();

    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

    final Map<String, String> queryParams = event.getQueryStringParameters();

    final String userName = queryParams.get("userName");
    final String companyName = queryParams.get("companyName");
    final String randomWord = queryParams.get("randomWord");
    Data usersInfo = new Data(userName, companyName, randomWord);

    int symbolAmount = randomNumberGenerator();

    usersInfo.m_Password = usersInfo.CreatePass(symbolAmount, new Random().nextBoolean());

    boolean isValid = Validate.validatePassword(usersInfo.m_Password);

    while (!isValid) {
      usersInfo.m_Password = usersInfo.CreatePass(symbolAmount, new Random().nextBoolean());
      isValid = Validate.validatePassword(usersInfo.m_Password);
    }

    try {
      WriteToFile(usersInfo, s3Client);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    response.setStatusCode(200);
    response.setBody("successfully generated your password: " + usersInfo.m_Password);
    response.setHeaders(new HashMap<String, String>() {{
      put("Access-Control-Allow-Origin", "https://password-generator.tracd-projects.uk");
    }});

    return response;
  }

  public static int randomNumberGenerator() {
    int[] numbers = { 1, 2, 3 };

    int index = new Random().nextInt(numbers.length);
    return numbers[index];
  }

  public static void UploadFileToS3(String fileName, File fileContents, S3Client s3Client) {
    String bucketName = "password-file-bucket";

    PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(fileName).build();

    try {
      s3Client.putObject(request, RequestBody.fromFile(fileContents));
      s3Client.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static InputStream checkFileExists(String fileName, S3Client s3Client) {
    String bucketName = "password-file-bucket";

    GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();

    try {
      return s3Client.getObject(request);
    } catch (NoSuchKeyException e) {
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.
      System.out.println("file doesn't exist, proceeding to create a new one");
      return null;
    }
  }

  public static void WriteToFile(Data infoToSave, S3Client s3Client) throws FileNotFoundException {
    String newFileName = infoToSave.m_Username + ".csv";
    File passwordFile = new File("/tmp/" + newFileName);
    //check if file already exists in s3 bucket store
    InputStream fileContents = checkFileExists(newFileName, s3Client);

    try {
      if (fileContents == null) {
        try (PrintWriter out = new PrintWriter(passwordFile)) {
          System.out.println("File created: " + passwordFile.getName());
          out.printf("%s, %s, %s\n", "Business", "Special Word", "Password");
          out.flush();
        }
      } else {
        System.out.println("File already exists, so will append to existing file");
        OutputStream outputStream = new FileOutputStream(passwordFile);
        IOUtils.copy(fileContents, outputStream);
      }
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }

    try (FileWriter f = new FileWriter(passwordFile, true);
        BufferedWriter b = new BufferedWriter(f);
        PrintWriter p = new PrintWriter(b);) {

      p.printf("%s, %s, %s\n", infoToSave.m_CompanyName, infoToSave.m_RandomWord, infoToSave.m_Password);
      p.flush();
      System.out.println("Data added");
    } catch (IOException i) {
      i.printStackTrace();
    }

    UploadFileToS3(newFileName, passwordFile, s3Client);
  }
}