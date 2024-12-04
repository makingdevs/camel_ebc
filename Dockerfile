# Usar la imagen base oficial de ActiveMQ
FROM rmohr/activemq:latest

# Establecer variables de entorno (opcional)
# Cambiar si necesitas un usuario/contraseña personalizados
ENV ACTIVEMQ_ADMIN_LOGIN=admin
ENV ACTIVEMQ_ADMIN_PASSWORD=admin
ENV ACTIVEMQ_CONFIG_MINMEMORY=512
ENV ACTIVEMQ_CONFIG_MAXMEMORY=2048

# Exponer los puertos
# 61616: Protocolo TCP para conexiones de cliente (JMS)
# 8161: Consola web de administración
EXPOSE 61616 8161

# Iniciar ActiveMQ cuando se inicie el contenedor
CMD ["/bin/sh", "-c", "bin/activemq console"]

# docker build -t activemq-custom .

# docker run -d --name activemq \
#   -p 61616:61616 \
#   -p 8161:8161 \
#   activemq-custom
