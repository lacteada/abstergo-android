# AGENTS.md — Operación de `abstergo-android`

El cliente Android del sistema municipal de atención ciudadana. Consume la API
de `abstergo` (`https://github.com/lacteada/abstergo`).

---

## 1. Cómo se trabaja acá

- Implementa el cambio directamente y reporta lo que quedó.
- El trabajo no está cerrado hasta que `./gradlew :app:assembleDebug` pase.
- Termina con un resumen corto: qué cambió y el resultado del build.

---

## 2. Principios del proyecto: simplicidad primero

La regla rectora del código. Si algo entra en duda, gana esta sección.

- La menor complejidad que resuelva el problema.
- POO de verdad: cada clase con una responsabilidad. Si hace dos cosas, se parte.
- DRY: una sola fuente por regla. Si una regla vive en dos lugares, ya está mal.
- Menos líneas con más propósito: lo que se repite tres veces se abstrae.
- Sin dependencias nuevas si la biblioteca estándar alcanza.
- Documentación corta y representable.

**El límite**

- "Menos líneas" no es comprimir: una función que hace cinco cosas es confusa.
- La claridad manda sobre el ingenio.
- Simplicidad no es menos verificación: el build sigue siendo obligatorio.

---

## 3. Convenciones

- Responder en español, formato vertical: **negrita** y viñetas de un punto por
  línea.
- Preferir editar en sitio sobre crear archivos.
- **El contrato de la API está en inglés**; el dominio interno y la interfaz, en
  español. Los DTO de `data/` siguen el contrato al pie de la letra (glosario en
  `documento_tecnico.md` §18.3 del repo del backend).
- Las pantallas heredan de `ActividadBase`; no repitas el manejo de insets.
- Los avisos y los diálogos salen de `Avisos.kt`.
- Las reglas de negocio (RUT, correo, catálogos) viven en `dominio/`, no en una
  actividad.

---

## 4. Estructura

```text
app/src/main/java/cl/lacteada/abstergo/
├── ActividadBase.kt      # pantalla base: insets y edge-to-edge
├── Avisos.kt             # avisos, errores y diálogos
├── FiltroTexto.kt        # el TextWatcher de las búsquedas
├── ListaAdapter.kt       # el adapter de los listados
├── Permisos.kt           # lo que el rol permite, según /auth/me/
├── SesionUI.kt           # volver al login si la sesión se cayó
├── data/                 # contrato con la API y base local: DTO, servicio, sesión y SQLite
└── dominio/              # reglas del negocio: RUT y patrones de entrada
```

---

## 5. Build y verificación

```bash
./gradlew :app:assembleDebug
```

- No hay emulador en el flujo normal: el build es la verificación.
- El `BASE_URL` sale de `buildConfigField` (`app/build.gradle.kts`); la variante
  `release` todavía apunta al host de desarrollo.
- El certificado autofirmado se confía **solo en debug**
  (`app/src/debug/res/xml/network_security_config.xml`), nunca en release.

---

## 6. Límites

- No renombrar el paquete `cl.lacteada.abstergo`.
- No commitear secretos ni certificados de release.
- No tocar el repositorio del backend desde acá.
