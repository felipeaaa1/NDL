package com.ndlcommerce;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

@SpringBootApplication
public class NdlCommerceApplication {

  public static void main(String[] args) {

    System.out.printf("\n\uD83D\uDD28 Starting NdlCommerceApplication");
    long startTime = System.currentTimeMillis();

    SpringApplication.run(NdlCommerceApplication.class, args);

    long endTime = System.currentTimeMillis();
    long startupTime = endTime - startTime;
    System.out.printf(
        "\n\uD83D\uDE80 Application started in %d ms (%d seconds) ",
        startupTime, startupTime / 1000);
  }

  // Passo 3 — Post-processor para “plugar” nosso scanner manual
  @Bean
  BeanFactoryPostProcessor beanFactoryPostProcessor(ApplicationContext ctx) {
    return beanFactory -> {
      if (ctx instanceof AnnotationConfigServletWebServerApplicationContext webCtx) {
        genericApplicationContext((BeanDefinitionRegistry) webCtx.getBeanFactory());
      }
    };
  }

  // passo 0: colocar um logger para acompanhar o procesos na tela
  @Bean
  BeanPostProcessor beanCreationLogger() {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(Object bean, String beanName)
          throws BeansException {
        String pkg = bean.getClass().getPackageName();
        if (pkg.contains("com.ndlcommerce")) {
          System.out.println("\u2699\uFE0F Initializing bean: " + beanName);
        }
        return bean;
      }
    };
  }

  // Passo 1 — Scanner manual
  void genericApplicationContext(BeanDefinitionRegistry beanRegistry) {
    // useDefaultFilters = false -> evita conflito com o component-scan padrão
    ClassPathBeanDefinitionScanner scanner =
        new ClassPathBeanDefinitionScanner(beanRegistry, false);

    // Inclui “tudo que NÃO termina com Model”
    scanner.addIncludeFilter(removeModelAndEntitiesFilter());

    // Dica: limite o scan a pacotes que NÃO têm controllers para evitar duplicidade
    // (controllers já serão achados pelo @SpringBootApplication)
    scanner.scan(
        "com.ndlcommerce.useCase",
        "com.ndlcommerce.adapters.persistence.*",
        // tinha esquecido a camada de persistencia, com isso não tinha instanciado a
        // UserRegisterInteractor pq não tinha nenhuma classe implementando a UserRegisterDsGateway,
        // que no caso quem ta implementando é a JpaUser.java
        //              ,"com.ndlcommerce.adapters.persistence"
        "com.ndlcommerce.entity.factory"
        // NÃO scanneie "controllers" aqui. Deixe o @SpringBootApplication encontrá-los com o
        // @RestController
        );
  }

  // Passo 2 — Filtro que ignora DTOs (*Model)
  static TypeFilter removeModelAndEntitiesFilter() {
    return (MetadataReader mr, MetadataReaderFactory mrf) ->
        (!mr.getClassMetadata().getClassName().endsWith("DTO")
            && !mr.getClassMetadata().getClassName().toLowerCase().contains("exception")
            && !mr.getClassMetadata().getClassName().contains(".useCase.model."));
  }
}
