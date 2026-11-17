package com.ndlcommerce.architecture;

import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

// @AnalyzeClasses: ArchUnit imports the compiled bytecode (.class) of these packages once
// and shares the result with every @ArchTest field/method in this class.
// DoNotIncludeTests removes target/test-classes, so only production code is checked.
@AnalyzeClasses(packages = "com.ndlcommerce", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureSmokeTest {

  // Each @ArchTest field is a rule. ArchUnit runs it against the imported classes
  // and fails the test with a readable message listing every violation.
  @ArchTest
  static final ArchRule no_classes_should_use_java_util_logging =
      NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
}
