package com.probuild.automation;

import io.camunda.client.CamundaClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "probuild.deployment", name = "enabled", havingValue = "true")
public class CamundaDeploymentRunner implements CommandLineRunner {

    private final CamundaClient camundaClient;

    public CamundaDeploymentRunner(final CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    @Override
    public void run(final String... args) {
        camundaClient.newDeployResourceCommand()
                .addResourceFromClasspath("probuild-operational-model.bpmn")
                .addResourceFromClasspath("forms/check-tool-availability.form")
                .addResourceFromClasspath("forms/submit-hire-request.form")
                .addResourceFromClasspath("forms/place-retail-order.form")
                .addResourceFromClasspath("forms/payment.form")
                .addResourceFromClasspath("forms/finance-application.form")
                .addResourceFromClasspath("forms/approve-repair-request.form")
                .addResourceFromClasspath("forms/fixpro-service-report.form")
                .send()
                .join();

        System.out.println("Deployed ProBuild BPMN model and Camunda Forms.");
    }
}
