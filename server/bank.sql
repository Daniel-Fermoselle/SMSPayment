create database if not exists serverdbSMS;
 
use serverdbSMS;
 
drop table if exists accountSMS;
create table accountSMS (
   iban char(25),
   balance int,
   username char(10),
   password char(7),
   counter int,
   publickey char(255),
   mobile char(9),
   tries int,
   primary key (mobile));
 
insert into accountSMS values ('PT12345678901234567890123', 100, 'nasTyMSR', '12345', 0, 'keys/nasTyMSRPublicKey', '911111111', 0);
insert into accountSMS values ('PT12345678901234567890124', 100, 'sigmaJEM', '12345', 0, 'keys/sigmaJEMPublicKey', '912222222', 0);
insert into accountSMS values ('PT12345678901234567890125', 100, 'jse', '12345', 0, 'keys/jsePublicKey', '913333333', 0);
insert into accountSMS values ('PT12345678901234567890126', 100, 'alpha', '12345', 0, 'keys/alphaPublicKey', '914444444', 0);
insert into accountSMS values ('PT12345678901234567890127', 100, 'pogchamp', '12345', 0, 'keys/pogchampPublicKey', '915555555', 0);
insert into accountSMS values ('PT12345678901234567890128', 100, 'bravo', '12345', 0, 'keys/bravoPublicKey', '916666666', 0);
insert into accountSMS values ('PT12345678901234567890129', 99999999, 'austrolopi', '1234567', 0, 'keys/austrolopiPublicKey', '917777777', 0);
insert into accountSMS values ('PT12345678901234567890130', 1000, 'bob', '1234567', 0, 'keys/bobPublicKey', '918888888', 0);
insert into accountSMS values ('PT12345678901234567890131', 50, 'alice', '1234567', 0, 'keys/alicePublicKey', '919999999', 0);
insert into accountSMS values ('PT12345678901234567890132', 1, 'mallory', '1234567', 0, 'keys/malloryPublicKey', '921111111', 0);