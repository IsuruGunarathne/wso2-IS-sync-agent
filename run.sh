# Delete the existing wso2is-7.0.0 folder
rm -rf wso2is-7.0.0

# Extract the ZIP file
# rc7
unzip wso2is-7.0.0.zip

# Copy the wso2is-7.0.0 folder in the  extracted folder to the destination directory

# rc7
# cp -r wso2is-7.0.0-rc7/wso2is-7.0.0 .

# Remove the extracted folder
# rc7
# rm -rf wso2is-7.0.0

echo "Extraction and copying completed."

cd readagent

JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64 mvn clean install

cp target/readagent-1.0-SNAPSHOT.jar ../wso2is-7.0.0/repository/components/dropins/

cd ..

# copy libraries from libraries directory to the lib folder
cp libraries/* wso2is-7.0.0/repository/components/lib

./wso2is-7.0.0/bin/wso2server.sh