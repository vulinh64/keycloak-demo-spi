package com.vulinh.keycloak.spi;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;

public class ExtendedUsernamePasswordForm extends UsernamePasswordForm {

  @Override
  protected boolean isDisabledByBruteForce(AuthenticationFlowContext context, UserModel user) {
    var bruteForceError = AuthenticatorUtils.getDisabledByBruteForceEventError(context, user);

    if (bruteForceError != null) {
      context.getEvent().user(user);
      context.getEvent().error(bruteForceError);

      var challengeResponse =
          challenge(
              context,
              ExtendedUsernamePasswordFormFactory.ERROR_MAPS.getOrDefault(
                  bruteForceError, Messages.INVALID_USER),
              disabledByBruteForceFieldError());

      context.forceChallenge(challengeResponse);

      return true;
    }

    return false;
  }
}
