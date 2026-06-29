# Tasks 8 & 9 — Report

## Status: COMPLETE

## Backend — Task 8
**Commit:** `42eb53c`
Added `MisIngresosResponse` DTO, `findCompletadasPorProfesionalYRango` to `CitaRepository`/`JpaCitaRepository`, `misIngresos()` use case in `ProfesionalService` (with `CitaRepository` injected), and `MiPortalController` exposing `GET /api/mi-portal/mis-citas` and `GET /api/mi-portal/ingresos`.

## Frontend — Task 9
**Commit:** `5d07daa`
Created `MiPortalApiService`, `ProfesionalPortalComponent` (standalone, two-tab PrimeNG view: citas del día + resumen de ingresos con detalle), `profesional-portal.routes.ts`, and registered `/mi-portal` route in `app.routes.ts` guarded by `roleGuard` for `PROFESIONAL` and `ADMIN_TENANT`.

## Notes
- `AuthUser` has no `profesionalId` field; the component uses `userId` as a best-effort value. A future task should add `profesionalId` to the JWT claim and the `AuthUser` model.
