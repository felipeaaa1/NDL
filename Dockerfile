# usa uma imagem com JDK para: compilar a aplicação, baixar dependências Maven, gerar o artefato JAR da aplicação
FROM eclipse-temurin:21-jdk AS build

# define o diretório de trabalho padrão da etapa
WORKDIR /ndl

# copia a pasta ".mvn" do projeto local para a pasta ".mvn" dentro da imagem
COPY .mvn .mvn

# copia o script Maven Wrapper "mvnw" para o diretório atual da imagem
COPY mvnw .

# copia o pom.xml
COPY pom.xml .

# no Linux o script pode não possuir permissão de execução, sem isso o "./mvnw" pode falhar
RUN chmod +x mvnw

# baixa dependências e plugins Maven antecipadamente
# deixando tudo disponível no cache local do Maven
#
# essa layer fica cacheada enquanto:
# - pom.xml
# - mvnw
# - .mvn
# não mudarem
#
# isso evita baixar dependências novamente
# a cada alteração no código-fonte
RUN ./mvnw dependency:go-offline -B

# copia o código-fonte da aplicação esse COPY separado dos arquivos copiados acima evita invalidar a layer de dependências quando apenas o src mudar
COPY src src

# compile that bad boy (compila e empacota a aplicação)
RUN ./mvnw clean package -DskipTests

# inicia a etapa final da imagem, contendo apenas o Java Runtime Environment (JRE) a etapa anterior será usada apenas durante o build
# basicamente o que foi executado acima é "perdido" se não for explicitamente invocado como em --from=build /ndl/target/*.jar ndl.jar
#então além do pom, wmnv src e o jar, o resto (ferramentas do JDK) são descartadas
FROM eclipse-temurin:21-jre

LABEL maintainer="Felipe <arnaud.felipe96@gmail.com>"
LABEL application="NDL-Commerce"
LABEL description="API de e-commerce com Spring Boot, Java 21, PostgreSQL e JWT"
LABEL version="1.0.0-SNAPSHOT"

# define o diretório de trabalho da imagem final, o WORKDIR da etapa anterior não é reutilizado, porque cada FROM inicia um novo stage
WORKDIR /ndl

# cria um usuário sem privilégios administrativos para evitar executar a aplicação como root
RUN useradd -u 1001 appuser

# copia o JAR gerado na etapa "build" para a imagem final com nome customizado
COPY --from=build /ndl/target/*.jar ndl.jar

# define o usuário padrão da aplicação
USER appuser

# variáveis padrão para ambiente local se forem passadas no docker run ou docker compose os valores abaixo serão sobrescritos
ENV SENDGRID_TOKEN=test
ENV DOMINIO_APLICACAO=localhost:8080

# documenta que a aplicação utiliza a porta 8080 a publicação real da porta é feita no docker run usando o parâmetro "-p"
EXPOSE 8080

# define o processo principal executado quando o container iniciar
ENTRYPOINT ["java", "-jar", "ndl.jar"]