
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/Conan/Desktop/DexiangXu-Scala/sample-projects/activator-reactive-kafka-scala/conf/routes
// @DATE:Wed Apr 26 16:26:47 CDT 2017


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
