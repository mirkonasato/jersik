package io.encoded.jersik.java.testclient;

import static org.junit.Assert.assertEquals;
import io.encoded.jersik.runtime.JsonCodec;
import io.encoded.jersik.runtime.ObjectCodec;
import io.encoded.jersik.testsuite.*;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RoundtripTest {

    private static class Operation {

        private final TestServiceClient serviceClient;
        private final String operationName;
        private final Class<?> requestClass;

        public Operation(TestServiceClient serviceClient, String operationName, Class<?> requestClass) {
            this.serviceClient = serviceClient;
            this.operationName = operationName;
            this.requestClass = requestClass;
        }

        public Object invoke(Object request) throws Exception {
            Method method = serviceClient.getClass().getMethod(operationName, requestClass);
            return method.invoke(serviceClient, request);
        }
    }

    private static final URI SERVICE_URI = URI.create("http://localhost:8080/test/");
    private static final TestServiceClient SERVICE_CLIENT = new TestServiceClient(HttpClients.createDefault(), SERVICE_URI);

    private final String fixture;
    private final ObjectCodec<?> objectCodec;
    private final Operation operation;


    public RoundtripTest(String fixture, ObjectCodec<?> objectCodec, Operation operation) {
        this.fixture = fixture;
        this.objectCodec = objectCodec;
        this.operation = operation;
    }

    @Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            { "Empty.json", EmptyCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testEmpty", Empty.class) },
            { "ScalarFieldsA.json", ScalarFieldsCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testScalarFieldsA", ScalarFields.class) },
            { "ScalarFieldsB.json", ScalarFieldsCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testScalarFieldsB", ScalarFields.class) },
            { "ScalarFieldsC.json", ScalarFieldsCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testScalarFieldsC", ScalarFields.class) },
            { "OptionalFieldsA.json", OptionalFieldsCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testOptionalFieldsA", OptionalFields.class) },
            { "OptionalFieldsB.json", OptionalFieldsCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testOptionalFieldsB", OptionalFields.class) },
            { "ListFieldsA.json", ListFieldsCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testListFieldsA", ListFields.class) },
            { "ListFieldsB.json", ListFieldsCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testListFieldsB", ListFields.class) },
            { "MapFieldsA.json", MapFieldsCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testMapFieldsA", MapFields.class) },
            { "MapFieldsB.json", MapFieldsCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testMapFieldsB", MapFields.class) },
            { "StructFieldsA.json", StructFieldsCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testStructFieldsA", StructFields.class) },
            { "StructFieldsB.json", StructFieldsCodec.INSTANCE, new Operation(SERVICE_CLIENT, "testStructFieldsB", StructFields.class) }
        });
    }

    @Test
    public void roundtripTest() throws Exception {
        String fileName = "../../test-suite/fixtures/"+ fixture;
        Object request = JsonCodec.INSTANCE.decode(new FileInputStream(fileName), objectCodec);
        Object response = operation.invoke(request);
        assertEquals(request, response);        
    }

}
