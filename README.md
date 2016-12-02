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
cd ../server
mvn install exec:java -Dexec.args="port" (deixar a correr num terminal a parte) por exemplo port=10000
cd ../client
mvn install exec:java -Dexec.args="port host" por exemplo port=100000 e host=localhost

```

[4] Clientes já disponiveis

Mobile: 911111111 username: nasTyMSR     password: 12345  
Mobile: 912222222 username: sigmaJEM     password: 12345  
Mobile: 913333333 username: jse          password: 12345  
Mobile: 914444444 username: alpha        password: 12345  
Mobile: 915555555 username: poghcamp     password: 12345  
Mobile: 916666666 username: bravo        password: 12345  
Mobile: 917777777 username: austrolopi   password: 1234567  
  
[5] Caso queria utilizar outros utilizadores

Adicionar na um "insert into" no script bank.sql da pasta server. 
Gerar as chaves para esse utilizador correndo a aplicação crypto da seguinte maneira:  
 
```
cd crypto
mvn exec:java -Dexec.args="server username1 username2" (server é sempre necessário)
```
 
-------------------------------------------------------------------------------
**FIM**
