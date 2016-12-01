# Projeto de Segurança Informática em Redes e Sistemas 2016-2017 #

Grupo de SIRS 10 - Campus Alameda

Daniel Fermoselle   78207 daniel.fermosele@gmail.com

João Marçal         78471 joao.marcal12@gmail.com

Tiago Rodrigues     78692 tiagomsr4s@gmail.com

Repositório:
[JoaoBraveCoding/SMSPayment](https://github.com/JoaoBraveCoding/SMSPayment)

-------------------------------------------------------------------------------

## Instruções de instalação 


### Ambiente

[0] Iniciar sistema operativo

Linux

[1] Criar pasta temporária

```
cd ~
mkdir Project
```

[2] Obter código fonte do projeto (versão entregue)

```
git clone  https://github.com/JoaoBraveCoding/SMSPayment.git
git checkout tags/SIRS_R1
```

[3] Instalar módulos de bibliotecas auxiliares

```
cd crypto
mvn clean install
cd server
mvn install exec:java (deixar a correr num terminal a parte)
cd client
mvn install exec:java

```

[4] Clientes já disponiveis

Mobile: 913534674 username: nasTyMSR   password: 12345
Mobile: 915667357 username: sigmaJEM   password: 12345
Mobile: 912436744 username: Alpha      password: 12345
Mobile: 912456434 username: jse        password: 12345
Mobile: 912456423 username: aaaaaaaaaa password: 1234567

Caso queria utilizar outros utilizadores:
Adicionar utilizador ao construtor da classe Server.java
Gerar as chaves para esse utilizador correndo a aplicação crypto da seguinte maneira:
 
```
cd crypto
mvn exec:java -Dexec.args="username"

```
 
-------------------------------------------------------------------------------
**FIM**
