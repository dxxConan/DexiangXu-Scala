
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object index_Scope0 {
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._

class index extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template1[String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(wsUrl: String):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.17*/("""

"""),format.raw/*3.1*/("""<!DOCTYPE html>
<html lang="en">
<head>
    <title>akka-stream-kafka activator</title>
    <script>
            var websocket = new WebSocket(""""),_display_(/*8.45*/wsUrl),format.raw/*8.50*/("""");
            websocket.onmessage = function(event) """),format.raw/*9.51*/("""{"""),format.raw/*9.52*/("""
                """),format.raw/*10.17*/("""var out = document.getElementById("out");
                out.textContent = event.data;
            """),format.raw/*12.13*/("""}"""),format.raw/*12.14*/("""
        """),format.raw/*13.9*/("""</script>
</head>
<body>
<h1 id="out">Connecting...</h1>
</body>
</html>"""))
      }
    }
  }

  def render(wsUrl:String): play.twirl.api.HtmlFormat.Appendable = apply(wsUrl)

  def f:((String) => play.twirl.api.HtmlFormat.Appendable) = (wsUrl) => apply(wsUrl)

  def ref: this.type = this

}


}

/**/
object index extends index_Scope0.index
              /*
                  -- GENERATED --
                  DATE: Wed Apr 26 16:26:48 CDT 2017
                  SOURCE: /Users/Conan/Desktop/DexiangXu-Scala/sample-projects/activator-reactive-kafka-scala/app/views/index.scala.html
                  HASH: d86d291fb931a26418545739b6732c0955a6848c
                  MATRIX: 527->1|637->16|665->18|835->162|860->167|941->221|969->222|1014->239|1142->339|1171->340|1207->349
                  LINES: 20->1|25->1|27->3|32->8|32->8|33->9|33->9|34->10|36->12|36->12|37->13
                  -- GENERATED --
              */
          