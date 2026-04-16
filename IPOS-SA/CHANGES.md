# IPOS-SA — Bug-fix and refactor summary

## Bugs fixed

1. **Merchant login "Invalid credentials"**
   - `AccountSQL.authenticateMerchant` and `getMerchantID` now use
     `LOWER(TRIM(...))` so leading/trailing whitespace and case
     differences in seeded rows no longer break logins.
   - `SchemaInit.seedMerchantLogins` matches on
     `TRIM(REPLACE(account_holder_name, '.', ''))` so "Cosymed Ltd."
     vs "Cosymed Ltd" mismatches are tolerated.

2. **"Field 'payment_method' doesn't have a default value"**
   - `SchemaInit` now adds the legacy `payment_method` column with a
     default of 'Bank transfer' AND force-modifies it to add the default
     even when the column already exists without one.
   - `OrdersPage.savePayment` writes both the new `method` column and
     the legacy `payment_method` column on every insert.

3. **"Duplicate entry '1' for key 'orders.PRIMARY'"**
   - `SchemaInit.ensureOrderIdAutoIncrement` promotes a legacy
     non-AUTO_INCREMENT `orders.order_ID` column to AUTO_INCREMENT.
   - `OrdersPage.saveOrder` uses
     `Statement.RETURN_GENERATED_KEYS` instead of a manual `MAX()+1`
     fallback, so concurrent inserts no longer collide.

4. **Auto-suspended / in-default not happening**
   - `MerchantStateUpdater` rewritten to compute days-late from the
     **oldest unpaid order's** end-of-next-month due date, not from a
     single static field that gets cleared on payment. Suspension and
     default transitions now happen on app startup, after every order
     creation, and after every payment.
   - Per §8.1: paying a suspended account that clears the balance
     restores it to 'normal' automatically; 'in_default' still requires
     explicit Director of Operations action.

5. **Monetary values shown to 1 decimal place**
   - New `ui/Formats.java` utility provides `money()`, `pound()`, and
     reusable `moneyCell()` / `poundCell()` cell factories.
   - Applied to all `Number` columns in OrdersPage, CataloguePage,
     MerchantOrdersPage, and MerchantManagementPanel.
   - `CatRef.toString()` and `MerchantRef.toString()` reformatted to
     2 dp; credit-limit input fields default to `%.2f`.

6. **Tabs visible to wrong roles**
   - `MainApp.buildSidebar` now gates each nav button by role:
     - Catalogue: visible if `canManageCatalogue()`
     - Orders: visible if `canManageOrders()` or `canRecordPayments()`
     - Reports: visible if `canGenerateReports()`
     - Applications: visible if `canManageMerchantAccounts()` or admin
     - Users: visible only to admins / Director of Operations
   - The "you require admin to view this page" placeholders are gone
     because the buttons themselves no longer render.

7. **Reports rewritten to match §8.1 IPOS-SA-RPT**
   - The six required reports are now implemented with date pickers
     and a merchant picker:
     - **(i) Turnover for period** — line chart of revenue by month +
       pie chart of revenue share by merchant + headline KPIs.
     - **(ii) Orders by merchant** — table of orders + bar chart of
       order values + paid/pending counts.
     - **(iii) Merchant activity report** — contact card + items-sold
       table + bar chart of revenue by item.
     - **(iv) Invoices for a merchant** — table of all invoices in
       period for the chosen merchant.
     - **(v) All invoices** — table + pie chart of invoiced value
       share across all merchants.
     - **(vi) Stock turnover** — bar chart with two series (sold in
       period vs current stock) per item.
   - All reports support Print and Save-as-file; debtor reminders are
     still generated on demand from the same screen.

8. **Code organised by §8.1 packages**
   - `systems/Accounts/` → `systems/ACC/`
   - `Order`, `OrderItem`, `OrderService`, `Invoice`, `InvoiceService`
     → `systems/ORD/`
   - `CatalogueItem` → `systems/CAT/`
   - `Report` → `systems/RPRT/`
   - `module-info.java` updated to export and open the new packages.
   - All cross-package imports throughout the project have been updated.
