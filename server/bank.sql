create database if not exists serverdbSMS;
 
use serverdbSMS;
 
drop table if exists accountSMS;
create table accountSMS (
   iban char(25),
   balance int,
   username char(10),
   password char(8),
   counter int,
   publickey char(255),
   sharedkey varchar(8000),
   mobile char(9),
   tries int,
   primary key (mobile));
 
insert into accountSMS values ('PT12345678901234567890123', 100, 'nasTyMSR', '12345', 0, 'keys/nasTyMSRPublicKey', 'no', '911111111', 0);
insert into accountSMS values ('PT12345678901234567890124', 100, 'sigmaJEM', '12345', 0, 'keys/sigmaJEMPublicKey', 'no', '912222222', 0);
insert into accountSMS values ('PT12345678901234567890125', 100, 'jse', '12345', 0, 'keys/jsePublicKey','no', '913333333', 0);
insert into accountSMS values ('PT12345678901234567890126', 100, 'alpha', '12345', 0, 'keys/alphaPublicKey', 'no', '914444444', 0);
insert into accountSMS values ('PT12345678901234567890127', 100, 'pogchamp', '12345', 0, 'keys/pogchampPublicKey', 'no', '915555555', 0);
insert into accountSMS values ('PT12345678901234567890128', 100, 'bravo', '12345', 0, 'keys/bravoPublicKey','no' , '916666666', 0);
insert into accountSMS values ('PT12345678901234567890129', 99999999, 'austrolopi', '12345678', 0, 'keys/austrolopiPublicKey', 'no', '917777777', 0);