package com.probuild.automation.workers;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

abstract class WorkerSupport {

    protected Map<String, Object> variables(final ActivatedJob job) {
        return new HashMap<>(job.getVariablesAsMap());
    }

    protected void complete(final JobClient client, final ActivatedJob job, final Map<String, Object> variables) {
        variables.put("lastCompletedJobType", job.getType());
        variables.put("lastCompletedAt", Instant.now().toString());

        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .join();
    }

    protected void complete(final JobClient client, final ActivatedJob job) {
        complete(client, job, variables(job));
    }

    protected void fail(final JobClient client, final ActivatedJob job, final Exception exception) {
        int retries = Math.max(0, job.getRetries() - 1);

        client.newFailCommand(job.getKey())
                .retries(retries)
                .errorMessage(exception.getMessage())
                .send()
                .join();
    }

    protected boolean bool(final Map<String, Object> variables, final String key, final boolean defaultValue) {
        Object value = variables.get(key);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    protected double number(final Map<String, Object> variables, final String key, final double defaultValue) {
        Object value = variables.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : defaultValue;
    }

    protected String text(final Map<String, Object> variables, final String key, final String defaultValue) {
        Object value = variables.get(key);
        return value == null ? defaultValue : value.toString();
    }

    /**
     * Shared by any worker that just needs to flip a single boolean "notice sent"
     * flag and complete the job. Previously duplicated identically in
     * RetailSalesWorkers and ToolHireWorkers.
     */
    protected void sendNotice(final JobClient client, final ActivatedJob job, final String key) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put(key, true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }
}
