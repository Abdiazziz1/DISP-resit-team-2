# ProBuild Process Modelling and Automation

**DISP Team 2**

This repository contains the portfolio submission for the ProBuild Supplies Ltd case study. The project models ProBuild as a socio-technical system and then develops the process into strategic BPMN, operational BPMN, Camunda Forms, and Camunda 8 automation.

The final automation uses **Camunda 8 Run (local orchestration cluster)** with a **Java Spring Boot external worker application**. Camunda Forms capture user input, BPMN service tasks create Zeebe jobs, and Java workers complete those jobs through matching `@JobWorker` methods.

---

## Project Scope

The project covers the following ProBuild business areas:

| Area | Included Process Work |
|---|---|
| Retail sales | Customer order, stock check, payment and order confirmation |
| Tool hire | Hire request, tool availability, deposit/payment and return flow |
| Warehouse | Stock updates, order fulfilment, IMS synchronisation |
| Finance | Credit checks, finance approval, repayment handling |
| FixPro repairs | Tool maintenance, repair reports and approval handling |
| Suppliers | Delivery and stock replenishment support |

---

## Portfolio Evidence

| Marking Area | Repository Evidence |
|---|---|
| Socio-technical model | `Socio-Technical-Model` |
| Strategic BPMN | `Strategic-BPMN` |
| Operational BPMN | `Operational-BPMN` |
| Camunda deployment resources | `Camunda-Upload-Resources` |
| Camunda Forms | `Camunda-Upload-Resources/forms` |
| Java automation workers | `Java-Worker-Project` |
| Testing evidence | `Evidence` |
| Configuration management | GitHub repository, commits and README |

---

## Main Folders

### `Socio-Technical-Model`

Contains the i* socio-technical model, including the visual model (`istar.png`) and source text file (`goalModel.txt`).

### `Strategic-BPMN`

Contains the revised high-level strategic BPMN model (`Strategic_BPMN_.bpmn`) showing the business process before technical automation detail, including corrected human task definitions.

### `Operational-BPMN`

Contains the revised detailed operational BPMN model (`Operational_BPMN_.bpmn`) used for automation, including service tasks, user tasks, gateways, pools and message flows.

> **Note:** This file was corrected to fix two structural validation errors that previously blocked deployment:
> - `Proc_Customer` had two blank ("none") start events, which is invalid BPMN. These were replaced with a single start event ("Customer decides to buy or hire") feeding an exclusive gateway that branches into Buy/Hire, matching the pattern already used in the Strategic BPMN.
> - `Proc_FinTrust` was missing a start event entirely. A start event ("Finance Request Received") was added ahead of the existing "Receive credit application from ProBuild" task.

### `Camunda-Upload-Resources`

Contains the BPMN and Camunda Forms that can be uploaded to Camunda:

- `probuild-operational-model.bpmn` — the deployable operational model (same corrected content as `Operational-BPMN/Operational_BPMN_.bpmn`, named to match the Zeebe job types referenced by the Java workers)
- `forms/` — all deployable Camunda Forms with input validation (required fields, pattern matching, min/max ranges)

> **Note:** `submit-hire-request.form` was corrected — its internal `id` field previously did not match the `formId` referenced by the BPMN (`submit-hire-request-1ama1l4` vs the expected `submit-hire-request`), which caused a deployment error. The form's `id` field now correctly reads `submit-hire-request`.

### `Java-Worker-Project`

Contains the Spring Boot application that connects to Camunda 8 and executes external workers using `@JobWorker`. Every Zeebe job type declared in the operational BPMN has a matching worker implementation:

- `ProBuildApplication.java` — Spring Boot entry point
- `CamundaDeploymentRunner.java` — deploys the BPMN model and forms to Camunda 8 on startup
- `workers/WorkerSupport.java` — shared completion/failure/variable-handling logic
- `workers/FinanceWorkers.java`, `FixProWorkers.java`, `InventoryWorkers.java`, `ReportingWorkers.java`, `RetailSalesWorkers.java`, `ToolHireWorkers.java` — job workers grouped by business area

The BPMN model and forms are also copied into `src/main/resources/` so the deployment runner can load them from the classpath when the app runs.

### `Evidence`

Contains the testing evidence:

- `Test_Strategy___Cases.xlsx` — test strategy summary and full test case list (Strategic, Operational, FixPro, forms, configuration)
- `Testing_Report.docx` — test coverage summary, validation approach and defect log

---

## Automation Design

The operational process follows this automation flow:

```text
Camunda Form
    ↓
BPMN User Task
    ↓
Process Variables
    ↓
BPMN Service Task
    ↓
Zeebe Job Type
    ↓
Java @JobWorker
    ↓
Variables returned to Camunda
    ↓
Gateway / Next Process Step
```

Service tasks in the BPMN are connected to Java workers by matching the BPMN job type with the Java `@JobWorker` annotation.

Example:

```java
@JobWorker(type = "run-real-time-credit-check-on-customer")
public void runRealTimeCreditCheck(final JobClient client, final ActivatedJob job) {
    Map<String, Object> variables = variables(job);
    String decision = text(variables, "financeDecision", "approved");
    boolean approvedDecision = "approved".equalsIgnoreCase(decision);
    variables.put("financeDecision", approvedDecision ? "approved" : "declined");
    variables.put("approved", approvedDecision);
    variables.put("declined", !approvedDecision);
    variables.put("creditChecked", true);
    complete(client, job, variables);
}
```

