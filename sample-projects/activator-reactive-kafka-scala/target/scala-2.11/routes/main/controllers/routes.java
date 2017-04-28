
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/Conan/Desktop/DexiangXu-Scala/sample-projects/activator-reactive-kafka-scala/conf/routes
// @DATE:Wed Apr 26 16:26:47 CDT 2017

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseHomeController HomeController = new controllers.ReverseHomeController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseHomeController HomeController = new controllers.javascript.ReverseHomeController(RoutesPrefix.byNamePrefix());
  }

}
