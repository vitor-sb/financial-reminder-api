# Financial Reminder API - Endpoint Documentation

Below are example requests for each API endpoint.

---

## 📖 Endpoints
- 🔐 [1. Authentication](#1-authentication)
- 👤 [2. Users](#2-users)
- 🛠️ [3. Admin: Users](#3-admin-users)
- 📅 [4. Payment Reminders](#4-payment-reminders)
- 💳 [5. Payment Transactions](#5-payment-transactions)

---

## 1. Authentication

### ➕ 1.1. User Login
Authenticates a user and returns a JWT token.

**Endpoint:** POST /auth/login  
**Description:** Validates user credentials and returns an access token.

**Example request body (JSON):**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

---

## 2. Users

Endpoints for user self-management.

### ➕ 2.1. Create a New User

**Endpoint:** POST /users  
**Description:** Registers a new user in the system.

**Example request body (JSON):**
```json
{
  "name": "João da Silva",
  "email": "joao.silva@example.com",
  "password": "senhaForte123",
  "role": "USER"
}
```
Possible values for `role`: [**RoleEnum.java**](./src/main/java/com/reminder/financial/api/domains/models/user/enums/RoleEnum.java).

### ➕ 2.2. Deactivate Own Account

**Endpoint:** POST /me/disable  
**Description:** Deactivates the logged-in user's account. No request body required.

### ➕ 2.3. Reactivate Own Account

**Endpoint:** POST /me/enable  
**Description:** Reactivates the logged-in user's account. No request body required.

### 🗑️ 2.4. Delete Own Account

**Endpoint:** DELETE /me  
**Description:** Deletes the logged-in user's account. No request body required.

---

## 3. Admin: Users

Endpoints for managing system users (requires ADMIN permission).

### 🔍 3.1. List and Filter Users

**Endpoint:** GET /admin/users/list  
**Description:** Returns a paginated list of users based on provided filters.

**Example request with query parameters:**
```
/admin/users/list?name=Joao&role=USER&page=0&size=20
```

**⚙️ Filter Parameters (ListUsersFilterRequest):**

- name (String)
- email (String)
- role (Enum): Filters by user role. Possible values in [**RoleEnum.java**](./src/main/java/com/reminder/financial/api/domains/models/user/enums/RoleEnum.java)
- createdFrom (LocalDate): Filters from creation date (format YYYY-MM-DD)
- createdUntil (LocalDate): Filters until creation date (format YYYY-MM-DD)
- enabled (Boolean): Filters by active/inactive users
- deleted (Boolean): Filters by deleted/non-deleted users

---

## 4. Payment Reminders

Manages all aspects of payment reminders.

### ➕ 4.1. Create Payment Reminder

**Endpoint:** POST /payments/reminders  
**Description:** Creates a new payment reminder.

**Example request body (JSON):**
```json
{
  "title": "Rent",
  "description": "Monthly apartment rent payment",
  "date": "2025-08-05",
  "recurrence": "MONTHLY",
  "method": "TRANSFER",
  "value": 1500.00,
  "installmentsNumber": 12
}
```
- Possible values for `recurrence`: [**RecurrenceEnum.java**](./src/main/java/com/reminder/financial/api/domains/models/reminder/enums/RecurrenceEnum.java)
- Possible values for `method`: [**TransactionMethodEnum.java**](./src/main/java/com/reminder/financial/api/domains/models/reminder/enums/TransactionMethodEnum.java)

### 🔍 4.2. Get Reminder by ID

**Endpoint:** GET /payments/reminders/{id}  
**Description:** Retrieves a specific payment reminder by ID.

### ✏️ 4.3. Update Payment Reminder

**Endpoint:** PATCH /payments/reminders/{id}  
**Description:** Updates a payment reminder.

**Example request body (JSON):**
```json
{
  "title": "Monthly Rent",
  "description": "Rent for apartment 123",
  "method": "PIX",
  "value": 1550.00
}
```

### ➕ 4.4. Disable Payment Reminder

**Endpoint:** POST /payments/reminders/{id}/disable  
**Description:** Disables a payment reminder and its pending transactions. No request body required.

### ➕ 4.5. Enable Payment Reminder

**Endpoint:** POST /payments/reminders/{id}/enable  
**Description:** Re-enables a payment reminder and its pending transactions. No request body required.

### 🗑️ 4.6. Delete Payment Reminder

**Endpoint:** DELETE /payments/reminders/{id}  
**Description:** Deletes a payment reminder and cancels its transactions. No request body required.

### 🔍 4.7. List Monthly Bills

**Endpoint:** GET /payments/reminders/monthly-bills  
**Description:** Returns existing transactions and simulated future accounts, including a summary of totals.

**Example request with query parameters:**
```
/payments/reminders/monthly-bills?year=2025&month=8
```

**⚙️ Query Parameters (optional):**
- year (Integer): The year for which to retrieve bills. If omitted, defaults to the current year.
- month (Integer): The month (1-12) for which to retrieve bills. If omitted, defaults to the current month.
- withSimulatedBills (Boolean): If true, includes forecasted future bills. If false, returns only actual transactions. Defaults to true.

### 🔍 4.8. List User Reminders

**Endpoint:** GET /payments/reminders/list  
**Description:** Returns a paginated list of all payment reminders for the logged-in user.

**Example request with query parameters:**
```
/payments/reminders/list?title=Rent&enabled=true
```

**⚙️ Filter Parameters (ListPaymentRemindersFilterRequest):**

- reminderId (String)
- title (String)
- dueDateFrom (LocalDate)
- dueDateUntil (LocalDate)
- recurrence (Enum)
- method (Enum)
- enabled (Boolean)

---

## 5. Payment Transactions

Manages individual payment transactions.

### ➕ 5.1. Make a Payment

**Endpoint:** POST /payments/transactions/{transactionId}/pay  
**Description:** Pays a pending transaction.

**⚙️ Request Body Parameters (Optional):**
- paymentValue (BigDecimal): overrides the original transaction value
- method (Enum): overrides the payment method. Possible values in [**TransactionMethodEnum.java**](./src/main/java/com/reminder/financial/api/domains/models/reminder/enums/TransactionMethodEnum.java)

**Example request body (JSON):**
```json
{
  "paymentValue": 1550.00,
  "method": "PIX"
}
```

### 🔍 5.2. List and Filter Transactions

**Endpoint:** GET /payments/transactions/list  
**Description:** Returns a paginated list of transactions based on filters.

**Example request with query parameters:**
```
/payments/transactions/list?status=PENDING&reminderId=abc-123
```

**⚙️ Filter Parameters (ListTransactionsFilterRequest):**

- transactionId (String)
- reminderId (String)
- dueDateFrom (LocalDate)
- dueDateUntil (LocalDate)
- method (Enum): [**TransactionMethodEnum.java**](./src/main/java/com/reminder/financial/api/domains/models/reminder/enums/TransactionMethodEnum.java)
- status (Enum): [**TransactionStatusEnum.java**](./src/main/java/com/reminder/financial/api/domains/models/reminder/enums/TransactionStatusEnum.java)
- enabled (Boolean)

### ✏️ 5.3. Update Transaction Details

**Endpoint:** PATCH /payments/transactions/{id}  
**Description:** Allows modifying the amount or payment method of an unpaid transaction.

**Example request body (JSON):**
```json
{
  "paymentValue": 1560.00,
  "method": "DEBIT"
}
```