---

## Running the Java Worker

Open a terminal and navigate to the worker project:

```bash
cd Java-Worker-Project
```

Run the Spring Boot application:

```bash
mvn spring-boot:run
```

On startup, `CamundaDeploymentRunner` will deploy `probuild-operational-model.bpmn` and the forms in `forms/` to your configured Camunda 8 cluster (requires `probuild.deployment.enabled=true` and valid Camunda client credentials in `application.properties`/environment variables).

The worker application must remain running while process instances are tested in Camunda.

---

## Deploying to Camunda 8 (Local / Camunda 8 Run)

**Important:** as of Camunda 8.9, linked Camunda Forms are no longer auto-deployed alongside a BPMN file when using the Camunda Desktop Modeler's Deploy button — each linked form must be explicitly included in the same deployment call, or the deployment is rejected with a `formId not found` error.

The most reliable way to deploy both the BPMN and all forms together is via the Camunda 8 REST API, using a single multipart request:

```powershell
curl.exe -X POST http://localhost:8080/v2/deployments `
  -F "resources=@Camunda-Upload-Resources/probuild-operational-model.bpmn" `
  -F "resources=@Camunda-Upload-Resources/forms/approve-repair-request.form" `
  -F "resources=@Camunda-Upload-Resources/forms/check-tool-availability.form" `
  -F "resources=@Camunda-Upload-Resources/forms/finance-application.form" `
  -F "resources=@Camunda-Upload-Resources/forms/fixpro-service-report.form" `
  -F "resources=@Camunda-Upload-Resources/forms/payment.form" `
  -F "resources=@Camunda-Upload-Resources/forms/place-retail-order.form" `
  -F "resources=@Camunda-Upload-Resources/forms/submit-hire-request.form"
```

A successful response returns a JSON object listing the deployed `processDefinition` and each `form`, each with its own `formKey`.

Once deployed, confirm the process is live in **Operate** (`http://localhost:8080/operate`), under the **Processes** tab, searching for `Proc_ProBuild`.

---

## Testing a Deployed Process

### Starting an instance with pre-filled variables

To run a full end-to-end instance where all gateway decisions are pre-answered (useful for testing the automated/service-task path):

```powershell
curl.exe -X POST http://localhost:8080/v2/process-instances -H "Content-Type: application/json" --data "@start-instance.json"
```

Where `start-instance.json` contains:

```json
{
  "processDefinitionId": "Proc_ProBuild",
  "variables": {
    "correlationId": "test-001",
    "toolAvailable": true,
    "stockAvailable": true,
    "paymentSuccessful": true,
    "financeDecision": "approved",
    "repairApproved": true,
    "tradeCard": false,
    "standard": true,
    "orderTotal": 150,
    "quantity": 1,
    "customerName": "Test Customer",
    "toolId": "DRILL-001",
    "approved": true,
    "declined": false,
    "confirmed": true,
    "routine": true,
    "repair": false,
    "passed": true,
    "failed": false
  }
}
```

### Demonstrating live user input via Tasklist

The operational process has one user task with a linked Camunda Form directly on the executable path: **"Approve major repair request from FixPro"** (`thT_approveRepair`, form `approve-repair-request`). To reach it directly for a demo without needing to simulate the full upstream message chain, start an instance using Camunda 8's `startInstructions` feature:

```json
{
  "processDefinitionId": "Proc_ProBuild",
  "processDefinitionVersion": -1,
  "startInstructions": [
    { "elementId": "thT_approveRepair" }
  ],
  "variables": {
    "correlationId": "demo-001"
  }
}
```

Then open **Tasklist** (`http://localhost:8080/tasklist`, login `demo`/`demo`), open the waiting task, fill in **Tool ID** and **Estimated repair cost**, tick **Approve repair**, and click **Complete Task**. The completed task and its submitted values are then visible under the Tasklist "Completed" filter and in Operate's instance history.

### Testing the warehouse/inventory automated path

The process also has a genuine "none" start event, `whS_supplierDel` ("Supplier delivery arrives"), which can be triggered with no special start instructions:

```json
{
  "processDefinitionId": "Proc_ProBuild",
  "processDefinitionVersion": -1,
  "variables": {
    "correlationId": "demo-warehouse-001"
  }
}
```

This runs through: Supplier delivery arrives → Receive goods-in from supplier → Scan and bin stock into IMS → Perform daily stock check → Perform weekly cycle count → Stock levels updated — fully automated, visible step-by-step in Operate's Instance History panel.

---

## Testing

Testing evidence is stored in the `Evidence` folder:

- `Test_Strategy___Cases.xlsx` — 17 test cases covering Strategic BPMN, Operational BPMN, FixPro, form validation and configuration/deployment
- `Testing_Report.docx` — test coverage summary, validation approach and defect log

Testing covers:

- BPMN import and structural validation (Camunda Web Modeler / Desktop Modeler)
- Camunda Form validation (required fields, pattern/range checks)
- Variable passing between tasks
- Gateway decision paths and boundary/exception events
- Completed process instances in Camunda Operate, including a live human task completion in Tasklist
- Defects and fixes found during deployment (see notes in `Operational-BPMN` and `Camunda-Upload-Resources` sections above)

---

## Authors

**DISP Team 2**
