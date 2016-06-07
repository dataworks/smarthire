package applicant.nlp

import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._

/**
 * Scala Test Spec to test the EntityGrabber
 * ****Need to add an actual test****
 */
 class EntityGrabberSpec extends FlatSpec {
   "Execute" must "grab entities" in {
     val models = List[String]("model/nlp/en-ner-degree.bin", "model/nlp/en-ner-location.bin", "model/nlp/en-ner-organization.bin", "model/nlp/en-ner-person.bin", "model/nlp/en-ner-school.bin", "model/nlp/en-ner-title.bin")
     val patterns = "model/nlp/regex.txt"
     val grabber = new EntityGrabber(models, patterns)

     val test = grabber.execute("Jhansi M Java/J2EE Developer State Street - Email me on Indeed: indeed.com/ EDUCATION Bachelors in Computer Science JNTU ADDITIONAL INFORMATION Technical Skills: Languages C, C++, Java , PL/SQL and Objective-C. J2EE Technologies Servlets, Struts 1.2/2, Hibernate , Spring, Log4j, Web services using SOAP And RESTful, XML, JDBC, EJB, JSP , JDBC,JSF 1.2/2.0, JMS, Groovy. Databases MySQL, ORACLE , NoSQL , DB2 and MongoDB. Web Technologies JSP, JSF, AngularJS, Bootstrap, AJAX , Java Script, EXT JS, XML, HTML, XML, CSS 3 Tools & Utilities Enterprise Architect, Visual SourceSafe, Rational Rose 200, Ant, JIRA, Netbeans, Cruise Control, Eclipse, Rational ClearCase/ClearQuest Domain Knowledge Banking, Insurance, Consulting and Healthcare IDE Eclipse, Spring Tool Suite, Jdeveloper, RAD and Net Beans. Application Servers Websphere, Weblogic, Jetty, Jboss and Tomcat. Platforms Windows, Unix, AIX, Linux and Mac OS X.")
  }
}
