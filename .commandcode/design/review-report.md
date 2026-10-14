# Review — abstergo-android

- **Proyecto**: abstergo-android (`cl.lacteada.abstergo`)
- **Modo**: `/design review`
- **Fecha**: 2026-10-14
- **Registro**: Product (app de operación, no de marca)
- **Alcance**: las 12 pantallas, los estilos, el flujo de autenticación, el CRUD de usuarios con SQLite y la pantalla de sensores.

---

## Puntuación

**24 / 50**

| Lente | Puntaje | Lectura |
|---|---|---|
| Primera impresión | 6 / 10 | Coherente desde que hay paleta y estilos, pero sin un punto de vista propio |
| Jerarquía | 5 / 10 | La escala tipográfica avanza en pasos que el ojo no distingue |
| Voz del color | 4 / 10 | Tres familias de color sin razón, y sin color de estado |
| Voz de la tipografía | 5 / 10 | Una sola familia con seis tamaños que no forman escala |
| Sensación de interacción | 4 / 10 | Faltan estados vacíos, el error de sincronización no se explica y hay controles sin nombre |

**Veredicto: Block.** Quedan hallazgos HIGH en pie.

---

## TL;DR

La app mejoró mucho en consistencia: hay paleta, hay estilos, hay estados de botón y la navegación tiene flecha. Lo que falta ya no es cosmético.

El problema central es que **la interfaz no tiene nombre ni etiqueta en los sitios donde el usuario decide**: los campos usan la pista como etiqueta, la linterna y la ampolleta son dos imágenes tocables que no dicen qué son, y el código de verificación son seis casillas sin etiqueta. Sumado a que cuatro pantallas no tienen desplazamiento, con el teclado abierto el botón principal se va de la pantalla.

**Acción principal**: `/design a11y` — etiquetas visibles, nombres en los controles y desplazamiento en las pantallas de formulario.

---

## Primera impresión

La primera pantalla que ve el usuario es el login. Se entiende qué es y dónde actuar: logo, título, dos campos, botón. Eso está bien resuelto.

Lo que no hay es un punto de vista memorable. Si le cambias el logo, la pantalla podría pertenecer a cualquier app de gestión: fondo oscuro, tarjetas translúcidas, botones con borde. No hay un objeto del dominio a la vista, ni un color que diga "municipal", ni una decisión de composición que solo tenga sentido acá.

El color es el tell más fuerte. El relleno de los botones es `#26960000` —un **rojo** al 15%—, el borde es `#80397676` —un **verde azulado** al 50%— y el texto es gris neutro. Son tres familias distintas conviviendo sin una razón. No parece una decisión: parece herencia.

---

## Recorrido del flujo principal

**Llega** → splash con logo y animación, 7 segundos. Es largo para una app que el operador abre varias veces al día.

**Entra** → login. Los campos se anuncian por su pista ("Correo electrónico"), que desaparece al escribir. Si el usuario se equivoca de campo, ya no sabe cuál era cuál.

**Se equivoca** → el aviso dice que la cuenta puede estar bloqueada, y eso es correcto. El error se explica bien.

**Recupera** → código de seis dígitos. Es la pantalla mejor resuelta del flujo: contador visible, reenvío que se habilita al vencer, y ahora se puede borrar el código completo. Pero las seis casillas no tienen etiqueta ni nombre para el lector de pantalla.

**Opera** → el listado de usuarios. Lee de SQLite y avisa el estado de sincronización, que es lo mejor de la app. Pero si la lista está vacía, la pantalla queda en blanco: no dice si no hay usuarios, si la búsqueda no encontró nada o si algo falló.

**Consulta sensores** → tres tarjetas idénticas y, abajo, dos íconos tocables. Esa pantalla es la que el usuario no puede resolver solo: nada indica cuál es la linterna y cuál la ampolleta.

---

## Hallazgos

