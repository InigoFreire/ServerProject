# Proyecto de Aplicación Cliente-Servidor

Este repositorio contiene tres proyectos principales: el Cliente, el Servidor y la Biblioteca de Intercambio. Cada uno de ellos puede ser explorado individualmente en GitHub a través de los siguientes enlaces:

- [Proyecto Cliente en GitHub](https://github.com/aitziberE/ClientProject.git)
- [Biblioteca de Intercambio en GitHub](https://github.com/AnderMoreno15/ExchangeLibrary.git)
- [Proyecto Servidor en GitHub](https://github.com/InigoFreire/ServerProject.git)

## Configuración y Parámetros de Propiedades

Para ejecutar y configurar correctamente los proyectos, se deben ajustar ciertos parámetros y archivos de propiedades. A continuación, se detallan las configuraciones para cada componente.

### Proyecto Cliente

Ubicación del archivo de propiedades: `src/resources/config.properties`

Parámetros clave:

- `IP`: Dirección IP del cliente (por defecto `127.0.0.1`)
- `PORT`: Puerto del cliente (por defecto `5000`)

Ejemplo del archivo `config.properties`:

```properties
IP = 127.0.0.1
PORT = 5000
```
### Proyecto Servidor

Ubicación del archivo de propiedades: `src/resources/config.properties`

Este archivo incluye configuraciones específicas para la conexión a la base de datos PostgreSQL. Asegúrate de ajustar el usuario y la contraseña de acuerdo a tu configuración de base de datos.

Parámetros clave:

- `DB_USER`: Nombre de usuario de la base de datos PostgreSQL
- `DB_PASSWORD`: Contraseña del usuario de la base de datos 
- `URL`: URL de conexión a PostgreSQL, que incluye la IP del servidor, el puerto (5432) y el nombre de la base de datos (ejemplo: `test`)
- `PORT`: Puerto del servidor (ejemplo: `3000`)
- `USER_CAP`: Capacidad máxima de usuarios permitidos (ejemplo: `10`)

Ejemplo del archivo `config.properties`:

```properties
# Configuración de la base de datos PostgreSQL
DB_USER = <tuUsuario>
DB_PASSWORD = <tuContraseña>
# URL de conexión para PostgreSQL (IP del servidor, puerto y nombre de la base de datos)
URL = jdbc:postgresql://192.168.13.130:5432/test
PORT = 3000
USER_CAP = 10
```

## Autores

- Iñigo Freire: [@InigoFreire](https://github.com/InigoFreire)
- Ander Moreno: [@AnderMoreno15](https://github.com/AnderMoreno15)
- Pablo Rodriguez: [@whereispebble](https://github.com/whereispebble)
- Aitziber Eskizabel: [@aitziberE](https://www.github.com/aitziberE)
