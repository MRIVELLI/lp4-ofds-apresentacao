# OFDS: Teste de Software (LP4)

## Rodar o site

```powershell
.\mvnw.cmd spring-boot:run
```
Se aparecer "Unable to establish loopback connection":

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Djdk.net.unixdomain.tmpdir=C:/Users/Public"
```

## Testes Selenium

**Pelo Selenium IDE:** abrir o Selenium IDE, Open project > `testes-selenium.side` > **Run all tests**.

**Pelo PowerShell** (precisa do Node.js e do Chrome):

```powershell
npm install -g selenium-side-runner
selenium-side-runner testes-selenium.side
```
OU
```powershell
selenium-ide
```
