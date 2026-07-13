package com.probuild.automation.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FinanceWorkers extends WorkerSupport {

    @JobWorker(type = "send-real-time-credit-application-to-fintrust-uk")
    public void sendCreditApplicationToFinTrust(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("creditApplicationSent", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "run-real-time-credit-check-on-customer")
    public void runRealTimeCreditCheck(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            String decision = text(variables, "financeDecision", "approved");
            boolean approvedDecision = "approved".equalsIgnoreCase(decision);
            variables.put("financeDecision", approvedDecision ? "approved" : "declined");
            variables.put("approved", approvedDecision);
            variables.put("declined", !approvedDecision);
            variables.put("creditChecked", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "send-approval-or-rejection-to-probuild")
    public void sendApprovalOrRejectionToProBuild(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            String decision = text(variables, "financeDecision", "approved");
            boolean approvedDecision = "approved".equalsIgnoreCase(decision);
            variables.put("approved", approvedDecision);
            variables.put("declined", !approvedDecision);
            variables.put("financeDecisionSentToProBuild", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "send-approval-confirmation-to-customer")
    public void sendApprovalConfirmationToCustomer(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("financeApprovalConfirmationSent", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "pay-probuild-full-purchase-amount-upfront")
    public void payProBuildFullPurchaseAmount(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("finTrustPaidProBuild", true);
            variables.put("purchaseAmountPaid", number(variables, "orderTotal", 100.00));
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }
}
