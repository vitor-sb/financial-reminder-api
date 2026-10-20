package com.reminder.financial.api.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.core.importer.ImportOption;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.reminder.financial.api", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainsNaoDependemDeInfrastructureNemAdapters = noClasses()
            .that().resideInAPackage("..domains..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..adapters..");

    @ArchTest
    static final ArchRule domainsNaoDependemDeApplication = noClasses()
            .that().resideInAPackage("..domains..")
            .should().dependOnClassesThat().resideInAPackage("..application..");

    @ArchTest
    static final ArchRule applicationNaoDependeDeSpringData = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework.data..");

    @ArchTest
    static final ArchRule applicationDependeSoDeDomains = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..adapters..");

    @ArchTest
    static final ArchRule adaptersNaoImportamInfrastructure = noClasses()
            .that().resideInAPackage("..adapters..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule adaptersInNaoDependemDeAdaptersOut = noClasses()
            .that().resideInAPackage("..adapters.in..")
            .should().dependOnClassesThat().resideInAPackage("..adapters.out..");

    @ArchTest
    static final ArchRule adaptersOutNaoDependemDeAdaptersIn = noClasses()
            .that().resideInAPackage("..adapters.out..")
            .should().dependOnClassesThat().resideInAPackage("..adapters.in..");

    @ArchTest
    static final ArchRule domainsEApplicationSemSpringDataRedis = noClasses()
            .that().resideInAnyPackage("..domains..", "..application..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework.data.redis..");

    @ArchTest
    static final ArchRule semCiclosEntrePacotes = slices()
            .matching("com.reminder.financial.api.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule semAutowiredEmCampo = noFields()
            .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired");
}
