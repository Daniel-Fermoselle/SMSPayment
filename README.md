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

Ubuntu 16.04 LTS (OS usado para desenvolvimento do projecto Mac OSx versão 10.11.6 arch: x86_64)  
Java version "1.8.0_111" (versão usada para desenvolvimento do projecto)  
MySQL version 5.7.16 (versão usada para desenvolvimento do projecto)  
Apache Maven version 3.3.9 (versão usada para desenvolvimento do projecto)  
O utilizador têm de ter sun.security.ec.SunEC, sun.security.provider.Sun e BouncyCastel como security providers  

[1] Criar pasta temporária

```
cd ~
mkdir Project
cd Project
```

[2] Obter código fonte do projeto (versão entregue)

```
git clone  https://github.com/JoaoBraveCoding/SMSPayment.git
git checkout tags/SIRS_R1
```

[3] Setup da base de dados 

```
cd server
Entrar no mysql com uma conta: mysql -uroot -p (para entrar com root, tem de saber a password, poderá utilizar outro utilizador se ja o tiver configurado)
source bank.sql;
\q (para sair do mysql)

```

[4] Instalar módulos de bibliotecas auxiliares

```

cd ../crypto
mvn clean install
cd ../server
mvn install exec:java -Dexec.args="port" (deixar a correr num terminal a parte) por exemplo port=10000
cd ../client
mvn install exec:java -Dexec.args="port host" por exemplo port=10000 e host=localhost

```

[5] Clientes já disponiveis

Mobile: 911111111 username: nasTyMSR     password: 12345  
Mobile: 912222222 username: sigmaJEM     password: 12345  
Mobile: 913333333 username: jse          password: 12345  
Mobile: 914444444 username: alpha        password: 12345  
Mobile: 915555555 username: poghcamp     password: 12345  
Mobile: 916666666 username: bravo        password: 12345  
Mobile: 917777777 username: austrolopi   password: 1234567  
Mobile: 918888888 username: bob          password: 1234567  
Mobile: 919999999 username: alice        password: 1234567  
Mobile: 921111111 username: mallory      password: 1234567  

  
[6] Caso queria utilizar outros utilizadores

Adicionar um "insert into" no script bank.sql da pasta server. 
Gerar as chaves para esse utilizador correndo a aplicação crypto da seguinte maneira:  
 
```
cd crypto
mvn exec:java -Dexec.args="username1 username2" (caso queira gerar uma nova chave para o server basta passar como argumeno "server")
```
 
-------------------------------------------------------------------------------
**FIM**
