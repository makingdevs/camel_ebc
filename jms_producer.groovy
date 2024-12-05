@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.activemq', module='activemq-all', version='5.16.5'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12')
])

import javax.jms.*
import org.apache.activemq.ActiveMQConnectionFactory

// Configuración de ActiveMQ
String brokerUrl = "tcp://localhost:61616" // Cambia esto si tu ActiveMQ usa otro puerto o protocolo
String queueName = "orders"

// Crear una conexión a ActiveMQ
ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl)
Connection connection = connectionFactory.createConnection()
connection.start()

// Crear sesión y cola
Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
Destination destination = session.createQueue(queueName)

// Crear un productor
MessageProducer producer = session.createProducer(destination)
producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT)

// Enviar mensajes a la cola
println "Enviando mensajes a la cola JMS '${queueName}'..."

// def orders = [
//   [id: 1, type: 'electronic', group: 'A'],
//   [id: 2, type: 'clothes', group: 'B'],
//   [id: 3, type: 'electronic', group: 'A'],
//   [id: 4, type: 'clothes', group: 'B'],
//   [id: 5, type: 'electronic', group: 'A'],
//   [id: 6, type: 'clothes', group: 'B']
// ]

// orders.each { order ->
//   TextMessage message = session.createTextMessage(order.toString())
//   message.setStringProperty("type", order.type)
//   message.setStringProperty("group", order.group)
//   producer.send(message)
//   println "Mensaje enviado: ${order}"
// }

(1..5).each { i ->
  String messageContent = "Order ${i} - Priority: ${(i % 2 == 0) ? 'High' : 'Low'}"
  TextMessage message = session.createTextMessage(messageContent)
  message.setStringProperty("type", (i % 2 == 0) ? "electronics" : "clothing")
  message.setStringProperty("priority", (i % 2 == 0) ? "high" : "low")
  producer.send(message)
  println "Mensaje enviado: ${messageContent}"
}

// Cerrar recursos
producer.close()
session.close()
connection.close()

println "Todos los mensajes han sido enviados."
