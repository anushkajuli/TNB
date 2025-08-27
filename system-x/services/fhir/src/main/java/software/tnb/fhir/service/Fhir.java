package software.tnb.fhir.service;

import software.tnb.common.client.NoClient;
import software.tnb.common.deployment.WithDockerImage;
import software.tnb.common.service.ConfigurableService;
import software.tnb.common.validation.NoValidation;
import software.tnb.fhir.account.FhirAccount;
import software.tnb.fhir.service.configuration.FhirConfiguration;

import java.util.Map;

public abstract class Fhir extends ConfigurableService<FhirAccount, NoClient, NoValidation, FhirConfiguration> implements WithDockerImage {
    public static final int PORT = 8080;

    public abstract int getPortMapping();

    public abstract String getServerUrl();

    public Map<String, String> containerEnvironment() {
        return Map.of(
            "FHIR_VERSION", getConfiguration().fhirVersion(),
            "AUTH_ENABLED", Boolean.toString(getConfiguration().getFhirAuthEnabled()),
            "AUTH_USERNAME", account().username(),
            "AUTH_PASSWORD", account().password());
    }

    @Override
    public String defaultImage() {
        return "quay.io/fuse_qe/hapi-fhir-auth:v3.0.1"";
    }

    @Override
    protected void defaultConfiguration() {
        getConfiguration().withFhirVersion("R4");
        getConfiguration().withFhirAuthEnabled(false);
    }
}
