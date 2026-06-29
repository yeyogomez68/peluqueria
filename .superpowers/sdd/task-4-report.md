# Task 4: API Endpoints for Payment and Commission Updates

## Status
DONE

## Commit
9aed329

## Summary
Added PATCH `/citas/{id}/pagar` endpoint for registering cita payments with automatic commission calculation, and PATCH `/profesionales/{id}/comision` endpoint for updating professional commission percentages.

### Changes
- **CitaController**: Added `registrarPago()` method with import of `RegistrarPagoRequest`
- **ProfesionalController**: Added `actualizarComision()` method with validation constraints (0-100%)
