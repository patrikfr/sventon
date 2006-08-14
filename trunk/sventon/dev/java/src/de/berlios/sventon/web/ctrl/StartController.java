package de.berlios.sventon.web.ctrl;

import de.berlios.sventon.config.ApplicationConfiguration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * StartController checks how many instances are configured and
 * redirects to appropriate page. If only one instance is configured
 * the controller will redirect to the repository browser view.
 * In any other case, the {@link ListInstancesController} will be called
 * so that the user can choose which repository instance to browse.
 *
 * @author jesper@users.berlios.de
 */
public class StartController extends AbstractController {

  /**
   * The application configuration. Used to get all instance names.
   */
  private ApplicationConfiguration configuration;

  protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
      throws Exception {

    final ModelAndView modelAndView;
    if (configuration.getInstanceCount() > 1) {
      modelAndView = new ModelAndView(new RedirectView("listinstances.svn"));
    } else if (configuration.getInstanceCount() == 1) {
      final String instanceName = configuration.getInstanceNames().iterator().next();
      modelAndView = new ModelAndView(new RedirectView("repobrowser.svn?name=" + instanceName));
    } else {
      throw new IllegalStateException("No instance has been configured!");
    }
    return modelAndView;
  }

  /**
   * Sets the application configuration.
   *
   * @param configuration Configuration.
   */
  public void setConfiguration(final ApplicationConfiguration configuration) {
    this.configuration = configuration;
  }

}
