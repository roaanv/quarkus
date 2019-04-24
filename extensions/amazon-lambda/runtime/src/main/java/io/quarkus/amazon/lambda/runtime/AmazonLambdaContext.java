package io.quarkus.amazon.lambda.runtime;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.StringJoiner;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.LambdaRuntime;
import com.fasterxml.jackson.databind.ObjectReader;

public class AmazonLambdaContext implements Context {

    private String awsRequestId;
    private String logGroupName;
    private String logStreamName;
    private String functionName;
    private String functionVersion;
    private String invokedFunctionArn;
    private CognitoIdentity cognitoIdentity;
    private ClientContext clientContext;
    private long runtimeDeadlineMs = 0;
    private int memoryLimitInMB;
    private LambdaLogger logger;

    public AmazonLambdaContext(HttpURLConnection request, ObjectReader cognitoReader, ObjectReader clientCtxReader)
            throws IOException {
        awsRequestId = request.getHeaderField("Lambda-Runtime-Aws-Request-Id");
        logGroupName = System.getenv("AWS_LAMBDA_LOG_GROUP_NAME");
        logStreamName = System.getenv("AWS_LAMBDA_LOG_STREAM_NAME");
        functionName = System.getenv("AWS_LAMBDA_FUNCTION_NAME");
        functionVersion = System.getenv("AWS_LAMBDA_FUNCTION_VERSION");
        invokedFunctionArn = request.getHeaderField("Lambda-Runtime-Invoked-Function-Arn");

        String cognitoIdentityHeader = request.getHeaderField("Lambda-Runtime-Cognito-Identity");
        if (cognitoIdentityHeader != null) {
            cognitoIdentity = cognitoReader.readValue(cognitoIdentityHeader);
        }

        String clientContextHeader = request.getHeaderField("Lambda-Runtime-Client-Context");
        if (clientContextHeader != null) {
            clientContext = clientCtxReader.readValue(clientContextHeader);
        }

        String functionMemorySize = System.getenv("AWS_LAMBDA_FUNCTION_MEMORY_SIZE");
        memoryLimitInMB = functionMemorySize != null ? Integer.valueOf(functionMemorySize) : 0;

        String runtimeDeadline = request.getHeaderField("Lambda-Runtime-Deadline-Ms");
        if (runtimeDeadline != null) {
            runtimeDeadlineMs = Long.valueOf(runtimeDeadline);
        }
        logger = LambdaRuntime.getLogger();
    }

    @Override
    public String getAwsRequestId() {
        return awsRequestId;
    }

    @Override
    public String getLogGroupName() {
        return logGroupName;
    }

    @Override
    public String getLogStreamName() {
        return logStreamName;
    }

    @Override
    public String getFunctionName() {
        return functionName;
    }

    @Override
    public String getFunctionVersion() {
        return functionVersion;
    }

    @Override
    public String getInvokedFunctionArn() {
        return invokedFunctionArn;
    }

    @Override
    public CognitoIdentity getIdentity() {
        return cognitoIdentity;
    }

    @Override
    public ClientContext getClientContext() {
        return clientContext;
    }

    @Override
    public int getRemainingTimeInMillis() {
        return (int) (runtimeDeadlineMs - Math.round(System.nanoTime() / 1_000_000d));
    }

    @Override
    public int getMemoryLimitInMB() {
        return memoryLimitInMB;
    }

    @Override
    public LambdaLogger getLogger() {
        return logger;
    }
}