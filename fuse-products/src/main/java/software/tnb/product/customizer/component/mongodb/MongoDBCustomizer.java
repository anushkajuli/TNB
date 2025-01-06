package software.tnb.product.customizer.component.mongodb;

import software.tnb.common.config.TestConfiguration;
import software.tnb.product.customizer.ProductsCustomizer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

public class MongoDBCustomizer extends ProductsCustomizer {
    private final String replicaSetUrl;

    public MongoDBCustomizer(String replicaSetUrl) {
        this.replicaSetUrl = replicaSetUrl;
    }

    @Override
    public void customizeQuarkus() {
        getIntegrationBuilder().addToApplicationProperties("quarkus.mongodb.connection-string", replicaSetUrl);
    }

    /**
     * getApplicationProperties().put("spring.data.mongodb.uri", replicaSetUrl);
     * could be used, but I lost so much time doing this method, I will use it, they both work.
     */
    @Override
    public void customizeSpringboot() {
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.setPackageDeclaration(TestConfiguration.appGroupId());

        compilationUnit.addImport("com.mongodb.client.MongoClients");
        compilationUnit.addImport("com.mongodb.client.MongoClient");
        compilationUnit.addImport("org.springframework.context.annotation.Bean");
        compilationUnit.addImport("org.springframework.context.annotation.Configuration");

        ClassOrInterfaceDeclaration mongoDBConfigurationClassDeclaration = compilationUnit.addClass("MongoDBConfiguration").setPublic(true);

        mongoDBConfigurationClassDeclaration.addAnnotation("Configuration");

        MethodDeclaration mongoClientMethodDeclaration = mongoDBConfigurationClassDeclaration.addMethod("mongoClient", Modifier.Keyword.PUBLIC);
        mongoClientMethodDeclaration.setType("MongoClient");
        mongoClientMethodDeclaration.addAnnotation("Bean");
        BlockStmt methodBody = new BlockStmt();
        methodBody.addStatement(String.format("return MongoClients.create(\"%s\");", replicaSetUrl));

        mongoClientMethodDeclaration.setBody(methodBody);

        getIntegrationBuilder().addClass(compilationUnit);
    }
}
