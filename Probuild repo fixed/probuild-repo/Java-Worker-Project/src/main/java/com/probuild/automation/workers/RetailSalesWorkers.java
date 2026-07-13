package com.probuild.automation.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RetailSalesWorkers extends WorkerSupport {

    @JobWorker(type = "apply-discount-band-up-to-10")
    public void applyDiscountBand(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            double orderTotal = number(variables, "orderTotal", 100.00);
            double discountRate = Math.min(number(variables, "discountRate", 0.10), 0.10);
            variables.put("discountApplied", orderTotal * discountRate);
            variables.put("discountRate", discountRate);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "accrue-loyalty-points-on-trade-card")
    public void accrueLoyaltyPoints(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            int points = (int) Math.floor(number(variables, "orderTotal", 100.00));
            variables.put("loyaltyPointsAccrued", points);
            variables.put("tradeCardUpdated", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "record-transaction-goods-released-to-customer")
    public void recordTransaction(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("transactionRecorded", true);
            variables.put("goodsReleased", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "apply-trade-card-loyalty-points")
    public void applyTradeCardPoints(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("tradeCard", true);
            variables.put("tradeCardPointsApplied", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "process-payment-cash-card")
    public void processRetailPayment(final JobClient client, final ActivatedJob job) {
        processPayment(client, job, "retailPaymentSuccessful");
    }

    @JobWorker(type = "send-order-confirmation")
    public void sendOrderConfirmation(final JobClient client, final ActivatedJob job) {
        sendNotice(client, job, "orderConfirmationSent");
    }

    @JobWorker(type = "send-payment-failure-notice")
    public void sendPaymentFailureNotice(final JobClient client, final ActivatedJob job) {
        sendNotice(client, job, "retailPaymentFailureNoticeSent");
    }

    @JobWorker(type = "notify-customer-out-of-stock")
    public void notifyCustomerOutOfStock(final JobClient client, final ActivatedJob job) {
        sendNotice(client, job, "outOfStockNoticeSent");
    }

    private void processPayment(final JobClient client, final ActivatedJob job, final String resultKey) {
        try {
            Map<String, Object> variables = variables(job);
            boolean paymentSuccessful = bool(variables, "paymentSuccessful", true);
            variables.put(resultKey, paymentSuccessful);
            variables.put("paymentOk", paymentSuccessful);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }
}
