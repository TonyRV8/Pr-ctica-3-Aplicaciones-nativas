GestorDeArchivos 📂

GestorDeArchivos es un explorador de archivos nativo para Android, desarrollado íntegramente en Kotlin. Su objetivo es ofrecer una experiencia de usuario fluida y eficiente para navegar por el almacenamiento del dispositivo, gestionar carpetas y archivos, y visualizar diversos tipos de contenido multimedia directamente desde la aplicación.

✨ Características Principales

Este proyecto implementa las funcionalidades esenciales de un administrador de archivos moderno y robusto:

🧭 Navegación Intuitiva

Exploración de Archivos: Navega por el almacenamiento interno y externo de tu dispositivo con una interfaz clara basada en listas.

Navegación "Breadcrumb": Una barra de navegación superior muestra la ruta actual y permite saltar rápidamente a cualquier directorio padre con un solo toque.

🛠️ Gestión Completa de Archivos

Crear: Genera nuevas carpetas para organizar tus archivos.

Renombrar: Cambia el nombre de archivos y directorios fácilmente.

Eliminar: Borra elementos no deseados de forma segura con diálogos de confirmación.

Copiar, Cortar y Pegar: Mueve o duplica archivos entre diferentes ubicaciones utilizando un portapapeles interno.

Selección Múltiple: Realiza acciones en lote (borrar, mover, copiar) seleccionando varios elementos a la vez mediante una pulsación larga.

👁️ Visores Integrados

Visor de Texto: Abre y lee archivos de texto sin formato como .txt, .md, .log, .json, y .xml sin salir de la app.

Visor de Imágenes Avanzado: Visualiza tus fotos e imágenes (formatos .jpg, .png, .webp, etc.) con herramientas integradas para hacer zoom, desplazarte por la imagen y rotarla.

🔍 Búsqueda y Organización

Búsqueda Potente: Encuentra archivos rápidamente por su nombre.

Filtros de Búsqueda: Refina tus búsquedas por tipo de archivo (ej. type:pdf) o por fecha de modificación (ej. date:2023-10-27).

Favoritos: Marca tus archivos y carpetas más usados para un acceso rápido. Filtra la vista principal para mostrar solo tus favoritos.

Recientes: Accede rápidamente a los últimos 10 archivos que has abierto a través del historial en el menú.

🎨 Personalización

Temas Dinámicos: Elige entre diferentes esquemas de color, como "Guinda IPN" o "Azul ESCOM".

Modo Oscuro/Claro: Selecciona tu preferencia de modo (Claro, Oscuro o según el Sistema) para cada tema.

🚀 Tecnologías Utilizadas

Lenguaje de Programación: Kotlin - Moderno, conciso y seguro.

Arquitectura: Aplicación nativa de Android siguiendo las mejores prácticas.

Componentes de Android Jetpack:

AppCompat, Activity KTX, ConstraintLayout, RecyclerView para una UI robusta y reactiva.

Material Components para un diseño moderno y consistente.

Librerías de Terceros:

Glide: Para una carga y visualización de imágenes rápida y eficiente.

⚙️ Instalación y Configuración

Sigue estos pasos para ejecutar el proyecto en tu entorno de desarrollo local:

Clonar el Repositorio:
Abre tu terminal y ejecuta el siguiente comando:

git clone [https://github.com/tu-usuario/GestorDeArchivos.git](https://github.com/tu-usuario/GestorDeArchivos.git)


Abrir en Android Studio:
Inicia Android Studio y selecciona "Open an Existing Project". Navega hasta el directorio donde clonaste el repositorio y selecciónalo.

Sincronización de Gradle:
Espera a que Android Studio sincronice el proyecto y descargue todas las dependencias necesarias.

Ejecución:
Conecta tu dispositivo Android o inicia un emulador. Haz clic en el botón "Run" (el triángulo verde) en Android Studio.

Nota Importante: La primera vez que inicies la aplicación, se te solicitará el permiso de "Acceso a todos los archivos". Este permiso es indispensable para que la aplicación pueda funcionar correctamente como un gestor de archivos en versiones modernas de Android (Android 11+).

🤝 Contribuciones

¡Las contribuciones son bienvenidas! Si tienes ideas para mejorar la aplicación, encuentras algún error o quieres añadir nuevas funcionalidades, no dudes en abrir un issue o enviar un pull request.

📄 Licencia

Este proyecto está bajo la Licencia MIT - siéntete libre de usarlo y modificarlo.
