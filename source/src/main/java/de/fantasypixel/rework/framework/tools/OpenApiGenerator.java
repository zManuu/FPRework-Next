package de.fantasypixel.rework.framework.tools;

import de.fantasypixel.rework.framework.FPUtils;
import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.web.Authenticated;
import de.fantasypixel.rework.framework.web.Servlet;
import io.smallrye.openapi.internal.models.info.Info;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * This tool is capable of generating the full OpenApi definition for the backend.
 */
public class OpenApiGenerator {

    private static final String OPENAPI_VERSION = "3.1.0";
    private static final String OPENAPI_TITLE = "FPRework OpenAPI";
    private static final String OPENAPI_PROJECT_VERSION = "0.1.0";

    public static void main(String[] args) {
        execute();
    }

    private static void execute() {
        OpenAPI openAPI = OASFactory.createOpenAPI()
                .openapi(OPENAPI_VERSION)
                .info(getInfo())
                .paths(getPaths());
        try {
            String yaml = serializeOpenApi(openAPI);
            writeOpenApi(yaml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Paths getPaths() {
        FPLogger logger = new FPLogger();
        FPUtils utils = new FPUtils(logger);
        Set<Class<?>> servletClasses = utils.getClassesAnnotatedWith(Servlet.class);
        Paths paths = OASFactory.createPaths();
        for (Class<?> servletClass : servletClasses) {
            Servlet servletData = servletClass.getAnnotation(Servlet.class);
            PathItem pathItem = getPathItem(servletClass);
            paths.addPathItem(servletData.value(), pathItem);
        }
        return paths;
    }

    @Nullable
    private static Method getMethod(Class<?> servletClass, String methodName) {
        try {
            return servletClass.getDeclaredMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static PathItem getPathItem(Class<?> servletClass) {
        PathItem pathItem = OASFactory.createPathItem();
        Method doGet = getMethod(servletClass, "doGet");
        if (doGet != null) {
            pathItem.setGET(getOperation(doGet).responses(getApiResponses(doGet)));
        }
        Method doPost = getMethod(servletClass, "doPost");
        if (doPost != null) {
            pathItem.setPOST(getOperation(doPost).responses(getApiResponses(doPost)));
        }
        Method doPut = getMethod(servletClass, "doPut");
        if (doPut != null) {
            pathItem.setPUT(getOperation(doPut).responses(getApiResponses(doPut)));
        }
        Method doDelete = getMethod(servletClass, "doDelete");
        if (doDelete != null) {
            pathItem.setDELETE(getOperation(doDelete).responses(getApiResponses(doDelete)));
        }
        return pathItem;
    }

    private static APIResponses getApiResponses(Method method) {
        APIResponses responses = OASFactory.createAPIResponses();
        // add default responses
        // 500
        org.eclipse.microprofile.openapi.models.responses.APIResponse response500 = OASFactory.createAPIResponse();
        response500.description("Internal server error.");
        responses.addAPIResponse("500", response500);
        // 401 & 403
        if (method.getDeclaringClass().isAnnotationPresent(Authenticated.class)) {
            org.eclipse.microprofile.openapi.models.responses.APIResponse response401 = OASFactory.createAPIResponse();
            response401.description("Not authenticated.");
            responses.addAPIResponse("401", response401);
            org.eclipse.microprofile.openapi.models.responses.APIResponse response403 = OASFactory.createAPIResponse();
            response403.description("Not authorized.");
            responses.addAPIResponse("403", response403);
        }

        APIResponse[] responseAnnotations = method.getAnnotationsByType(APIResponse.class);
        for (APIResponse responseAnnotation : responseAnnotations) {
            org.eclipse.microprofile.openapi.models.responses.APIResponse response = OASFactory.createAPIResponse();
            if (responseAnnotation != null) {
                response.setDescription(responseAnnotation.description());
                for (Content contentAnno : responseAnnotation.content()) {
                    org.eclipse.microprofile.openapi.models.media.Content content = getContent(contentAnno);
                    response.setContent(content);
                }

                responses.addAPIResponse(responseAnnotation.responseCode(), response);
            }
        }
        return responses;
    }

    private static org.eclipse.microprofile.openapi.models.media.@NonNull Content getContent(Content contentAnno) {
        org.eclipse.microprofile.openapi.models.media.Content content = OASFactory.createContent();
        MediaType mediaType = OASFactory.createMediaType();
        Schema schemaAnno = contentAnno.schema();
        org.eclipse.microprofile.openapi.models.media.Schema schema = OASFactory.createSchema();
        if (!schemaAnno.implementation().equals(Void.class)) {
            if (schemaAnno.implementation().equals(String.class)) {
                schema.setType(List.of(org.eclipse.microprofile.openapi.models.media.Schema.SchemaType.STRING));
            } else {
                schema.setType(List.of(org.eclipse.microprofile.openapi.models.media.Schema.SchemaType.OBJECT));
                schema.setRef("#/components/schemas/" + schemaAnno.implementation().getSimpleName());
            }
        }
        mediaType.setSchema(schema);
        mediaType.setExample(contentAnno.example());
        content.addMediaType(contentAnno.mediaType(), mediaType);
        return content;
    }

    private static org.eclipse.microprofile.openapi.models.Operation getOperation(Method method) {
        Operation operationAnno = method.getAnnotation(Operation.class);
        org.eclipse.microprofile.openapi.models.Operation op = OASFactory.createOperation();
        if (operationAnno != null) {
            op.setSummary(operationAnno.summary());
            op.setDescription(operationAnno.description());
        }
        return op;
    }

    private static org.eclipse.microprofile.openapi.models.info.Info getInfo() {
        return new Info()
                .title(OPENAPI_TITLE)
                .version(OPENAPI_PROJECT_VERSION);
    }

    private static String serializeOpenApi(OpenAPI openAPI) throws IOException {
        return OpenApiSerializer.serialize(openAPI, Format.YAML);
    }

    private static void writeOpenApi(String yaml) throws IOException {
        File targetFile = new File("target/generated/openapi.yaml");
        targetFile.createNewFile();
        try (FileWriter fileWriter = new FileWriter(targetFile)) {
            fileWriter.write(yaml);
        }
    }

}
