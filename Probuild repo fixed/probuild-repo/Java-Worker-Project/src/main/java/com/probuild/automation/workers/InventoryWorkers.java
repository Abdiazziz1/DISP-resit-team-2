package com.probuild.automation.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryWorkers extends WorkerSupport {

    @JobWorker(type = "check-tool-availability-in-ims")
    public void checkToolAvailability(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            boolean available = bool(variables, "toolAvailable", true);
            variables.put("available", available);
            variables.put("confirmed", available);
            variables.put("unavailable", !available);
            variables.put("imsToolAvailabilityChecked", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "check-stock-availability-in-ims")
    public void checkStockAvailability(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            boolean inStock = bool(variables, "stockAvailable", true);
            variables.put("stockAvailable", inStock);
            variables.put("standard", !bool(variables, "tradeCard", false));
            variables.put("tradeCard", bool(variables, "tradeCard", false));
            variables.put("imsStockChecked", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "update-tool-status-returned-in-inspection")
    public void updateReturnedInspection(final JobClient client, final ActivatedJob job) {
        updateStatus(client, job, "RETURNED_IN_INSPECTION");
    }

    @JobWorker(type = "update-tool-status-hired")
    public void updateStatusHired(final JobClient client, final ActivatedJob job) {
        updateStatus(client, job, "HIRED");
    }

    @JobWorker(type = "update-tool-status-ready-to-hire")
    public void updateStatusReadyToHire(final JobClient client, final ActivatedJob job) {
        updateStatus(client, job, "READY_TO_HIRE");
    }

    @JobWorker(type = "update-tool-status-out-of-service")
    public void updateStatusOutOfService(final JobClient client, final ActivatedJob job) {
        updateStatus(client, job, "OUT_OF_SERVICE");
    }

    @JobWorker(type = "update-tool-status-in-service-fixpro")
    public void updateStatusInServiceFixPro(final JobClient client, final ActivatedJob job) {
        updateStatus(client, job, "IN_SERVICE_FIXPRO");
    }

    @JobWorker(type = "update-online-stock-display-retail")
    public void updateOnlineStockDisplayRetail(final JobClient client, final ActivatedJob job) {
        updateOnlineChannel(client, job, "retail");
    }

    @JobWorker(type = "update-online-hire-availability-th")
    public void updateOnlineHireAvailability(final JobClient client, final ActivatedJob job) {
        updateOnlineChannel(client, job, "toolHire");
    }

    @JobWorker(type = "update-online-inventory-status-wh")
    public void updateOnlineInventoryStatusWarehouse(final JobClient client, final ActivatedJob job) {
        updateOnlineChannel(client, job, "warehouse");
    }

    @JobWorker(type = "deduct-stock-on-sale-pos-sync")
    public void deductStockOnSale(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            int quantity = (int) number(variables, "quantity", 1);
            variables.put("stockDeducted", quantity);
            variables.put("posSynced", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "reserve-tool-stock-on-confirmed-booking")
    public void reserveToolStock(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("toolReserved", true);
            variables.put("bookingStatus", "CONFIRMED");
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    @JobWorker(type = "sync-fixpro-service-records-via-api")
    public void syncFixProServiceRecords(final JobClient client, final ActivatedJob job) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("fixProRecordsSynced", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    private void updateStatus(final JobClient client, final ActivatedJob job, final String status) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put("toolStatus", status);
            variables.put("imsUpdated", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }

    private void updateOnlineChannel(final JobClient client, final ActivatedJob job, final String channel) {
        try {
            Map<String, Object> variables = variables(job);
            variables.put(channel + "OnlineStatusUpdated", true);
            complete(client, job, variables);
        } catch (Exception e) {
            fail(client, job, e);
        }
    }
}
