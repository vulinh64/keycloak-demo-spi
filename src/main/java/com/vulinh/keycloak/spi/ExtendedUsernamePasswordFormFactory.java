package com.vulinh.keycloak.spi;

import java.util.Collections;
import java.util.List;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

public class ExtendedUsernamePasswordFormFactory implements AuthenticatorFactory {

  public static final ExtendedUsernamePasswordForm INSTANCE = new ExtendedUsernamePasswordForm();

  private static final Requirement[] REQUIREMENT_CHOICES = {Requirement.REQUIRED};

  private static final String PROVIDER_ID = "0000-auth-username-password-form";
  private static final String DISPLAY_TYPE = "0000/New Username Password Form";
  private static final String HELP_TEXT =
      "Replace Username Password Form with different messages when enforced by brute-force protection";

  @Override
  public Authenticator create(KeycloakSession session) {
    return INSTANCE;
  }

  @Override
  public void init(Config.Scope config) {
    // Do nothing
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    // Do nothing
  }

  @Override
  public void close() {
    // FFS please look at UsernamePasswordForm
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String getReferenceCategory() {
    return PasswordCredentialModel.TYPE;
  }

  @Override
  public boolean isConfigurable() {
    return false;
  }

  @Override
  public Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
  }

  @Override
  public String getDisplayType() {
    return DISPLAY_TYPE;
  }

  @Override
  public String getHelpText() {
    return HELP_TEXT;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return Collections.emptyList();
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }
}
