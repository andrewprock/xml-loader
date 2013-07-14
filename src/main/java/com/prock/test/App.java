package com.prock.test;

import org.json.XML;
import org.json.JSONML;
import org.json.JSONObject;
import org.json.JSONException;

import java.net.UnknownHostException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 *
 */
public class App 
{
    public static int PRETTY_PRINT_INDENT_FACTOR = 4;

    // Use Java 1.6 for compatibility
    //
    // Read the entire file into a String.  Do not read if file is greater
    // than the given size.
    public static String readFile(String fileName, long maxSize) throws IOException {
        File file = new File(fileName);
        InputStream insputStream = new FileInputStream(file);
        long length = file.length();
        if (length > maxSize)
            throw new IOException("file larger than requested maximum size");

        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int bytesRead = 0;
        while (offset < bytes.length
               && (bytesRead = insputStream.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += bytesRead;
        }
        insputStream.close();
        return new String(bytes);
    }

    // This is the initial pass at converting XML to JSON.
    public static String xmlToJson(String inputString) {
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(inputString);
            return xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
        } catch (JSONException je) {
            System.out.println("Caught JSON exception in xmlToJson:");
            System.out.println(je.toString());
            System.exit(-1);
        }
        return "ERROR"; // unreachable
    }

    // Using deprecated Mongo interface, not the 2.10.0 MongoClient interface.
    public static void storeJson(String json, String host, int port, String database, String inputCollection) {
        try {
            Mongo mongo = new Mongo(host, port);
            DB db = mongo.getDB(database);
            DBCollection collection = db.getCollection(inputCollection);

            // just test our connectivity
            DBObject oneObje = collection.findOne();
            String str = oneObje.toString();
            System.out.println(str);

            // tranform json for insertion
            DBObject parsedObj = (DBObject)JSON.parse(json);
            collection.insert(parsedObj);


        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    // Run the application.
    public void run(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java -cp target/xml-loader-1.0-SNAPSHOT.jar com.prock.test.App <input xml>");
            System.out.println("");
            System.out.println("The file specified by <input xml> will be transformed to JSON and added to a MongoDB");
            System.exit(-1);
        }
            
        try {
            long ONE_MEGABYATE = 1024*1024;
            String xmlInput = readFile(args[0],ONE_MEGABYATE);
            String jsonOutput = xmlToJson(xmlInput);
            System.out.println(jsonOutput);

            // Now add the json data to the database.
            storeJson(jsonOutput, "localhost", 27017, "mydb", "testData");

        } catch (Exception je) {
            System.out.println(je.toString());
        }
    }

    public static void main(String[] args) throws IOException {
        new App().run(args);
    }
}
