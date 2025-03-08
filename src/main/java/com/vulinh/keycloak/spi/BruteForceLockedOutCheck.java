package com.vulinh.keycloak.spi;

import static org.keycloak.authentication.authenticators.util.AuthenticatorUtils.getDisabledByBruteForceEventError;

import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.authentication.authenticators.directgrant.ValidateUsername;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BruteForceLockedOutCheck extends ValidateUsername {

  private static final String ID = "0000-check-brute-force-locked-out";
  private static final String DISPLAY_TYPE = "0000/Username Validation/Brute-force Check";
  private static final String HELP_TEXT =
      "Replace Username Validation step, check if a user gets locked out due to Brute-force Protection mechanism";

  private static final Map<String, List<String>> ERROR_MAPPING =
      Map.ofEntries(
          Map.entry(
              "user_disabled",
              List.of(
                  "brute_force_user_permanent_lock",
                  "User has been permanently locked out due to Brute-force Protection policy")),
          Map.entry(
              "user_temporarily_disabled",
              List.of(
                  "brute_force_temporary_locked_out",
                  "User has attempted too many quick successive logins, please wait for a while")));

  private static final String GENERIC_INVALID_REQUEST = "invalid_request";
  private static final String GENERIC_INVALID_GRANT = "invalid_grant";
  private static final String GENERIC_MESSAGE_INVALID_CREDENTIALS = "Invalid user credentials";

  private static final Logger log = LoggerFactory.getLogger(BruteForceLockedOutCheck.class);

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    var username = retrieveUsername(context);

    // Blank input username -> 400 invalid_request
    if (username == null) {
      context.getEvent().error(Errors.USER_NOT_FOUND);
      Response challengeResponse =
          errorResponse(
              Response.Status.BAD_REQUEST.getStatusCode(),
              GENERIC_INVALID_REQUEST,
              "Missing parameter: username");
      context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
      return;
    }

    context.getEvent().detail(Details.USERNAME, username);
    context
        .getAuthenticationSession()
        .setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

    try {
      var user =
          KeycloakModelUtils.findUserByNameOrEmail(
              context.getSession(), context.getRealm(), username);

      continueFlowWithNonBlankUsername(context, user);
    } catch (ModelDuplicateException mde) {
      handleModelDuplication(context, mde);
    }
  }

  private void continueFlowWithNonBlankUsername(AuthenticationFlowContext context, UserModel user) {
    // No such a username
    if (user == null) {
      context.getEvent().error(Errors.USER_NOT_FOUND);
      var challengeResponse =
          errorResponse(
              Response.Status.UNAUTHORIZED.getStatusCode(),
              GENERIC_INVALID_GRANT,
              GENERIC_MESSAGE_INVALID_CREDENTIALS);
      context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
      return;
    }

    var bruteForceError = getDisabledByBruteForceEventError(context, user);

    // the value here should be "user_disabled"
    // in case of temporary quick login prevention, the value is "user_temporarily_disabled"

    if (bruteForceError != null) {
      context.getEvent().user(user);
      context.getEvent().error(bruteForceError);

      var challengeResponse =
          errorResponse(
              Response.Status.UNAUTHORIZED.getStatusCode(),
              ERROR_MAPPING.getOrDefault(bruteForceError, List.of(GENERIC_INVALID_GRANT)).get(0),
              ERROR_MAPPING
                  .getOrDefault(bruteForceError, List.of("", GENERIC_MESSAGE_INVALID_CREDENTIALS))
                  .get(1));

      context.forceChallenge(challengeResponse);

      return;
    }

    if (!user.isEnabled()) {
      context.getEvent().user(user);
      context.getEvent().error(Errors.USER_DISABLED);
      Response challengeResponse =
          errorResponse(
              Response.Status.UNAUTHORIZED.getStatusCode(),
              GENERIC_INVALID_GRANT,
              "Account disabled");
      context.forceChallenge(challengeResponse);
      return;
    }
    context.setUser(user);
    context.success();
  }

  @Override
  public String getDisplayType() {
    return DISPLAY_TYPE;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getHelpText() {
    return HELP_TEXT;
  }

  private static void handleModelDuplication(
      AuthenticationFlowContext context, ModelDuplicateException mde) {
    log.warn("Model duplication exception", mde);

    var challengeResponse =
        Response.status(Response.Status.UNAUTHORIZED.getStatusCode())
            .entity(
                new OAuth2ErrorRepresentation(
                    GENERIC_INVALID_REQUEST, GENERIC_MESSAGE_INVALID_CREDENTIALS))
            .type(MediaType.APPLICATION_JSON_TYPE)
            .build();

    context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
  }
}
