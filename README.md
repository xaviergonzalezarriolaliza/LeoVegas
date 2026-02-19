# LeoVegas API Test Project

This project demonstrates API testing using Java, Maven, RestAssured, and JUnit. It includes a simple mocked API (using SparkJava) and example test cases.

## Structure
- `src/main/java/com/leovegas/mockapi/MockApiServer.java`: Simple mocked API with `/hello` and `/echo` endpoints.
- `src/test/java/com/leovegas/apitest/MockApiTest.java`: Example RestAssured + JUnit test cases.
- `pom.xml`: Maven configuration with dependencies and Surefire test report plugin.

## Usage

### 1. Start the Mock API Server
Run the main method in `MockApiServer.java` (port 4567):

```
mvn exec:java -Dexec.mainClass="com.leovegas.mockapi.MockApiServer"
```

### 2. Run the Tests
In a separate terminal:

```
mvn test
```

Test reports will be generated in `target/surefire-reports`.

## Requirements
- Java 17+
- Maven 3.8+

---

Feel free to extend the API and add more tests as needed.
