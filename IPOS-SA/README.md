# IPOS-SA — Wholesaler Subsystem

A JavaFX desktop GUI for the InfoPharma supplier (SA) subsystem.

## Running

**From IntelliJ / your IDE (recommended):**
Open the project, let Maven resolve dependencies, then run
`com.prototype.ipossa.Launcher`. Nothing else to configure.

**From the command line:**
```
./run.sh           # macOS / Linux
run.bat            # Windows
```
Both scripts use the project's existing `mvnw` wrapper to invoke
`mvn javafx:run`. No new tooling is required.

## Database

Connection settings live in `src/main/resources/config.properties`. On
first launch the app auto-creates any missing auxiliary tables
(`orders`, `order_items`, `payments`, `applications`, `user_emails`).
Core tables (`logins`, `merchants`, `merchants_discounts`, `catalogue`)
are expected to already exist.

## Sample logins (per the brief)

| Username    | Password         | Role                        |
|-------------|------------------|-----------------------------|
| sysdba      | London_weighting | Administrator               |
| manager     | Get_it_done      | Director of Operations      |
| accountant  | Count_money      | Senior accountant           |
| clerk       | Paperwork        | Accountant                  |
| warehouse1  | Get_a_beer       | Warehouse employee          |
| warehouse2  | Lot_smell        | Warehouse employee          |
| delivery    | Too_dark         | Delivery department employee|

## Features

**Login** — themed card with async credential check against `logins`.

**Sidebar** — collapsible with the ☰ button in the top bar; collapses
to icon-only width.

**Light / dark mode** — toggle in the top bar; persisted via the in-memory
ThemeManager and applied to every scene including dialogs.

**Dashboard** — five summary cards plus recent orders and merchant
account-state lists.

**Catalogue** — add / edit / delete products, set/modify minimum stock,
update stock quantities, sensible search across ID/description/package,
low-stock banner, and a low-stock warning dialog at login.

**Orders** — view all orders, filter "incomplete only", search by
merchant or order ID, create new orders (with cart, auto-deducts stock),
advance status (`accepted → ready to dispatch → dispatched → delivered`),
generate invoices, record payments by various methods (auto-restores
suspended accounts when balance clears).

**Reports** — six built-in reports (merchant balances, low stock, order
summary, payments, debtors, full catalogue) with print and "save as file"
(PDF-equivalent per the brief). Plus a "generate debtor reminders"
button that produces ready-to-send reminder letters.

**Applications** — receive non-commercial / commercial applications
from the PU portal, approve / reject, and send the outcome by email
(SMTP configured in Settings → System).

**Users** — create / delete staff accounts and change roles.
**Admins cannot change or delete their own account** (enforced both
client-side and via the existing safeguard in `AccountService`).

**Settings** — three tabs:
- *Account*: change your own password and email.
- *Merchants* (admin): create / edit / delete merchant accounts, set
  credit limits, manage discount plans (fixed and variable tiers),
  change account states (`normal` / `suspended` / `in_default`).
  Reactivating an "in default" account is restricted to the Director of
  Operations or Administrator per the brief.
- *System*: SMTP settings and the dark-mode toggle.

## Role-based access

Every action checks the current user's role through
`SessionManager.requirePermission(...)`. Buttons disable themselves when
the signed-in user lacks the relevant privilege, and the underlying
service layer rejects unauthorised calls as a second line of defence.

## Notes / known limitations

- The "send email" button shows a preview dialog and logs success;
  wiring an actual SMTP transport (e.g. Jakarta Mail) was out of scope
  but is a straightforward addition to `ApplicationsPage.emailOutcome`.
- Print uses `javafx.print.PrinterJob`; on machines with no printer
  configured, use **Save as file** instead.
- Cascaded deletes are handled in `AccountSQL.deleteMerchantAccount`
  (removes discount tiers before the merchant row).
