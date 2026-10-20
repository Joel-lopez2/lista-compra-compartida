# Diseño de la aplicación — Lista de la compra compartida

Estilo: minimalista y funcional, inspirado en la app Recordatorios de iOS.
Material 3 con una paleta verde (fresco, asociado a la compra) y mucho espacio en blanco.

## 1. Mapa de navegación

```
Login <-> Registro
   |
   v
Mis listas ----> Detalle de lista ----> Diálogo "Compartir / Miembros"
   |                   |
   |                   +--> Diálogo "Nueva / editar categoría"
   |                   +--> Diálogo "Nuevo / editar producto"
   +--> Diálogo "Nueva / renombrar lista"
```

Rutas (Navigation Compose): `login`, `register`, `lists`, `detail/{listId}`.
Si hay sesión iniciada la app arranca en `lists`; si no, en `login`.

## 2. Pantallas

### 2.1 Login

```
+------------------------------+
|                              |
|          🛒                  |
|    Lista de la compra        |
|                              |
|  [ Correo electrónico      ] |
|  [ Contraseña           👁 ] |
|                              |
|  [      Iniciar sesión     ] |
|                              |
|  ¿No tienes cuenta? Regístrate|
+------------------------------+
```
- Campos obligatorios; correo con formato válido.
- Errores bajo el campo (ej. "Correo o contraseña incorrectos").
- Indicador de progreso mientras se comprueba.

### 2.2 Registro

```
+------------------------------+
|  <  Crear cuenta             |
|                              |
|  [ Nombre                  ] |
|  [ Correo electrónico      ] |
|  [ Contraseña (mín. 6)  👁 ] |
|  [ Repetir contraseña   👁 ] |
|                              |
|  [      Registrarme        ] |
+------------------------------+
```
- Contraseña de 6 caracteres o más y coincidencia entre las dos.
- Al registrarse se crea el documento `users/{uid}`.

### 2.3 Mis listas

```
+------------------------------+
|  Mis listas            ⎋ [salir]
|                              |
|  MIS LISTAS                  |
|  +------------------------+  |
|  | Compra semanal      ⋮ |  |
|  | 12 productos           |  |
|  +------------------------+  |
|  | Fiesta cumpleaños   ⋮ |  |
|  +------------------------+  |
|                              |
|  COMPARTIDAS CONMIGO         |
|  +------------------------+  |
|  | Casa de la playa    👥 |  |
|  | de ana@correo.com      |  |
|  +------------------------+  |
|                          (+) |
+------------------------------+
```
- Dos secciones: propias y compartidas. Las compartidas llevan el icono 👥 y el propietario.
- Menú ⋮ en las propias: Renombrar, Compartir, Eliminar (con confirmación).
- En las compartidas: Salir de la lista.
- Botón flotante (+) para crear una lista (nombre obligatorio).
- Estado vacío: "Aún no tienes listas. Pulsa + para crear la primera".

### 2.4 Detalle de lista

```
+------------------------------+
|  <  Compra semanal   👥  ⋮   |
|                              |
|  🥛 LÁCTEOS                  |
|   ( ) Leche            2 L   |
|   ( ) Yogur                  |
|                              |
|  🍎 FRUTAS Y VERDURAS        |
|   ( ) Manzanas         1 kg  |
|                              |
|  SIN CATEGORÍA               |
|   ( ) Pilas AA               |
|                              |
|  COMPLETADOS (2)          v  |
|   (x) ~~Pan~~                |
|   (x) ~~Huevos~~             |
|                              |
|  [ Añadir producto...    ] + |
+------------------------------+
```
- Productos agrupados por categoría, con la categoría en mayúsculas pequeñas.
- Pulsar el círculo marca/desmarca como completado (tachado y gris; pasa a la sección
  "Completados", que se puede plegar). Nunca se elimina al completarlo.
- Pulsar el producto abre el diálogo de edición; deslizar o menú para eliminar.
- Barra inferior para añadir rápido: se escribe el nombre y se confirma.
  (Más adelante: la categoría se propondrá sola con el extra de autoclasificación.)
- Menú ⋮: Nueva categoría, Gestionar categorías, Compartir.

### 2.5 Diálogos

**Producto:** nombre (obligatorio), cantidad (opcional), categoría (desplegable, incluye "Sin categoría").
**Categoría:** nombre (obligatorio), icono/emoji (opcional).
**Compartir:** campo de correo + botón "Compartir"; debajo, lista de miembros con botón de quitar
(solo el propietario). Mensajes: "Ese usuario no está registrado", "Ya tiene acceso".
**Eliminar:** confirmación con el nombre del elemento. Al borrar una categoría, sus productos pasan a "Sin categoría".

## 3. Estilo visual

### Paleta

| Rol | Claro | Oscuro |
|---|---|---|
| Primario (verde) | `#2E7D4F` | `#7FD6A0` |
| Sobre primario | `#FFFFFF` | `#00391C` |
| Contenedor primario | `#B7F0CB` | `#005229` |
| Secundario | `#4F6354` | `#B5CCB9` |
| Terciario (acento naranja) | `#B5651D` | `#FFB77C` |
| Fondo | `#F7F9F5` | `#101411` |
| Superficie | `#FFFFFF` | `#181D19` |
| Error | `#BA1A1A` | `#FFB4AB` |

Se desactiva el color dinámico de Android 12+ para que la app tenga siempre la misma identidad
(y las capturas de la documentación sean coherentes).

### Tipografía y formas
- Tipografía por defecto de Material 3 (Roboto); títulos en `titleLarge`, categorías en `labelLarge` en mayúsculas.
- Esquinas redondeadas de 16 dp en tarjetas y 12 dp en campos.
- Márgenes laterales de 16 dp; separación de 8 dp entre elementos de lista.

### Componentes Compose previstos
`Scaffold` + `TopAppBar`, `LazyColumn`, `Card`, `OutlinedTextField`, `Checkbox`/`IconToggleButton`,
`FloatingActionButton`, `AlertDialog`, `DropdownMenu`, `Snackbar` para errores y confirmaciones.

## 4. Textos principales (español)

Todos los textos irán en `res/values/strings.xml` (nada escrito a mano en el código).
Ejemplos: `login_title`, `email_label`, `password_label`, `lists_empty`, `new_list`,
`share_list`, `user_not_found`, `completed_section`, `uncategorized`.
