# Categoría: Estado del Ítem

Esta sección documenta los efectos correspondientes a **Estado del Ítem** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `add_socket`

- **Identificador / Alias:** `ADD_SOCKET`
- **Descripción:** Añade un nuevo zócalo/socket vacío al ítem.

### Ejemplo de Configuración
```yaml
effects:
 - type: ADD_SOCKET
    socket: gem
```

---

## `add_upgrade`

- **Identificador / Alias:** `ADD_UPGRADE`
- **Descripción:** Aumenta en +1 el nivel de mejora del ítem invocando el motor de upgrades.

### Ejemplo de Configuración
```yaml
effects:
 - type: ADD_UPGRADE
```

---

## `add_variable`

- **Identificador / Alias:** `ADD_VARIABLE`
- **Descripción:** Suma una cantidad numérica a una variable persistente del ítem.

### Ejemplo de Configuración
```yaml
effects:
 - type: ADD_VARIABLE
    variable: combo
    amount: 1
```

---

## `cancel_event`

- **Identificador / Alias:** `CANCEL_EVENT`
- **Descripción:** Cancela el evento nativo de Bukkit que originó el trigger (previene el daño, la rotura del bloque o el clic).

### Ejemplo de Configuración
```yaml
effects:
 - type: CANCEL_EVENT
```

---

## `modify_variable`

- **Identificador / Alias:** `MODIFY_VARIABLE`
- **Descripción:** Permite operaciones de `INCREMENT`, `DECREMENT` o `SET` sobre variables.

### Ejemplo de Configuración
```yaml
effects:
  - type: MODIFY_VARIABLE
```

---

## `remove_variable`

- **Identificador / Alias:** `REMOVE_VARIABLE`
- **Descripción:** Elimina una variable almacenada.

### Ejemplo de Configuración
```yaml
effects:
 - type: REMOVE_VARIABLE
    variable: combo
```

---

## `set_durability`

- **Identificador / Alias:** `SET_DURABILITY`
- **Descripción:** Modifica directamente la durabilidad actual del ítem.

### Ejemplo de Configuración
```yaml
effects:
 - type: SET_DURABILITY
    durability: 500
```

---

## `set_variable`

- **Identificador / Alias:** `SET_VARIABLE`
- **Descripción:** Establece el valor de una variable del ítem.

### Ejemplo de Configuración
```yaml
effects:
 - type: SET_VARIABLE
    variable: combo
    value: 0
```

---

