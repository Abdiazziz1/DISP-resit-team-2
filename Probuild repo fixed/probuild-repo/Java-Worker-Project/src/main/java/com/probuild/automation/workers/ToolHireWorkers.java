package com.probuild.automation.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class ToolHireWorkers extends WorkerSupport {

    @JobWorker(type = "validate-hire-request")
    public void validateHireRequest(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("hireRequestValid", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "create-provisional-booking")
    public void createProvisionalBooking(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("bookingReference", "PB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            variables.put("bookingStatus", "PROVISIONAL");
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "process-payment-or-deposit")
    public void processPaymentOrDeposit(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            boolean paymentSuccessful = bool(variables, "paymentSuccessful", true);
            variables.put("paymentOk", paymentSuccessful);
            variables.put("depositProcessed", paymentSuccessful);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "generate-booking-confirmation")
    public void generateBookingConfirmation(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("bookingConfirmationGenerated", true);
            variables.put("confirmed", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "send-booking-confirmation-to-customer")
    public void sendBookingConfirmation(final JobClient client, final ActivatedJob job) {
        sendNotice(client, job, "bookingConfirmationSent");
    }

    @JobWorker(type = "send-unavailability-notice-to-customer")
    public void sendUnavailabilityNotice(final JobClient client, final ActivatedJob job) {
        sendNotice(client, job, "unavailabilityNoticeSent");
    }

    @JobWorker(type = "send-payment-failure-message")
    public void sendPaymentFailureMessage(final JobClient client, final ActivatedJob job) {
        sendNotice(client, job, "hirePaymentFailureMessageSent");
    }

    @JobWorker(type = "send-repair-approval-to-fixpro")
    public void sendRepairApproval(final JobClient client, final ActivatedJob job) {
        sendNotice(client, job, "repairApprovalSentToFixPro");
    }

    @JobWorker(type = "send-repair-rejection-to-fixpro")
    public void sendRepairRejection(final JobClient client, final ActivatedJob job) {
        sendNotice(client, job, "repairRejectionSentToFixPro");
    }
}
