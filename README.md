Projekt je napravljen pomoću Spring Boot frameworka i razdijeljen na tri standardna sloja:

- Presentation layer ([REST controller](src/main/java/rba/zadatak/Controller.java))
- Application logic ([Service](src/main/java/rba/zadatak/service/CardApplicantServiceImpl.java))
- Data layer ([Repository](src/main/java/rba/zadatak/repository/CardRepositoryImpl.java),[File repository fragment](src/main/java/rba/zadatak/repository/FileRepositoryFragmentImpl.java),[JPA repository fragment](src/main/java/rba/zadatak/repository/JPARepositoryFragment.java))

[Controller](src/main/java/rba/zadatak/Controller.java) nudi 3 endpointa:
- /api/save **POST**
- /api/find/{id} **GET**
- /api/edit **PUT**
- /api/delete/{id} **DELETE**

Kontroler očekuje sljedeći JSON objekt { firstName:**string**, lastName:**string**, pin: **string**}

Polje *status* se ignorira prilikom deserijalizacije jer njime upravlja aplikacija, ali vraća se prilikom serijalizacije kao odgovor u  /api/find/{id} endpointu.

*Status* status je enum koji  može imati sljedeće vrijednosti: *PENDING*, *ACCEPTED*, *OUTDATED*, *DELETED*

- *PENDING* označava stanje da je zahtjev za izradom kartice predan pomoću **/save** endpointa, ali još nije generirana tekstualna datoteka.
- *ACCEPTED* označava stanje da je zahtjev prihvaćen (pozivanjem **/find/{pin}** endpointa) za izradu i generirana je tekstualna datoteka na disku.
- *OUTDATED* označava da su korisnički podaci promijenjeni. Ako se pozove **/edit** endpoint, a datoteka je izrađena, tada će datoteka sadržavati status *OUTDATED*, a korisnički podaci u bazi će se promijeniti i novi status bit će *PENDING*, tj. bit će potrebno generirati datoteku s ažuriranim podacima pomoću **find** endpointa
- *DELETE*D status znači da je korisnik izbrisan iz baze podataka, a njegova aktivna datoteka ima status *DELETED*

Korisnik ima aktivnu datoteku samo ako je *ACCEPTED*. Ukoliko dođe do promjene statusa, ta datoteka ostaje, ali u nju se upisuje status *DELETED* ili *OUTDATED*.



## Validacija ulaza
Validacija se vrši pomoću anotacija na DTO ([CardApplicantDto](src/main/java/rba/zadatak/dto/CardApplicantDto.java)) i Entity modelu([CardApplicant](src/main/java/rba/zadatak/entity/CardApplicant.java)). Anotacije osiguravaju da će se prilikom određenih akcija (npr JSON deserijalizacija ili upis u bazu podataka) u slučaju neispravnih podataka podignuti iznimka. Napravljen je i JSON deserializer za DTO klasu koji nakon deserijalizacije poziva Validator bean i pomoću njega detektira zadovoljava li novonastali objekt pravila definirana anotacijama.

Pretvorba između DTO i Entity beansa vrši se u ([Controlleru](src/main/java/rba/zadatak/Controller.java)) pomoću MapStruct knjižnice kako bi se izbjegao boilerplate kod. Lombok knjižnica koristi se za automatsko generiranje getter i setter metoda. 


## Izolacija u kontekstu paralelnog korištenja

Java Servleti stvaraju novi thread za svaki HTTP request u obradi. To dovodi do paralelnog pristupanja i mijenjanja podataka što može utjecati na valjanost programa.
Kako bi se osigurala sinkronizacija threadova, aplikacijska logika koristi lockove iz RDBMS-a čime se sprječava da različiti threadovi istovremeno pristupaju istom Entityu ako je u tijeku operacija koja bi mogla ugroziti konzistentnost podataka. Lockovi se koriste tako da je se u [JPA repozitoriju](src/main/java/rba/zadatak/repository/JPARepositoryFragment.java) postavljaju anotacije na metode kojima se dohvaća Entity iz baze podataka.

Kako bi se spriječio racing uvjet u slučaju stvaranja resursa pomoću HTTP POST metode (kada u bazi ne postoji Entity koji se može iskoristiti kao lock), implementirana je mapa objekata za sinkronizaciju threadova u [Service klasi](src/main/java/rba/zadatak/service/CardApplicantServiceImpl.java).

## Transakcije
Osim što pomažu sinkronizaciji pristupa podacima, transakcije su važna jer određuju rollback točke. U slučaju jedinstvenog Data Sourcea (npr relacijske baze) dovoljno je anotirati metode s @Transactional anotacijom i framework će automatski rollbackat sve promjene u bazi ako metoda rezultira *unchecked exceptionom*.

U ovom zadatku postoje dva Data Sourcea: embedded H2 baza podataka i disk na kojem se spremaju datoteke. Prilikom izvođenja transakcija važno je voditi računa da se u slučaju greške promjene ne rollbackaju samo u relacijskoj bazi već i na disku.


## Logging

Spring Boot već sadrži defaultni logger koji je iskorišten za zapisivanje edge case scenarija u posebnu datoteku. [logback.xml](/src/main/resources/logback.xml)


## Testovi
Testovi su implementirani pomoću JUnit frameworka i koriste @SpringBootTest anotaciju koja omogućuje testiranje čitave aplikacije. U [ControllerTest](src/test/java/rba/zadatak/ControllerTest.java) klasi nalaze se testovi CRUD operacija definiranih u kontroleru. [InputValidationTest](src/test/java/rba/zadatak/InputValidationTest.java) sadrži testove za ulazne podatke koji se dobivaju JSON deserijalizacijom. [FileRepositoryTest](src/test/java/rba/zadatak/FileRepositoryTest.java) sadrži testove za upravljanje datotekama u koje se spremaju Entity objekti.
