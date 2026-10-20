package com.reminder.financial.api.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.core.importer.ImportOption;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.reminder.financial.api", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainsNaoDependemDeInfrastructureNemAdapters = noClasses()
            .that().resideInAPackage("..domains..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..adapters..");
}
