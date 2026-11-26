# OFDS: Teste de Software (LP4)

Online Food Delivery System usado no trabalho de Teste de Software.

Precisa do **Java 17**. Com o Java 25 os testes com Mockito não rodam.

## Baixar

```powershell
git clone https://github.com/MRIVELLI/lp4-ofds-apresentacao.git
cd lp4-ofds-apresentacao
```

## Testes JUnit (IntelliJ)

1. File > Open > pasta do projeto e esperar o Maven carregar.
2. File > Project Structure > SDK: escolher o Java 17.
3. Botão direito em `src/test/java` > **Run 'All Tests'** (30 testes).
4. Cobertura: botão direito em `src/test/java` > More Run/Debug > **Run 'All Tests' with Coverage**.

Pelo PowerShell:

```powershell
.\mvnw.cmd test
```

## Rodar o site

```powershell
.\mvnw.cmd spring-boot:run
```

Abrir http://localhost:8080. Para parar: `Ctrl + C`.

Se aparecer "Unable to establish loopback connection":

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Djdk.net.unixdomain.tmpdir=C:/Users/Public"
```

| Perfil | E-mail | Senha |
|---|---|---|
| Cliente | alex@ofds.com | pass123 |
| Dono de restaurante | daniel@ofds.com | pass123 |
| Entregador | ravi@ofds.com | pass123 |
| Administrador | admin@ofds.com | admin123 |

## Testes Selenium

Os 5 testes estão em `testes-selenium.side`: login correto, login incorreto, login do administrador, cadastro de cliente e cadastro de entregador.

O site precisa estar rodando. Os testes de cadastro só passam uma vez por execução do site, porque o e-mail fica cadastrado: para repetir, pare o site e rode de novo.

**Pelo Selenium IDE:** abrir o Selenium IDE, Open project > `testes-selenium.side` > **Run all tests**.

**Pelo PowerShell** (precisa do Node.js e do Chrome):

```powershell
npm install -g selenium-side-runner
selenium-side-runner testes-selenium.side
```
