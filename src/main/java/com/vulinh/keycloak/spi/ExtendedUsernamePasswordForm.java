package com.vulinh.keycloak.spi;

import java.util.Map;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.events.Errors;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;

public class ExtendedUsernamePasswordForm extends UsernamePasswordForm {

  private static final Map<String, String> ERROR_MAPS =
      Map.ofEntries(
          Map.entry(Errors.USER_DISABLED, "brute-force-permanent-disabled"),
          Map.entry(Errors.USER_TEMPORARILY_DISABLED, "brute-force-temporary-disabled"));

  @Override
  protected boolean isDisabledByBruteForce(AuthenticationFlowContext context, UserModel user) {
    var bruteForceError = AuthenticatorUtils.getDisabledByBruteForceEventError(context, user);

    if (bruteForceError != null) {
      context.getEvent().user(user);
      context.getEvent().error(bruteForceError);

      var challengeResponse =
          challenge(
              context,
              ERROR_MAPS.getOrDefault(bruteForceError, Messages.INVALID_USER),
              disabledByBruteForceFieldError());

      context.forceChallenge(challengeResponse);

      return true;
    }

    return false;
  }
}
