package com.probuild.automation.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FixProWorkers extends WorkerSupport {

    @JobWorker(type = "classify-service-type")
    public void classifyServiceType(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            String serviceType = text(variables, "serviceType", "standard");
            boolean majorRepair = "major".equalsIgnoreCase(serviceType)
                    || "major-repair".equalsIgnoreCase(serviceType)
                    || bool(variables, "majorRepair", false);
            variables.put("serviceType", majorRepair ? "majorRepair" : "standard");
            variables.put("majorRepair", majorRepair);
            variables.put("standardService", !majorRepair);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "request-major-repair-authorisation-from-probuild")
    public void requestMajorRepairAuthorisation(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("majorRepairAuthorisationRequested", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }
}
