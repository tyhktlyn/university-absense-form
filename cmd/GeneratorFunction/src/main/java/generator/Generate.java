package generator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Random;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.PrintWriter;

public class Generate implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
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
      WriteToFile(usersInfo);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    response.setStatusCode(200);
    response.setBody("successfully generated your password: " + usersInfo.m_Password);

    return response;
  }

  public static int randomNumberGenerator() {
    int[] numbers = { 1, 2, 3 };

    int index = new Random().nextInt(numbers.length);
    return numbers[index];
  }

  public static void UploadFileToS3(String fileName, File fileContents) {
    String bucketName = "password-file-bucket";
    String clientRegion = "eu-west-2";

    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        .withRegion(clientRegion)
        .build();

    PutObjectRequest request = new PutObjectRequest(bucketName, fileName, fileContents);

    try {
      s3Client.putObject(request);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void WriteToFile(Data infoToSave) throws FileNotFoundException {
    String newFileName = infoToSave.m_Username + ".csv";
    File passwordFile = new File("/tmp/" + newFileName);

    try {
      if (passwordFile.createNewFile()) {
        try (PrintWriter out = new PrintWriter(passwordFile)) {
          System.out.println("File created: " + passwordFile.getName());
          out.printf("%s, %s, %s\n", "Business", "SpecialWord", "Password");
          out.flush();
        }
      } else {
        System.out.println("File already exists.");
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

    UploadFileToS3(newFileName, passwordFile);
  }
}