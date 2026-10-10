# CategorÃ­a: Estado del Ítem

Esta secciÃ³n documenta los efectos correspondientes a **Estado del Ítem** disponibles en **ZenCore** y compartidos por **ZenCustomItems** y **ZenEnchants**.

---

## `add_socket`

- **Identificador / Alias:** `ADD_SOCKET`
- **DescripciÃ³n:** Añade un nuevo zócalo/socket vacío al ítem.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: ADD_SOCKET
    socket: gem
```

---

## `add_upgrade`

- **Identificador / Alias:** `ADD_UPGRADE`
- **DescripciÃ³n:** Aumenta en +1 el nivel de mejora del ítem invocando el motor de upgrades.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: ADD_UPGRADE
```

---

## `add_variable`

- **Identificador / Alias:** `ADD_VARIABLE`
- **DescripciÃ³n:** Suma una cantidad numérica a una variable persistente del ítem.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: ADD_VARIABLE
    variable: combo
    amount: 1
```

---

## `cancel_event`

- **Identificador / Alias:** `CANCEL_EVENT`
- **DescripciÃ³n:** Cancela el evento nativo de Bukkit que originó el trigger (previene el daño, la rotura del bloque o el clic).

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: CANCEL_EVENT
```

---

## `modify_variable`

- **Identificador / Alias:** `MODIFY_VARIABLE`
- **DescripciÃ³n:** Permite operaciones de `INCREMENT`, `DECREMENT` o `SET` sobre variables.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
  - type: MODIFY_VARIABLE
```

---

## `remove_variable`

- **Identificador / Alias:** `REMOVE_VARIABLE`
- **DescripciÃ³n:** Elimina una variable almacenada.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: REMOVE_VARIABLE
    variable: combo
```

---

## `set_durability`

- **Identificador / Alias:** `SET_DURABILITY`
- **DescripciÃ³n:** Modifica directamente la durabilidad actual del ítem.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: SET_DURABILITY
    durability: 500
```

---

## `set_variable`

- **Identificador / Alias:** `SET_VARIABLE`
- **DescripciÃ³n:** Establece el valor de una variable del ítem.

### Ejemplo de ConfiguraciÃ³n
```yaml
effects:
 - type: SET_VARIABLE
    variable: combo
    value: 0
```

---

