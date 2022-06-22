package fileHandling;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.File;
import java.io.PrintWriter;

public class FileManipulation {
    public static void UploadFileToS3(String fileName, File fileContents) {
        String bucketName = "password-file-bucket";
        String clientRegion = "eu-west-2";

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
                .withCredentials(new ProfileCredentialsProvider())
                .build();

        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, fileContents);

        try {
            s3Client.putObject(request);
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
    }

    public static InputStream GetFileFromS3(String fileName) {
        String bucketName = "password-file-bucket";
        String clientRegion = "eu-west-2";

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
                .withCredentials(new ProfileCredentialsProvider())
                .build();

        GetObjectRequest request = new GetObjectRequest(bucketName, fileName);

        try {
            return s3Client.getObject(request).getObjectContent();
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }

        return null;
    }

    public static void WriteToFile(Data infoToSave) throws FileNotFoundException {
        String newFileName = infoToSave.m_Username + ".csv";
        File passwordFile = new File(newFileName);

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