| # | Severidad | Disciplina | Ubicación | Antes | Después | Por qué |
|---|---|---|---|---|---|---|
| 1 | HIGH | Accessibility | `app/src/main/res/layout/activity_sensors.xml:139-165` | `imgNoti` e `imgAmpolleta` son `ImageView` tocables, sin `contentDescription` ni rótulo visible | Poner `contentDescription="Linterna"` y `"Ampolleta"`, y un texto debajo de cada uno | Dos controles que no dicen qué son: el usuario adivina y el lector de pantalla no anuncia nada |
| 2 | HIGH | Accessibility | `app/src/main/res/layout/activity_confirmation_code.xml:60-135` | Seis `EditText` de un dígito, sin etiqueta | Envolver el grupo con una etiqueta `labelFor` al primer campo, o un solo campo de 6 con `letterSpacing` | Seis campos que no se anuncian como el código de verificación |
| 3 | HIGH | Accessibility | `app/src/main/res/values/styles.xml:20-30` (estilo `Campo`), usado en 14 campos | El texto del campo es una pista (`android:hint`) y desaparece al escribir | Una etiqueta `TextView` encima de cada campo; la pista queda solo como ejemplo | La pista no puede hacer de etiqueta: al escribir, el campo queda sin nombre |
| 4 | HIGH | Responsive | `activity_main.xml`, `activity_new_password.xml`, `activity_account_recovery.xml`, `activity_account_signup.xml` | Sin `ScrollView`: el contenido es fijo y la ventana se redimensiona con el teclado | Envolver el contenido en `ScrollView`, como ya hace `activity_usuario_form.xml:37` | Con el teclado abierto en una pantalla chica, el botón principal queda fuera de alcance |
| 5 | MEDIUM | Color | `colors.xml:11-13` y todos los layouts | Relleno `#26960000` (rojo al 15%), borde `#80397676` (verde azulado al 50%), texto gris | Un solo tono como acento y un par semántico para éxito y error | Tres familias de color sin intención, y ningún color que signifique estado |
| 6 | MEDIUM | Interaction | `UsuarioCrud.kt` (`filtrar`) | Sin estado vacío: la lista se ve en blanco | Un mensaje distinto para "aún no hay usuarios" y para "sin resultados" | Una pantalla en blanco no dice si está vacía, si filtró de más o si falló |
| 7 | MEDIUM | Interaction | `UsuarioCrud.kt` (`sincronizar`) | El fallo de sincronización solo cambia la línea de estado | Un aviso con cuántos quedaron pendientes y por qué | El usuario ve "pendiente" y nunca sabe qué lo detuvo |
| 8 | MEDIUM | Type | `styles.xml` | Escala 14 / 16 / 18 / 20 / 22 / 30 sp | Cuatro pasos con razón mínima de 1.3 | Los pasos de 1.14 y 1.13 no se distinguen, y el salto a 30 sí |
| 9 | MEDIUM | Layout | `activity_sensors.xml` | Tres tarjetas idénticas con el mismo peso | Una lectura principal (temperatura) y las demás como filas secundarias | Todo pesa lo mismo: la prueba de entrecerrar los ojos no revela qué importa |
| 10 | MEDIUM | Writing | `activity_account_recovery.xml:60` | El título dice "Recuperar contraseña" y el botón "Recuperar cuenta" | Un solo sustantivo para la misma cosa | Dos nombres para una acción |
| 11 | MEDIUM | Surface | `README.md:11` y `AGENTS.md` §4 | Documentan que la app mantiene "vecinos, atenciones y usuarios" y que `dominio/` tiene "catálogos" | Actualizar ambos a las pantallas y carpetas que existen | La documentación describe una app que ya no es esta |
| 12 | LOW | Color | `item_lista.xml:7,10` | Es el único layout con colores escritos a mano (`#12FFFFFF`, `#CCD3D3D3`) | Usar `@color/tarjeta` y `@color/texto` | Última copia suelta de la paleta |
| 13 | LOW | Accessibility | `ConfirmationCode.kt` (contador) | El texto del contador cambia cada segundo sin región viva | Anunciar solo el vencimiento, no cada segundo | Un cambio cada segundo que el lector de pantalla no comunica |

---

## Considerados y descartados

| Ubicación | Candidato | Por qué se descartó |
|---|---|---|
| `ScrollingBackgroundDrawable.kt` | Movimiento decorativo que ignora `prefers-reduced-motion` | El fondo solo se desplaza con el scroll del usuario, no arranca solo: no es movimiento autónomo |
| `activity_user_menu.xml` | Dos acciones con el mismo peso, sin jerarquía | Son las dos entradas legítimas de un menú de dos opciones; el mismo peso es la forma correcta acá |
| `UsuarioCrud.kt` (eliminar) | Pide confirmación en vez de permitir deshacer | La operación es irreversible y la disciplina acepta confirmación en ese caso |
| Toda la app | No hay tema claro | Es una app de operación de un solo registro; ninguna regla pide un segundo tema |
| `activity_usuario_form.xml` | El `Switch` "Activo" viene encendido | Es el valor correcto para un alta; ninguna regla se rompe |

---

## Verificación

**Ejecutado**
- `./gradlew :app:assembleDebug` → `BUILD SUCCESSFUL`. El estado actual compila y AAPT valida los doce layouts.
- Búsqueda de colores sueltos en `res/layout/`: queda **1 archivo** (`item_lista.xml`).
- Búsqueda de `contentDescription` en `res/layout/`: **1** ocurrencia, el ícono de GitHub. Las dos imágenes tocables de sensores no tienen.
- Búsqueda de `android:hint`: **14** campos; etiquetas `TextView`: **0**.
- Búsqueda de `ScrollView`: **1** layout de 12.
- Lectura completa de los 12 layouts, `styles.xml`, `colors.xml`, `AGENTS.md` y `README.md`.

**No verificado**
- El contraste real sobre el fondo animado: hace falta verlo en el dispositivo.
- La visibilidad del foco en los campos y en los botones al navegar con teclado externo.
- El comportamiento con la fuente al 200% y en un ancho de 320dp.
- El estado vacío del listado y el aviso de fallo de sincronización: no los probé en ejecución, los deduzco del código.

Un vacío de verificación no es un hallazgo. Queda anotado como tal.

---

## Veredicto

**Block.** Cuatro hallazgos HIGH siguen en pie: dos controles sin nombre, catorce campos sin etiqueta y cuatro pantallas cuyo botón principal queda fuera de alcance con el teclado abierto.

---

## Siguientes modos

- `/design a11y` — etiquetas visibles, nombres en los controles, desplazamiento en los formularios y región viva en el contador. **Primero este.**
- `/design recolor` — un solo tono como acento y un par semántico para éxito y error.
- `/design typeset` — escala de cuatro pasos con razón mínima de 1.3.
- `/design surface` — estado vacío del listado y aviso de fallo de sincronización.
- `/design relayout` — jerarquía en la pantalla de sensores.
