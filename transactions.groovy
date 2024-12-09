@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-jdbc', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-file', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-spring', version='4.8.1'),
  @Grab(group='org.springframework', module='spring-jdbc', version='5.3.39'),
  @Grab(group='org.xerial', module='sqlite-jdbc', version='3.47.1.0'),
  @Grab(group='org.apache.commons', module='commons-dbcp2', version='2.13.0'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12')
])

import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.commons.dbcp2.BasicDataSource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.apache.camel.spring.spi.SpringTransactionPolicy

def dbUrl = "jdbc:sqlite:transactions.db"
def dbDriver = "org.sqlite.JDBC"
def dataSource = new BasicDataSource(driverClassName: dbDriver, url: dbUrl)

CamelContext camelContext = new DefaultCamelContext()
camelContext.registry.bind("transactions_db", dataSource)

// @Autowired
// TransactionManager transactionManager

PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource)
camelContext.registry.bind("transactionManager", transactionManager)

def transactionPolicy = new SpringTransactionPolicy(transactionManager: transactionManager)
camelContext.registry.bind("PROPAGATION_REQUIRED", transactionPolicy)

def createTable = """
create table if not exists transactions (
  id integer primary key autoincrement,
  description text,
  status text
);
"""
dataSource.connection.prepareStatement(createTable).execute()

camelContext.addRoutes(new RouteBuilder() {
  @Override
  void configure() {

    onException(Exception)
      .maximumRedeliveries(3)
      .redeliveryDelay(2000)
      .handled(true)
      .to("log:error")
      .to("file://errors")

    from("file://input?noop=true")
      .log('Procesando archivo: ${headers}')
      .transacted("PROPAGATION_REQUIRED")
      .process { exchange ->
        def body = exchange.in.getBody(String)
        if(body.contains("FAIL"))
          throw new RuntimeException("ERROR SIMULADO")
      }
      .process { exchange ->
        def body = exchange.in.getBody(String)
        def sql = "INSERT INTO transactions(description, status) VALUES ('${body}', 'SUCCESS')"
        exchange.in.setBody(sql)
      }
      .to('file://insertions?fileName=data-${date:now:dd-MM-yyyyHH:mm:ss}.txt')
      //.to("jms:queue:insertions")
      .to("jdbc:transactions_db")
  }
})

camelContext.start()

addShutdownHook {
  camelContext.stop()
}
synchronized(this) { this.wait() }
