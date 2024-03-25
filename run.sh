cd readagent

mvn clean install

mvn exec:java -Dexec.mainClass="com.readagent.Main"