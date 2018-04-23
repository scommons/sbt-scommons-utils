package modules

import controllers.ExampleController
import play.api.mvc.ControllerComponents
import scaldi.Module

class ApplicationModule extends Module {

  private implicit lazy val components = inject[ControllerComponents]

  bind[ExampleController] to new ExampleController()
}
