@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-http', version='4.8.1'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12')
])

import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.builder.RouteBuilder

CamelContext camelContext = new DefaultCamelContext()

camelContext.addRoutes(new RouteBuilder() {
  @Override
  void configure() {

    from("timer:fileGenerator?period=5000")
      .setBody(simple('Archivo generado a las: ${date:now:dd-MM-yyyy HH:mm:ss}'))
      .to('file://output?fileName=data-${date:now:dd-MM-yyyyHH:mm:ss}.txt')
      .log('Archivo creado: ${body}')

    // Ruta 2: Leer archivos y enviar el contenido a un servidor HTTP
    from("file://output?noop=true") // Leer archivos sin moverlos
      .log('Archivo leído: ${headers}')
      .setHeader("CamelHttpMethod", constant("POST")) // Configurar el método HTTP
      .to("http://httpbin.org/post") // Enviar el contenido al servidor HTTP
      .log("Respuesta del servidor: ${body()}")
      .log('Encabezados del response: ${headers}')
  }
})

camelContext.start()

addShutdownHook {
  camelContext.stop()
}
synchronized(this) { this.wait() }
