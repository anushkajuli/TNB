# TNB - Fuse Products

This module contains the logic for working with supported fuse products on both `local machine` and `OpenShift`.

For each product there are two main areas covered:

- deployment - deploying and undeploying product (where applicable)
- integrations - creating, starting, stopping of integrations

The integration code is generated from a "meta" [Integration Builder](src/main/java/software/tnb/product/integration/builder/AbstractIntegrationBuilder.java)
class and 0..x [Customizer](src/main/java/software/tnb/product/customizer/Customizer.java)s for given system-x services using
the [javaparser](https://javaparser.org/) framework. See [RouteBuilders guide](RouteBuilders.md) for more details.

In order to deploy the integration in Openshift, it is possible to implement a specific strategy listed in 
[OpenshiftDeployStrategyType](src/main/java/software/tnb/product/deploystrategy/OpenshiftDeployStrategyType.java)
enum and instantiated by [OpenshiftDeployStrategyFactory](src/main/java/software/tnb/product/deploystrategy/OpenshiftDeployStrategyFactory.java) 
searching for [OpenshiftDeployStrategy](src/main/java/software/tnb/product/deploystrategy/OpenshiftDeployStrategy.java) classes. 
The implementation class must be a [OpenshiftDeployer](src/main/java/software/tnb/product/interfaces/OpenshiftDeployer.java) 
in order to execute all deployment phases. 
You can run the deployment strategy via property `openshift.deploy.strategy`

There are several integration builder classes to use dependending on the use-case:
- [AbstractIntegrationBuilder](src/main/java/software/tnb/product/integration/builder/AbstractIntegrationBuilder.java) serves as a base
for creating integrations on all products (so there are methods related to every product only)
  - it is possible to instantiate it via [IntegrationBuilder](src/main/java/software/tnb/product/integration/builder/IntegrationBuilder.java) class
- AbstractGitIntegrationBuilder
- AbstractMavenGitIntegrationBuilder
- [SpringBootIntegrationBuilder](src/main/java/software/tnb/product/csb/integration/builder/SpringBootIntegrationBuilder.java)
  that extends `AbstractIntegrationBuilder` and adds methods related to camel on springboot only

Customizers are used when the integration should run on all products, but the configuration differs between products. In that case, you need to use
a customizer, where you have access to the IntegrationBuilder and all its methods.

Again, there are multiple customizers you can use:
- [ProductsCustomizer](src/main/java/software/tnb/product/customizer/ProductsCustomizer.java) - when you want to do modifications for two
or more products
- [SpringBootCustomizer](src/main/java/software/tnb/product/csb/customizer/SpringbootCustomizer.java),
[QuarkusCustomizer](src/main/java/software/tnb/product/cq/customizer/QuarkusCustomizer.java)

Instead of creating `new SpringBoot|Quarkus customizers`, you can use
[Customizers](src/main/java/software/tnb/product/customizer/Customizers.java) enum, for example:

```java
Customizers.QUARKUS.customize(ib -> ...)
```

There are also customizer implementations for common modifications needed for a given product. You can check them out in `customizer` sub-package
inside the product's package.


The integrations are created differently for each product:

- `camel on springboot`:
    - an application skeleton is generated from the [archetype](https://github.com/apache/camel-spring-boot/tree/main/archetypes/camel-archetype-spring-boot)
    - the `integration code` is dumped as a `java file` in the app skeleton
- `camel quarkus`:
    - an application skeleton is generated from the `io.quarkus:quarkus-maven-plugin:<version>:create` maven plugin
    - the `integration code` is dumped as a `java file` in the app skeleton

All products are implementing [JUnit 5 extensions](https://junit.org/junit5/docs/current/user-guide/#extensions) so creating a fuse product in your
test is as simple as adding following piece of code:

```java
@RegisterExtension
public static Product product = ProductFactory.create();
```

In this case a correct product instance is determined based on system property `fuse.product` (camelspringboot, camelquarkus)
and based on `openshift.url` property presence (determines if the deployment is local or openshift)

If you want a specific instance of a given fuse product, you can use:

```java
import software.tnb.product.cq.LocalCamelQuarkus;

@RegisterExtension
public static LocalCamelQuarkus cq = ProductFactory.create(LocalCamelQuarkus.class);
```

for example to test features specific to Camel-Quarkus only.
