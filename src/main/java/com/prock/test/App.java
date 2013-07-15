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
    private static int PRETTY_PRINT_INDENT_FACTOR = 4;

    private Mongo mongo;
    private DB db;
    private DBCollection collection;

    /**
     * Simple App for inserting xml into mongodb as json.
     *
     * We maintain one database connection per invocation of the App.
     *
     * NOTE: using deprecated Mongo interface, not the 2.10.0 MongoClient
     * interface.
     */
    public App(String host, int port, String database, String inputCollection) {
        try {
            this.mongo = new Mongo(host, port);
            this.db = mongo.getDB(database);
            this.collection = db.getCollection(inputCollection);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Read the entire file into a String.  Do not read if file is greater
     * than the given size.
     *
     * @param filenName         file which contains the xml
     * @param maxSize           maximum number of bytes to read
     *
     * Use Java 1.6 for compatibility
     */
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

    /**
     * Convert XML to JSON
     *
     * @param inputString       xml imput
     * @returns                 json output
     */
    public static String xmlToJson(String inputString) throws JSONException {
        JSONObject xmlJSONObj = XML.toJSONObject(inputString);
        return xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
    }

    /**
     * Store a document in MongoDB as a document.
     *
     * @param json              document to store
     *
     * The document is stored as is, no processsing or structure is added.
     * No validation is done to ensure that the json is valid.
     */
    public void storeDocument(String json) {
        DBObject parsedObj = (DBObject)JSON.parse(json);
        this.collection.insert(parsedObj);
    }

    /**
     * Retrieve a document from MondoDB.
     *
     * @param field             the field to query
     * @param value             the value of the field queried
     *
     * MongoDB is quried for a docuemnt with the specified field=value.
     */
    public String retrieveDocument(String field, Object value) {
        BasicDBObject fields = new BasicDBObject();
        fields.put(field, value);

        DBCursor result = this.collection.find(fields);
        String document = result.next().toString();
        System.out.println(document);
        return document;
    }

    /**
     * Driver.
     *
     * @param args      expected to be the command line arguments
     */
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
            storeDocument(jsonOutput);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * Run the project.
     *
     * TODO: The database parameters are hard coded here.  In production
     * environment, there should be a mechanism for specifying them outside
     * of code.
     */
    public static void main(String[] args) throws IOException {
        new App("localhost", 27017, "mydb", "testData").run(args);
    }
}
