package com.probuild.automation.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReportingWorkers extends WorkerSupport {

    @JobWorker(type = "upload-exceptions-report")
    public void uploadExceptionsReport(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("exceptionsReportUploaded", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }
}
