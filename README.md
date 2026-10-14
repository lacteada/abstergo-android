# abstergo-android

Cliente Android del **Sistema Municipal de Atención Ciudadana**. Consume la API
de [`abstergo`](https://github.com/lacteada/abstergo), que corre en Django.

## Qué hace

- Iniciar sesión con el correo y el token JWT.
- Recuperar la contraseña: código por correo, verificación y contraseña nueva.
- Listar y mantener **vecinos**, **atenciones** y **usuarios**, según lo que el
  rol permita.

## Cómo se compila

```bash
./gradlew :app:assembleDebug
```

Requiere el SDK de Android y `local.properties` con `sdk.dir`.

## Cómo habla con el backend

- El contrato está en inglés y la interfaz en español; el glosario completo está
  en `documento_tecnico.md` §18.3 del repo del backend.
- La URL base sale de `buildConfigField` en `app/build.gradle.kts`.
- En desarrollo el servidor usa un certificado autofirmado: la app lo confía
  **solo en la variante debug**, y nunca en release.

## Estructura

- `data/`: el contrato con la API (DTO, `ApiService`, `Sesion`, `Api`).
- `dominio/`: las reglas del negocio (RUT, patrones, catálogos).
- El resto: las pantallas, todas heredando de `ActividadBase`.
