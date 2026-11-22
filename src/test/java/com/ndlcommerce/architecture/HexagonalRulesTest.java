package com.ndlcommerce.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;

@AnalyzeClasses(packages = "com.ndlcommerce", importOptions = ImportOption.DoNotIncludeTests.class)
public class HexagonalRulesTest {

  @ArchTest
  public static final ArchRule current_use_cases_should_not_depend_on_adapters =
      FreezingArchRule.freeze(
          noClasses()
              .that()
              .resideInAPackage("..useCase..")
              .should()
              .dependOnClassesThat()
              .resideInAPackage("..adapters..")
              .because("application use cases must not depend on infrastructure adapters"));

  @ArchTest
  public static final ArchRule current_use_cases_should_not_depend_on_config =
      FreezingArchRule.freeze(
          noClasses()
              .that()
              .resideInAPackage("..useCase..")
              .should()
              .dependOnClassesThat()
              .resideInAPackage("..config..")
              .because(
                  "configuration may wire application components, but application must not depend on configuration"));

  public static final ArchRule current_request_dtos_should_not_depend_on_jakarta =
      FreezingArchRule.freeze(
          noClasses()
              .that()
              .resideInAPackage("..useCase.request..")
              .should()
              .dependOnClassesThat()
              .resideInAPackage("jakarta..")
              .because("request DTOs in the application layer should be framework agnostic"));

  @ArchTest
  public static final ArchRule current_use_case_ports_should_be_interfaces =
      classes()
          .that()
          .resideInAPackage("..useCase.interfaces..")
          .should()
          .beInterfaces()
          .because("use case boundaries and gateways define contracts, not implementations");

  @ArchTest
  public static final ArchRule current_web_adapters_should_not_depend_on_persistence_adapters =
      noClasses()
          .that()
          .resideInAPackage("..adapters.web..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..adapters.persistence..", "..adapters.out..")
          .because("web adapters must call application boundaries instead of persistence directly");

  // Strict (not frozen, not allowEmpty): domain was migrated in T1-06 and must never be empty.
  @ArchTest
  public static final ArchRule domain_should_not_depend_on_outer_layers =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..application..", "..useCase..", "..adapters..", "..config..")
          .because("the domain is the innermost layer and must not depend on outer layers");

  @ArchTest
  public static final ArchRule domain_should_not_depend_on_frameworks =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("jakarta..", "org.springframework..", "com.fasterxml.jackson..")
          .because(
              "the domain must not depend on framework, persistence, or serialization details");

  // Target packages are empty until migration; legacy rules above remain active meanwhile.
  @ArchTest
  public static final ArchRule target_application_should_not_depend_on_adapters =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapters..")
          .because(
              "application policies and ports must remain independent of delivery and persistence adapters")
          .allowEmptyShould(true);

  @ArchTest
  public static final ArchRule target_application_should_not_depend_on_config_or_frameworks =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..config..", "jakarta..", "org.springframework..")
          .because(
              "application logic should remain framework independent; configuration belongs at the boundary")
          .allowEmptyShould(true);

  @ArchTest
  public static final ArchRule target_inbound_adapters_should_not_depend_on_outbound_adapters =
      noClasses()
          .that()
          .resideInAPackage("..adapters.in..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..adapters.out..", "..adapters.persistence..")
          .because(
              "inbound adapters must use application ports rather than bypassing them for outbound adapters")
          .allowEmptyShould(true);

  @ArchTest
  public static final ArchRule target_application_ports_should_be_interfaces =
      classes()
          .that()
          .resideInAPackage("..application.port..")
          .should()
          .beInterfaces()
          .because("application ports describe contracts implemented by use cases and adapters")
          .allowEmptyShould(true);

  @ArchTest
  public static final ArchRule current_packages_should_be_free_of_cycles =
      FreezingArchRule.freeze(
          slices()
              .matching("com.ndlcommerce.(*)..")
              .should()
              .beFreeOfCycles()
              .because(
                  "cyclic package dependencies undermine independent layers and inward dependency flow"));
}
