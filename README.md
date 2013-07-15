This is a simple demonstration project which parses XML into JSON and
stores the resulting document, valid or not, in MongoDB.

The project was generated using maven:

    mvn archetype:generate \
        -DgroupId=com.prock.test \
        -DartifactId=xml-loader \
        -DarchetypeArtifactId=maven-archetype-quickstart \
        -DinteractiveMode=false

To build the project use maven to create the package:

    mvn package

Before running the project, mongodb must be installed and running on the
system.  Once mongodb is installed, no further configuration should be
required.

To run the project execute:

    java -cp target/xml-loader-1.0-SNAPSHOT.jar com.prock.test.App data/test.xml

Running that command will transform the XML contained in the file into
JSON, then insert it into MongoDB.  The MondoDB parameters used are:

    host: localhos
    port: 27017
    db: mydb
    collection: testData

