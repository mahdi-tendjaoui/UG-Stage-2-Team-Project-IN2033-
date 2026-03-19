# IPOS SA (Inventory/Purchase Order System - Supplier API) - AI Coding Guidelines

## Project Overview
A Java-based purchasing system implementing supplier API interactions. The architecture follows an interface-driven design with core API implementation patterns.

**Key Components:**
- `ISupplierAPI.java` - Interface defining supplier catalog, order, and invoice operations
- `IPOS_SA_CoreAPI.java` - Implementation class for purchase order submission and supplier integration
- Package: `iposSA` - Single package namespace for this module

## Architecture & Design Patterns

### Interface-First Design
All functionality is declared through `ISupplierAPI` interface contracts:
- `getProductCatalogue()` - Retrieve available products
- `submitPurchaseOrder(Order)` - Submit orders with boolean confirmation
- `getDeliveryStatus(String)` - Track order delivery status by ID
- `getOutstandingInvoices()` - Retrieve unpaid invoices

**Implementation:** `IPOS_SA_CoreAPI` implements these contracts. Follow interface contracts when adding methods.

### Type Conventions
- Use `string` for identifiers (see `getDeliveryStatus(string orderID)`)
- Use typed arrays: `ProductList[]`, `Invoice[]` for collections
- Return `boolean` for operation success/failure

## Development Workflow

### Build & Compilation
- **Build System:** Maven (implied by `iposSA.iml` - IntelliJ Module format)
- **Java Version:** Verify in `.iml` file or `pom.xml` when available
- **Compilation:** Use IntelliJ IDEA or `mvn compile` command

### Code Structure
1. All classes belong to `package iposSA;`
2. Import necessary interfaces: `import iposSA.ISupplierAPI.*;`
3. Implement stub methods that currently throw `UnsupportedOperationException()`

## Implementation Patterns

### Stub Methods
Current implementation uses TODO markers with exception throws:
```java
public ProductList[] getProductCatalogue() {
    // TODO - implement IPOS_SA_CoreAPI.getProductCatalogue
    throw new UnsupportedOperationException();
}
```

When implementing: Replace exception with actual logic while preserving method signature and return types.

### Expected Entity Classes
Referenced but not yet defined - create when implementing:
- `Order` - Purchase order entity with order details
- `ProductList` - Product catalog entry
- `Invoice` - Invoice record for outstanding payments

## Integration Points

### Supplier API Contract
The system integrates with supplier systems through:
- **Catalog Queries:** Retrieve products via `getProductCatalogue()`
- **Order Submission:** Send purchase orders via `submitPurchaseOrder(Order)`
- **Order Tracking:** Query status and invoices using order IDs

When adding integrations, maintain the interface contract and use the same method signatures.

## Common Tasks

### Adding New Interface Methods
1. Declare in `ISupplierAPI.java` with JavaDoc
2. Add stub implementation in `IPOS_SA_CoreAPI.java` with `UnsupportedOperationException`
3. Document parameter and return types clearly

### Implementing Stub Methods
1. Replace the `UnsupportedOperationException` throw
2. Preserve the method signature and return type
3. Ensure implementation matches interface contract
4. Add business logic for supplier API interaction

## Notes for AI Agents
- This is an early-stage project with placeholder implementations
- Focus on maintaining interface contracts when implementing methods
- Entity classes (Order, Invoice, ProductList) need to be defined
- Consider adding error handling for supplier API failures
- Document assumptions about external supplier system behavior
